package io.jiyoon.booking_core.api.checkout.service;

import io.jiyoon.booking_core.api.checkout.dto.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse getCheckout(Long productId, Long userId);
}