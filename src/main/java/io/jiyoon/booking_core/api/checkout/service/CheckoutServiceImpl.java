package io.jiyoon.booking_core.api.checkout.service;

import io.jiyoon.booking_core.api.checkout.converter.CheckoutConverter;
import io.jiyoon.booking_core.api.checkout.dto.CheckoutResponse;
import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.point.entity.UserPoint;
import io.jiyoon.booking_core.domain.point.repository.UserPointRepository;
import io.jiyoon.booking_core.domain.product.entity.Product;
import io.jiyoon.booking_core.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutServiceImpl implements CheckoutService {

    private final ProductRepository productRepository;
    private final UserPointRepository userPointRepository;

    @Override
    public CheckoutResponse getCheckout(Long productId, Long userId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorStatus.PRODUCT_NOT_FOUND));

        UserPoint userPoint = userPointRepository.findById(userId)
                .orElse(UserPoint.empty(userId));

        boolean reservable = product.isReservable();

        return CheckoutConverter.toResponse(product, userPoint, reservable);
    }
}