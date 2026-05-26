package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.api.booking.dto.BookingResponse;

public interface BookingService {

    /**
     * 예약 요청
     * - Redis 선점 검증 (재고/중복/판매 상태)
     * - 성공 시 DB Booking 생성
     */
    BookingResponse reserve(Long productId, Long userId);

    /**
     * 결제 처리
     * - Redis 결제 가능 여부 검증
     * - Booking → Payment 생성 및 확정
     */
    String pay(Long productId, Long userId);
}