package io.jiyoon.booking_core.api.booking.controller;

import io.jiyoon.booking_core.api.booking.dto.BookingResponse;
import io.jiyoon.booking_core.api.booking.service.BookingService;
import io.jiyoon.booking_core.apiPayload.ApiResponse;
import io.jiyoon.booking_core.apiPayload.status.SuccessStatus;
import io.jiyoon.booking_core.api.booking.dto.PaymentRequestDto;
import io.jiyoon.booking_core.api.booking.service.BookingPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingPayService bookingPayService;

    @Operation(
            summary = "예약 선점 API",
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
            summary = "결제 수단 등록 API",
            description = """
                    예약 건에 대한 결제 수단 및 결제 금액 정보를 등록합니다.
                    
                    [동작 방식]
                    - 예약 상태가 INIT 인 경우에만 등록 가능합니다.
                    - 결제 금액 총합은 예약 금액과 반드시 일치해야 합니다.
                    - CARD + YPAY 동시 사용은 허용되지 않습니다.
                    - POINT + 외부 결제(CARD/YPAY) 복합 결제를 지원합니다.
                    
                    [요청 예시]
                    {
                      "paymentDetails": [
                        {
                          "paymentMethod": "POINT",
                          "amount": 3000
                        },
                        {
                          "paymentMethod": "CARD",
                          "amount": 7000
                        }
                      ]
                    }
                    """
    )
    @PostMapping("/{bookingId}/payments/register")
    public ApiResponse<Void> registerPaymentMethods(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId,

            @RequestBody PaymentRequestDto.RegisterMethods request
    ) {
        bookingPayService.registerPaymentMethods(bookingId, userId, request);
        return ApiResponse.success(SuccessStatus.BOOKING_PAYMENT_SUCCESS);
    }

    @Operation(
            summary = "외부 PG 결제 승인 API",
            description = """
                    외부 결제 수단(CARD 또는 YPAY)에 대한 승인 요청을 처리합니다.
                    
                    [동작 방식]
                    - READY 상태의 외부 결제 건만 승인 가능합니다.
                    - 결제 수단별 PgPaymentClient 를 통해 외부 PG 승인 요청을 수행합니다.
                    - 승인 성공 시 Payment 상태는 SUCCESS 로 변경됩니다.
                    - 승인 실패 시 Payment 상태는 FAILED 로 변경됩니다.
                    
                    [요청 예시]
                    {
                      "paymentMethod": "CARD"
                    }
                    """
    )
    @PostMapping("/{bookingId}/payments/pg-approve")
    public ApiResponse<Void> processPgPayment(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId,

            @RequestBody PaymentRequestDto.PgApprove request
    ) {
        bookingPayService.processPgPayment(bookingId, userId, request);
        return ApiResponse.success(SuccessStatus.BOOKING_PAYMENT_SUCCESS);
    }

    @Operation(
            summary = "최종 예약 확정 API",
            description = """
                    예약 확정 및 내부 포인트 결제를 최종 처리합니다.
                    
                    [동작 방식]
                    - 외부 PG 결제 승인 완료 여부를 검증합니다.
                    - POINT 결제 건이 존재할 경우 사용자 포인트를 차감합니다.
                    - Redis 선점 상태를 결제 완료 상태로 반영합니다.
                    - 예약 상태를 CONFIRMED 로 변경하고 상품 재고를 차감합니다.
                    - 처리 실패 시 BookingFailedEvent 를 발행하여 보상 트랜잭션을 수행합니다.
                    """
    )
    @PostMapping("/{bookingId}/complete")
    public ApiResponse<Void> completeBooking(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long bookingId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId
    ) {
        bookingPayService.completeBookingAndPoint(bookingId, userId);
        return ApiResponse.success(SuccessStatus.BOOKING_PAYMENT_SUCCESS);
    }

}