package io.jiyoon.booking_core.domain.point.entity;

import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.BaseEntity;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPoint extends BaseEntity {

    @Id
    private Long userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    public static UserPoint empty(Long userId) {
        return UserPoint.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build();
    }

    public void use(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorStatus.POINT_INVALID_AMOUNT);
        }

        if (balance.compareTo(amount) < 0) {
            throw new CustomException(ErrorStatus.POINT_NOT_ENOUGH);
        }

        this.balance = this.balance.subtract(amount);
    }

    public void restore(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorStatus.POINT_INVALID_AMOUNT);
        }

        this.balance = this.balance.add(amount);
    }
}