package io.jiyoon.booking_core.domain.booking.entity;

public enum ReserveStatus {
    SUCCESS,            // 예약 성공
    ALREADY_RESERVED,   // 중복 예약
    ALREADY_PAID,       // 이미 결제 완료
    SOLD_OUT,           // 재고 없음
    ERROR               // 시스템 오류
}