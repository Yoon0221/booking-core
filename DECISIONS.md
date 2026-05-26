## Checkout API 예약 가능 여부 판단 기준
<details>

### 상황
주문서 진입 시점에도 사용자가 실제 구매 가능한 상품인지 확인할 필요가 있었습니다.

### 선택
Checkout API 에서 다음 기준으로 reservable 여부를 계산하도록 설계했습니다.
- 상품 상태 ACTIVE
- 남은 재고 존재
- 판매 시작 시간 이후

### 이유
단순 상품 조회만 수행할 경우,
00시 이전에도 구매 가능 상품처럼 표시될 수 있어 과제 요구사항(특정 시간 오픈 상품)에 맞지 않았습니다.
때문에, 최종 검증은 Booking API 에서 다시 수행하지만
Checkout 단계에서도 사용자에게 정확한 예약 가능 상태를 보여주기 위해 사전 판단 로직을 포함했습니다.

</details>


## Redis 장애 시 Fallback 전략
<details>

### 상황
Redis는 예약 선점 및 동시성 제어의 핵심 gate 역할을 합니다.  
Redis 장애 시 모든 요청이 DB로 직접 유입될 위험이 있습니다.

### 선택
Redis 장애 시 **degraded mode** 로 운영합니다.
- Reserve API → 즉시 실패 (Fail Fast, 503)
- Payment API → 기존 Booking 기준 제한 운영
- 조회 API → 정상 제공
즉, **신규 예약은 중단하고 후속 기능만 허용**합니다.

### 이유
DB 직접 fallback 시
- burst traffic(500~1000 TPS)이 DB에 집중
- connection pool 고갈 가능
- lock 경쟁 증가
- overselling 위험 발생
따라서 **예약 차단이 시스템 붕괴 및 재고 정합성 훼손보다 안전하다**고 판단했습니다.

특히 이 시스템은 재고 정합성이 핵심인 flash-sale 구조이므로,
일시적인 예약 중단이 잘못된 예약 허용보다 안전하다고 판단했습니다.

따라서 Redis 장애 시에는 DB 직접 fallback 대신
신규 예약은 차단하여 시스템 보호와 재고 정합성을 우선하고,
이미 생성된 예약에 대한 후속 기능만 제한적으로 운영하는 전략을 선택했습니다.

</details>