package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.domain.payment.dto.PaymentRequestDto;

public interface BookingPayService {

    /**
     * 예약에 사용할 결제 수단 및 금액 정보 등록
     *
     * - 예약 상태가 INIT 인 경우에만 결제 수단 등록 가능
     * - 결제 금액 총합과 예약 금액 일치 여부 검증
     * - CARD + YPAY 중복 사용 불가 정책 검증
     * - 결제 정보를 Payment 엔티티로 변환 후 Booking 에 저장
     */
    void registerPaymentMethods(Long bookingId, Long userId, PaymentRequestDto.RegisterMethods request);

    /**
     * 외부 PG 결제 승인 처리
     *
     * - READY 상태의 CARD/YPAY 결제 건만 승인 가능
     * - 결제 수단에 맞는 PgPaymentClient 를 통해 외부 승인 요청 수행
     * - 승인 성공 시 Payment SUCCESS 및 Booking PAYMENT_PENDING 상태 반영
     * - 승인 실패 시 Payment FAILED 처리 후 예외 발생
     */
    void processPgPayment(Long bookingId, Long userId, PaymentRequestDto.PgApprove request);

    /**
     * 최종 예약 확정 및 내부 포인트 결제 처리
     *
     * - 외부 PG 결제 승인 완료 여부 검증
     * - POINT 결제 건 존재 시 사용자 포인트 차감 및 결제 완료 처리
     * - Redis 선점 상태를 결제 완료 상태로 변경
     * - 예약 CONFIRMED 처리 및 상품 재고 차감
     * - 실패 시 BookingFailedEvent 발행을 통해 보상 트랜잭션 수행
     */
    void completeBookingAndPoint(Long bookingId, Long userId);
}