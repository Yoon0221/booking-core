package io.jiyoon.booking_core.domain.booking.repository;

import io.jiyoon.booking_core.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
}