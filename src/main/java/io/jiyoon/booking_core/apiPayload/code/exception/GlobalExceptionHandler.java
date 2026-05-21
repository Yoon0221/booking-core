package io.jiyoon.booking_core.apiPayload.code.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jiyoon.booking_core.apiPayload.ApiResponse;
import io.jiyoon.booking_core.apiPayload.code.ResponseCode;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. @RequestParam, @PathVariable Validation 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (existing, replacement) -> existing
                ));

        logValidationError("ConstraintViolation", errors);

        return error(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    // 2. @RequestBody Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        logValidationError("MethodArgumentNotValid", errors);

        return error(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    // 3. JSON Parsing / Enum Parsing 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {

        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType().isEnum()) {

            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "unknown"
                    : invalidFormatException.getPath().get(0).getFieldName();

            String invalidValue = String.valueOf(
                    invalidFormatException.getValue()
            );

            log.warn(
                    "Invalid enum value '{}' for field '{}'",
                    invalidValue,
                    fieldName
            );

            return error(
                    ErrorStatus.COMMON_BAD_REQUEST,
                    String.format(
                            "Invalid value '%s' for field '%s'",
                            invalidValue,
                            fieldName
                    )
            );
        }

        return error(
                ErrorStatus.COMMON_BAD_REQUEST,
                "Invalid request format"
        );
    }

    // 4. 필수 Request Parameter 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex
    ) {

        return error(
                ErrorStatus.COMMON_BAD_REQUEST,
                "Required parameter is missing: " + ex.getParameterName()
        );
    }

    // 5. Request Parameter 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {

        return error(
                ErrorStatus.COMMON_BAD_REQUEST,
                String.format(
                        "Parameter '%s' should be of type '%s'",
                        ex.getName(),
                        ex.getRequiredType() != null
                                ? ex.getRequiredType().getSimpleName()
                                : "unknown"
                )
        );
    }

    // 6. 지원하지 않는 HTTP Method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {

        return error(
                ErrorStatus.COMMON_METHOD_NOT_ALLOWED,
                "HTTP method '" + ex.getMethod() + "' is not supported"
        );
    }

    // 7. Custom Exception
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(
            CustomException ex
    ) {

        log.error(
                "Custom Error: {} - Status: {}",
                ex.getMessage(),
                ex.getErrorCode().getHttpStatus()
        );

        return error(ex.getErrorCode(), ex.getMessage());
    }

    // 8. 예상하지 못한 서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(
            Exception ex
    ) {

        log.error("Unexpected Error: ", ex);

        return error(
                ErrorStatus.COMMON_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private void logValidationError(
            String type,
            Map<String, String> errors
    ) {
        log.warn("{} Validation Error: {}", type, errors);
    }

    private ResponseEntity<ApiResponse<Object>> error(
            ResponseCode status,
            Object data
    ) {

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ApiResponse.failure(status, data));
    }
}