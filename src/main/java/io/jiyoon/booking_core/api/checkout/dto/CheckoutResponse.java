package io.jiyoon.booking_core.api.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CheckoutResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;

    private BigDecimal availableUserPoint;

    private boolean reservable;
}