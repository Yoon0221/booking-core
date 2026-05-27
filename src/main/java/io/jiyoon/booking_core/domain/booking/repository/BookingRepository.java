package io.jiyoon.booking_core.domain.booking.repository;

import io.jiyoon.booking_core.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}