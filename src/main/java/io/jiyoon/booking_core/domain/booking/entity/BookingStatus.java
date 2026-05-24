package io.jiyoon.booking_core.domain.booking.entity;

public enum BookingStatus {
    INIT,
    PAYMENT_PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED
}