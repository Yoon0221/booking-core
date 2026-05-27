package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import io.jiyoon.booking_core.domain.booking.repository.BookingRepository;
import io.jiyoon.booking_core.api.booking.converter.PaymentConverter;
import io.jiyoon.booking_core.api.booking.dto.PaymentRequestDto;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import io.jiyoon.booking_core.domain.payment.entity.PaymentStatus;
import io.jiyoon.booking_core.api.booking.event.BookingFailedEvent;
import io.jiyoon.booking_core.domain.point.entity.UserPoint;
import io.jiyoon.booking_core.domain.point.repository.UserPointRepository;
import io.jiyoon.booking_core.pg.PgPaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPayServiceImpl implements BookingPayService {

    private final BookingRepository bookingRepository;
    private final UserPointRepository userPointRepository;
    private final Map<PaymentMethod, PgPaymentClient> pgPaymentClients;
    private final TicketingRedisService ticketingRedisService;
    private final PaymentConverter paymentConverter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void registerPaymentMethods(Long bookingId, Long userId, PaymentRequestDto.RegisterMethods request) {

        // 1. 예약 내역 존재 여부 및 권한 검증
        Booking booking = getValidBooking(bookingId, userId);

        // 2. 예약이 최초 생성 단계(INIT) 상태일 때만 결제 수단 등록 허용
        if (booking.getStatus() != BookingStatus.INIT) {
            throw new CustomException(ErrorStatus.BOOKING_CONFIRM_INVALID);
        }

        // 3. 요청으로 들어온 각 결제 수단별 금액의 총합 계산
        BigDecimal totalInputAmount = request.paymentDetails().stream()
                .map(PaymentRequestDto.Detail::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 허용되지 않는 외부 결제 조합 검증 (신용카드 + Y페이 동시 선택 차단)
        validatePaymentCombination(request);

        // 5. 실제 예약 금액과 결제 금액 총합 일치 여부 검증
        if (booking.getTotalAmount().compareTo(totalInputAmount) != 0) {
            throw new CustomException(ErrorStatus.POINT_INVALID_AMOUNT);
        }

        // 6. 컨버터를 활용하여 엔티티 매핑 후 Booking 엔티티에 연관관계 편입
        request.paymentDetails().forEach(detail -> booking.addPayment(paymentConverter.toPayment(detail)));
    }

    @Override
    @Transactional
    public void processPgPayment(Long bookingId, Long userId, PaymentRequestDto.PgApprove request) {

        // 1. 예약 내역 존재 여부 및 권한 검증
        Booking booking = getValidBooking(bookingId, userId);

        // 2. 승인 대상 외부 결제 수단 조회 (READY 상태인 CARD/YPAY 필터링)
        Payment targetPayment = booking.getPayments().stream()
                .filter(p -> p.getPaymentMethod() == request.paymentMethod() && p.getStatus() == PaymentStatus.READY)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID));

        // 3. PG 승인
        PgPaymentClient matchedClient = getClient(request.paymentMethod());
        try {
            matchedClient.approve(targetPayment.getId(), targetPayment.getAmount());
        } catch (Exception e) {

            // PG 실패 시 PaymentStatus 업데이트 (FAILED)
            targetPayment.fail();
            throw e;
        }

        // 4. PG 성공 이후 DB 반영
        targetPayment.success();
        booking.markPaymentPending();
    }

    @Override
    @Transactional
    public void completeBookingAndPoint(Long bookingId, Long userId) {

        // 1. 예약 내역 존재 여부 및 권한 검증
        Booking booking = getValidBooking(bookingId, userId);

        // 2. 복합 결제시 PG 승인 상태 체크
        validatePgApprovalStatus(booking);

        // 3. 내부 포인트 결제 건 조회
        Payment pointPayment = booking.getPayments().stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.POINT && p.getStatus() == PaymentStatus.READY)
                .findFirst().orElse(null);

        try {
            // 포인트 결제
            if (pointPayment != null) {
                UserPoint userPoint = userPointRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.POINT_NOT_ENOUGH));
                userPoint.use(pointPayment.getAmount());
                pointPayment.success();
            }

            // Redis 완료 처리
            ticketingRedisService.tryPay(booking.getProduct().getId(), userId);

            // DB 결제 완료
            booking.confirm();
            booking.getProduct().decreaseStock();

        } catch (RuntimeException e) {
            log.error("최종 확정 실패, 보상 이벤트 발행. Booking ID: {}", bookingId);
            // 메인 트랜잭션 롤백과 함께 외부 시스템 복구를 위한 이벤트 발행
            eventPublisher.publishEvent(new BookingFailedEvent(bookingId, userId));
            throw e;
        }
    }

    private Booking getValidBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException(ErrorStatus.BOOKING_CONFIRM_INVALID));
        if (!booking.getUserId().equals(userId)) {
            throw new CustomException(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR);
        }
        return booking;
    }

    private void validatePaymentCombination(PaymentRequestDto.RegisterMethods request) {
        boolean hasCard = request.paymentDetails().stream().anyMatch(d -> d.paymentMethod() == PaymentMethod.CARD);
        boolean hasYPay = request.paymentDetails().stream().anyMatch(d -> d.paymentMethod() == PaymentMethod.YPAY);
        if (hasCard && hasYPay) throw new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID);
    }

    private PgPaymentClient getClient(PaymentMethod method) {
        return Optional.ofNullable(pgPaymentClients.get(method))
                .orElseThrow(() -> new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID));
    }

    private void validatePgApprovalStatus(Booking booking) {
        boolean hasUnapprovedPg = booking.getPayments().stream()
                .anyMatch(p -> (p.getPaymentMethod() == PaymentMethod.CARD || p.getPaymentMethod() == PaymentMethod.YPAY)
                        && p.getStatus() != PaymentStatus.SUCCESS);
        if (hasUnapprovedPg) throw new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID);
    }

}