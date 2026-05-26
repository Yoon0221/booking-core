package io.jiyoon.booking_core.apiPayload.status;

import io.jiyoon.booking_core.apiPayload.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements ResponseCode {

    // 공통 에러
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMON_500","서버 에러가 발생했습니다. 관리자에게 문의하세요."),
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."),
    COMMON_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP Method 입니다."),

    // REDIS
    REDIS_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,"REDIS_503","예약 시스템을 일시적으로 사용할 수 없습니다."),

    // BOOKING
    BOOKING_CONFIRM_INVALID(HttpStatus.BAD_REQUEST, "BOOKING_4001", "예약 확정이 불가능한 상태입니다."),
    BOOKING_FAIL_INVALID(HttpStatus.BAD_REQUEST, "BOOKING_4002", "예약 실패 처리가 불가능한 상태입니다."),
    BOOKING_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "BOOKING_4003", "이미 예약된 상품입니다."),

    // PAYMENT
    PAYMENT_SUCCESS_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_4001", "결제 성공 처리가 불가능한 상태입니다."),
    PAYMENT_FAIL_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_4002", "결제 실패 처리가 불가능한 상태입니다."),
    PAYMENT_CANCEL_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_4003", "결제 취소 처리가 불가능한 상태입니다."),
    PAYMENT_ALREADY(HttpStatus.BAD_REQUEST, "PAYMENT_4004", "이미 결제가 완료되었습니다."),

    // PRODUCT
    PRODUCT_SOLD_OUT(HttpStatus.BAD_REQUEST, "PRODUCT_4001", "재고가 부족합니다."),
    PRODUCT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT_4002", "판매 가능한 상품 상태가 아닙니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_4041", "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "PRODUCT_4003", "판매 시작 전 상품입니다."),

    // POINT
    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "POINT_4001", "포인트가 부족합니다."),
    POINT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_4002", "유효하지 않은 포인트 금액입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}