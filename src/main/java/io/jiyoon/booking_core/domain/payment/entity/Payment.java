package io.jiyoon.booking_core.domain.payment.entity;

import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.BaseEntity;
import io.jiyoon.booking_core.domain.booking.entity.Booking;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.READY;

    public void success() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorStatus.PAYMENT_SUCCESS_INVALID);
        }

        this.status = PaymentStatus.SUCCESS;
    }

    public void fail() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorStatus.PAYMENT_FAIL_INVALID);
        }

        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        if (this.status != PaymentStatus.READY &&
                this.status != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorStatus.PAYMENT_CANCEL_INVALID);
        }

        this.status = PaymentStatus.CANCELLED;
    }

    public void assignBooking(Booking booking) {
        this.booking = booking;
    }
}