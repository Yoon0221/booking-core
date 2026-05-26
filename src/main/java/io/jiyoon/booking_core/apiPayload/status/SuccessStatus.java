package io.jiyoon.booking_core.apiPayload.status;

import io.jiyoon.booking_core.apiPayload.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements ResponseCode {

    SUCCESS(HttpStatus.OK, "COMMON_200", "성공입니다."),

    // Checkout API
    CHECKOUT_SUCCESS(HttpStatus.OK, "CHECKOUT_2001", "주문서 조회 성공입니다."),

    // Booking API
    BOOKING_RESERVE_SUCCESS(HttpStatus.OK, "BOOKING_2001", "예약 선점 성공입니다."),
    BOOKING_PAYMENT_SUCCESS(HttpStatus.OK, "BOOKING_2002", "결제 처리 성공입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}