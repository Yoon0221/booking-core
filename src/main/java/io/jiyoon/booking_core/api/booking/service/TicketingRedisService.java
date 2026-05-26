package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.api.booking.dto.ReserveResult;
import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.booking.entity.ReserveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketingRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> reserveScript;
    private final RedisScript<Long> payScript;
    private static final int LIMIT = 10;            // 상품당 최대 예약 가능 수
    private static final long TTL_SECONDS = 300;    // TTL (초 단위: 5분)

    /**
     * 예약 선점 (Redis 기반 원자 처리)
     *
     * @param productId 상품 ID
     * @param userId 사용자 ID
     * @return 예약 결과 상태 + 남은 시간
     */
    public ReserveResult tryReserve(Long productId, Long userId) {
        try {
            // Redis Key 구성
            String pendingKey = buildPendingKey(productId);     // 예약 대기
            String completedKey = buildCompletedKey(productId); // 결제 완료

            // Lua 스크립트 실행 (원자적 처리)
            List<Long> result = redisTemplate.execute(
                    reserveScript,
                    List.of(pendingKey, completedKey),
                    String.valueOf(userId),
                    String.valueOf(LIMIT),
                    String.valueOf(TTL_SECONDS)
            );

            // Lua 실행 실패 또는 반환값 이상
            if (result == null || result.size() < 2) {
                return new ReserveResult(ReserveStatus.ERROR, 0L);
            }

            // Lua 반환값 파싱
            int statusCode = result.get(0).intValue();  // 상태 코드
            long remainMillis = result.get(1);          // 남은 TTL(ms)

            // 상태 코드 → enum 매핑
            return new ReserveResult(mapStatus(statusCode), remainMillis);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.REDIS_UNAVAILABLE);
        }
    }

    /**
     * 결제 처리
     *
     * @param productId 상품 ID
     * @param userId 사용자 ID
     * @return true = 결제 성공, false = 실패
     */
    public boolean tryPay(Long productId, Long userId) {

        String pendingKey = buildPendingKey(productId);
        String completedKey = buildCompletedKey(productId);

        // Lua로 pending → completed 이동 처리
        Long result = redisTemplate.execute(
                payScript,
                List.of(pendingKey, completedKey),
                String.valueOf(userId)
        );

        // 0 = 성공
        return result != null && result == 0L;
    }

    // 예약 대기 키 생성
    private String buildPendingKey(Long productId) {
        return "product:" + productId + ":pending";
    }

    // 결제 완료 키 생성
    private String buildCompletedKey(Long productId) {
        return "product:" + productId + ":completed";
    }

    // Lua 상태코드를 비즈니스 상태로 변환
    private ReserveStatus mapStatus(int code) {
        return switch (code) {
            case 0 -> ReserveStatus.SUCCESS;          // 예약 성공
            case 1 -> ReserveStatus.ALREADY_RESERVED; // 이미 예약됨
            case 2 -> ReserveStatus.SOLD_OUT;         // 매진
            case 3 -> ReserveStatus.ALREADY_PAID;     // 이미 결제됨
            default -> ReserveStatus.ERROR;           // 알 수 없는 오류
        };
    }

}