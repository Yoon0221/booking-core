package io.jiyoon.booking_core.domain.payment.converter;

import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import io.jiyoon.booking_core.domain.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentConverter {

    public Payment toPayment(Booking booking, PaymentMethod paymentMethod, BigDecimal amount) {
        return Payment.builder()
                .booking(booking)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build();
    }
}