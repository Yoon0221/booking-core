package io.jiyoon.booking_core.api.booking.controller;

import io.jiyoon.booking_core.api.booking.dto.BookingResponse;
import io.jiyoon.booking_core.api.booking.service.BookingService;
import io.jiyoon.booking_core.apiPayload.ApiResponse;
import io.jiyoon.booking_core.apiPayload.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "예약 요청 API",
            description = """
                    상품에 대한 예약을 요청합니다.
                    
                    [테스트 데이터]
                    - productId=1 : 예약 가능 상품
                    - productId=2 : 품절 임박 상품
                    - productId=3 : 예약 불가 상품
                    
                    - userId=1 : 일반 사용자
                    """
    )
    @PostMapping("/reserve")
    public ApiResponse<BookingResponse> reserveSlot(
            @Parameter(description = "상품 ID", example = "1")
            @RequestParam Long productId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId
    ) {
        return ApiResponse.success(SuccessStatus.BOOKING_RESERVE_SUCCESS, bookingService.reserve(productId, userId));
    }

    @Operation(
            summary = "임시 결제 API (무조건 결제 성공)"
    )
    @PostMapping("/pay")
    public ApiResponse<String> processPayment(
            @Parameter(description = "상품 ID", example = "1")
            @RequestParam Long productId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId
    ) {
        return ApiResponse.success(SuccessStatus.BOOKING_PAYMENT_SUCCESS, bookingService.pay(productId, userId));
    }

}