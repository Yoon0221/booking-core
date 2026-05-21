package io.jiyoon.booking_core.apiPayload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jiyoon.booking_core.apiPayload.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    private final boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    public static ApiResponse<Void> success(ResponseCode code) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(ResponseCode code, T result) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static <T> ApiResponse<T> failure(ResponseCode code, T result) {
        return new ApiResponse<>(false, code.getCode(), code.getMessage(), result);
    }

}