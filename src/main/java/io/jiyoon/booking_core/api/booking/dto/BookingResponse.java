package io.jiyoon.booking_core.api.booking.dto;

import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookingResponse{
    private Long bookingId;
    private BookingStatus bookingStatus;
    private Long remainMillis;
}