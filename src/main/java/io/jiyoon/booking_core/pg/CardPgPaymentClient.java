package io.jiyoon.booking_core.pg;

import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPgPaymentClient implements PgPaymentClient {

    @Override
    public PaymentMethod getSupportMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public void approve(Long paymentId, BigDecimal amount) {
        // 신용카드사 전용 승인 API 호출 로직
    }

    @Override
    public void cancel(Long paymentId, BigDecimal amount) {
        // 신용카드사 전용 취소 API 호출 로직
    }
}