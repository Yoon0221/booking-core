package io.jiyoon.booking_core.domain.payment.repository;

import io.jiyoon.booking_core.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}