package io.jiyoon.booking_core.domain.booking.entity;

import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.entity.BaseEntity;
import io.jiyoon.booking_core.domain.product.entity.Product;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.INIT;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    public void confirm() {
        if (this.status != BookingStatus.PAYMENT_PENDING) {
            throw new CustomException(ErrorStatus.BOOKING_CONFIRM_INVALID);
        }

        this.status = BookingStatus.CONFIRMED;
    }

    public void fail() {
        if (this.status != BookingStatus.PAYMENT_PENDING) {
            throw new CustomException(ErrorStatus.BOOKING_FAIL_INVALID);
        }

        this.status = BookingStatus.FAILED;
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.assignBooking(this);
    }

    public void markPaymentPending() {
        this.status = BookingStatus.PAYMENT_PENDING;
    }
}