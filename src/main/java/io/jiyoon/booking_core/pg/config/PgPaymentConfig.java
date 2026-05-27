package io.jiyoon.booking_core.pg.config;

import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import io.jiyoon.booking_core.pg.PgPaymentClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class PgPaymentConfig {

    @Bean
    public Map<PaymentMethod, PgPaymentClient> pgPaymentClients(List<PgPaymentClient> pgPaymentClientList) {
        return pgPaymentClientList.stream()
                .collect(Collectors.toMap(
                        PgPaymentClient::getSupportMethod,
                        client -> client
                ));
    }
}