package io.jiyoon.booking_core.api.booking.converter;

import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import io.jiyoon.booking_core.domain.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class BookingConverter {

    public Booking toBooking(Long userId, Product product) {
        return Booking.builder()
                .userId(userId)
                .product(product)
                .totalAmount(product.getPrice())
                .status(BookingStatus.INIT)
                .build();
    }

}