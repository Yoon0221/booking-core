package io.jiyoon.booking_core.domain.payment.converter;

import io.jiyoon.booking_core.domain.payment.dto.PaymentRequestDto;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.domain.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentConverter {

    public Payment toPayment(PaymentRequestDto.Detail detail) {
        return Payment.builder()
                .paymentMethod(detail.paymentMethod())
                .amount(detail.amount())
                .status(PaymentStatus.READY)
                .build();
    }

}