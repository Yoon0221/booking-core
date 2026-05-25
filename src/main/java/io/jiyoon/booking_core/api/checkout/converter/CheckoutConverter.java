package io.jiyoon.booking_core.api.checkout.converter;

import io.jiyoon.booking_core.api.checkout.dto.CheckoutResponse;
import io.jiyoon.booking_core.domain.point.entity.UserPoint;
import io.jiyoon.booking_core.domain.product.entity.Product;

public class CheckoutConverter {

    public static CheckoutResponse toResponse(
            Product product,
            UserPoint userPoint,
            boolean reservable
    ) {
        return CheckoutResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .checkInAt(product.getCheckInAt())
                .checkOutAt(product.getCheckOutAt())
                .availableUserPoint(userPoint.getBalance())
                .reservable(reservable)
                .build();
    }
}