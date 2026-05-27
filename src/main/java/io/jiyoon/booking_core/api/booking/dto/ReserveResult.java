package io.jiyoon.booking_core.api.booking.dto;

import io.jiyoon.booking_core.domain.booking.entity.ReserveStatus;

public record ReserveResult(
        ReserveStatus status,
        long remainMillis
) {}