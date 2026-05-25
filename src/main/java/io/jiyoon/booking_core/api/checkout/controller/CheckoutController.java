package io.jiyoon.booking_core.api.checkout.controller;

import io.jiyoon.booking_core.apiPayload.ApiResponse;
import io.jiyoon.booking_core.apiPayload.status.SuccessStatus;
import io.jiyoon.booking_core.api.checkout.dto.CheckoutResponse;
import io.jiyoon.booking_core.api.checkout.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Operation(
            summary = "주문서 진입 API",
            description = """
                    상품 정보, 사용자 포인트, 예약 가능 여부를 조회합니다.
                    
                    [테스트용 Seed 데이터]
                    - productId=1 : 예약 가능 상품
                    - productId=2 : 판매 시작 전 상품
                    - productId=3 : 품절 상품
                    
                    - userId=1 : 500000 포인트 보유 사용자
                    """
    )
    @GetMapping
    public ApiResponse<CheckoutResponse> getCheckout(
            @Parameter(description = "상품 ID", example = "1")
            @RequestParam Long productId,

            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId
    ) {
        return ApiResponse.success(
                SuccessStatus.CHECKOUT_SUCCESS,
                checkoutService.getCheckout(productId, userId)
        );
    }
}