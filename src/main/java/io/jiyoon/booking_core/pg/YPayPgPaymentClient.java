package io.jiyoon.booking_core.pg;

import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class YPayPgPaymentClient implements PgPaymentClient {

    @Override
    public PaymentMethod getSupportMethod() {
        return PaymentMethod.YPAY;
    }

    @Override
    public void approve(Long paymentId, BigDecimal amount) {
        // Y페이 전용 내부 승인 API 호출 로직
    }

    @Override
    public void cancel(Long paymentId, BigDecimal amount) {
        // Y페이 전용 내부 취소 API 호출 로직
    }
}