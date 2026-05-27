package io.jiyoon.booking_core.api.booking.dto;

import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public class PaymentRequestDto {

    public record RegisterMethods(
            List<Detail> paymentDetails
    ) {}

    public record Detail(
            PaymentMethod paymentMethod,
            BigDecimal amount
    ) {}

    public record PgApprove(
            PaymentMethod paymentMethod
    ) {}
}