package io.jiyoon.booking_core.domain.product.entity;

import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.entity.BaseEntity;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer remainingStock;

    @Column(nullable = false)
    private LocalDateTime saleStartAt;

    @Column(nullable = false)
    private LocalDateTime checkInAt;

    @Column(nullable = false)
    private LocalDateTime checkOutAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    public void decreaseStock() {
        if (status != ProductStatus.ACTIVE) {
            throw new CustomException(ErrorStatus.PRODUCT_NOT_ACTIVE);
        }

        if (remainingStock <= 0) {
            throw new CustomException(ErrorStatus.PRODUCT_SOLD_OUT);
        }

        this.remainingStock--;

        if (remainingStock == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    public void increaseStock() {
        this.remainingStock++;

        if (status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.ACTIVE;
        }
    }
}