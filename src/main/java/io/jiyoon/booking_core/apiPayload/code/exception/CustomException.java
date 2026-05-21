package io.jiyoon.booking_core.apiPayload.code.exception;

import io.jiyoon.booking_core.apiPayload.code.ResponseCode;

public class CustomException extends RuntimeException {

    private final ResponseCode errorCode;

    public CustomException(ResponseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ResponseCode getErrorCode() {
        return errorCode;
    }
}