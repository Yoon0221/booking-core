package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.api.booking.dto.BookingResponse;
import io.jiyoon.booking_core.api.booking.dto.ReserveResult;
import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.booking.converter.BookingConverter;
import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import io.jiyoon.booking_core.domain.booking.repository.BookingRepository;
import io.jiyoon.booking_core.domain.payment.converter.PaymentConverter;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import io.jiyoon.booking_core.domain.payment.repository.PaymentRepository;
import io.jiyoon.booking_core.domain.product.entity.Product;
import io.jiyoon.booking_core.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TicketingRedisService ticketingRedisService;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConverter paymentConverter;
    private final BookingConverter bookingConverter;

    @Override
    @Transactional
    public BookingResponse reserve(Long productId, Long userId) {

        // Redis 선점 시도
        ReserveResult result = ticketingRedisService.tryReserve(productId, userId);

        return switch (result.status()) {

            case SUCCESS -> {
                // 상품 조회
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.PRODUCT_NOT_FOUND));

                // Booking 엔티티 생성, 저장 (INIT 상태)
                Booking booking = bookingConverter.toBooking(userId, product);
                bookingRepository.save(booking);

                // 응답 반환
                yield BookingResponse.builder()
                        .bookingId(booking.getId())
                        .bookingStatus(BookingStatus.INIT)
                        .remainMillis(result.remainMillis())
                        .build();
            }

            case ALREADY_RESERVED -> throw new CustomException(ErrorStatus.BOOKING_ALREADY_RESERVED);
            case ALREADY_PAID -> throw new CustomException(ErrorStatus.PAYMENT_ALREADY);
            case SOLD_OUT -> throw new CustomException(ErrorStatus.PRODUCT_SOLD_OUT);

            default -> throw new CustomException(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR);
        };
    }

    @Override
    @Transactional
    public String pay(Long productId, Long userId) {

        // Redis 기반 결제 가능 여부 검증 (TTL / 예약 상태 확인) + 결제 완료 처리
        boolean canPay = ticketingRedisService.tryPay(productId, userId);

        if (!canPay) {
            throw new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID);
        }

        // 해당 유저의 INIT 예약 조회
        Booking booking = bookingRepository.findByProductIdAndUserIdAndStatus(
                        productId,
                        userId,
                        BookingStatus.INIT
                )
                .orElseThrow(() -> new CustomException(ErrorStatus.BOOKING_CONFIRM_INVALID));

        // 결제 상태 PENDING 전환
        booking.markPaymentPending();

        // Payment 엔티티 생성 (아직 외부 PG 없음 → 내부 상태 모델링만)
        Payment payment = paymentConverter.toPayment(
                booking,
                PaymentMethod.CARD,
                booking.getTotalAmount()
        );

        // Booking ↔ Payment 연관관계 연결
        booking.addPayment(payment);

        // 결제 성공 처리
        payment.success();

        // 예약 확정 처리
        booking.confirm();

        // 재고 차감 (최종 확정 시점)
        Product product = booking.getProduct();
        product.decreaseStock();

        // Payment 저장 (Booking은 영속 상태라 dirty checking)
        paymentRepository.save(payment);

        return "PAYMENT_SUCCESS";
    }

}