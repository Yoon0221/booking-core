package io.jiyoon.booking_core.domain.booking.repository;

import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByProductIdAndUserIdAndStatus(Long productId, Long userId, BookingStatus bookingStatus);

}