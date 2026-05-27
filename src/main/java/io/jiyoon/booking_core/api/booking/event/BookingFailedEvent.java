package io.jiyoon.booking_core.api.booking.event;

public record BookingFailedEvent(Long bookingId, Long userId) {}