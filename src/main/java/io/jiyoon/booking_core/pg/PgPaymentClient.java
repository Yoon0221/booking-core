package io.jiyoon.booking_core.pg;

import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import java.math.BigDecimal;

public interface PgPaymentClient {
    // 결제 수단 (CARD 또는 YPAY)
    PaymentMethod getSupportMethod();

    void approve(Long paymentId, BigDecimal amount);
    void cancel(Long paymentId, BigDecimal amount);
}