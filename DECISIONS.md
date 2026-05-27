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


## 보상 트랜잭션 처리 전략
<details>

### 상황
예약/결제는 아래와 같이 단계적으로 처리되도록 설계했습니다.
1. 예약 선점
2. 결제 수단 등록 
3. 외부 PG 승인(CARD/YPAY)
4. 최종 예약 확정 및 포인트 차감
이 과정에서 외부 PG 승인 이후,
최종 예약 확정 단계(DB 재고 차감, Redis 상태 변경 등)에서 예외가 발생할 수 있습니다.
이 경우 이미 승인된 외부 결제를 취소하고,
예약 및 결제 상태를 실패 상태로 복구하는 보상 처리가 필요했습니다.

### 선택
TransactionalEventListener(phase = AFTER_ROLLBACK) 기반의 보상 트랜잭션 구조를 적용했습니다.
- 메인 예약 확정 트랜잭션에서 예외 발생 시 BookingFailedEvent 발행
- 메인 트랜잭션 rollback 완료 이후 AFTER_ROLLBACK 리스너 실행
- 리스너는 REQUIRES_NEW 트랜잭션으로 분리
- 승인 완료된 외부 PG 결제에 대해 cancel 수행
- Booking / Payment 상태를 FAILED 또는 CANCELLED 로 복구
- Redis 완료 상태 제거 

### 이유
외부 PG 취소 요청까지 메인 트랜잭션 내부에서 직접 수행할 경우,
외부 API 지연으로 인해 DB 트랜잭션 점유 시간이 길어질 수 있습니다.

따라서 rollback 이후 별도 이벤트 흐름에서 보상 로직을 수행하도록 분리하여
메인 트랜잭션을 최대한 짧게 유지하도록 설계했습니다.

또한 AFTER_ROLLBACK 단계에서만 보상 처리를 수행함으로써,
실제로 예약 확정이 실패한 경우에만 외부 결제 취소가 실행되도록 보장했습니다.

이를 통해 다음 효과를 기대했습니다.

- 예약 상태와 외부 결제 상태 간 정합성 확보
- 예외 발생 시 자동 복구
- 외부 API 호출과 메인 트랜잭션 분리
- 트랜잭션 유지 시간 최소화
- 장애 상황에서의 안정성 향상

</details>


## 결제 수단 확장 전략
<details>

### 상황
예약 결제는 다음과 같은 복합 결제를 지원하도록 설계했습니다.
1. CARD
2. YPAY
3. POINT
4. POINT + 외부 결제 조합

결제 수단이 증가할수록 서비스 내부에
if-else 또는 switch 기반 분기 로직이 계속 추가되는 문제가 발생할 수 있습니다.
특히 PG사별 승인/취소 방식이 달라질 경우,
서비스 레이어가 각 구현 세부사항을 직접 알게 되어 결합도가 높아집니다.

### 선택
Map 기반 전략 패턴(Dynamic Client Mapping)을 적용했습니다.
- PgPaymentClient 인터페이스 정의
- 결제 수단별 PG 구현체 분리
- PaymentMethod 기준으로 런타임 동적 조회 수행

### 이유
새로운 결제 수단이 추가되더라도
기존 서비스 로직 수정 없이 구현체만 추가하면 되도록 설계했습니다.

서비스 레이어는 구체적인 PG 연동 방식 대신
approve(), cancel() 같은 공통 인터페이스만 호출하므로
비즈니스 흐름과 외부 연동 로직을 분리할 수 있습니다.

또한 승인/취소 정책이 결제 수단별로 달라지더라도
각 구현체 내부로 캡슐화할 수 있어 유지보수성이 향상됩니다.

이를 통해 다음 효과를 기대했습니다.

- 결제 수단 확장 용이
- OCP(Open-Closed Principle) 준수
- PG 연동 로직 분리
- 서비스 레이어 단순화
- 유지보수성 및 테스트 용이성 향상

</details>


## TPS 급증 대응 전략
<details>

### 상황
프로모션 시작 시각(00:00)에는 짧은 시간 동안 대량의 예약 요청이 동시에 유입될 것으로 예상되었습니다.
- 평시 트래픽 : 약 50 TPS
- 프로모션 시작 직후 : 약 500~1000 TPS 예상
- 상품 재고 : 10개 수준의 극소량 한정 판매

이 경우 모든 요청이 DB까지 직접 진입하면 아래 문제가 발생할 수 있습니다.
- DB Row Lock 경합 증가
- Connection Pool 고갈
- 트랜잭션 대기 증가
- 재고 정합성 깨짐 위험
- DB 과부하로 인한 전체 서비스 장애 가능성

특히 순간적인 동시 요청 환경에서는
"실패할 요청"까지 모두 DB에 진입하는 구조가 가장 큰 병목이 된다고 판단했습니다.

### 선택
Redis + Lua Script 기반의 선착순 게이트(Gate) 구조를 적용했습니다.

```text
1000 TPS
Client ───────────────► Redis (Lua Gate)
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
         REJECT (SOLD OUT)        SUCCESS (10~50 TPS)
                                          │
                                          ▼
                                        DB write
```

예약 요청은 우선 Redis Lua Script를 통해 처리되며,
실제로 예약 가능한 요청만 DB까지 진입하도록 설계했습니다.

Lua Script 내부에서 아래 로직을 원자적으로 수행합니다.

- 만료된 선점 슬롯 제거
- 이미 결제 완료된 사용자 검증
- 중복 선점 여부 검증
- 현재 선점/결제 완료 인원 수 계산
- 재고 초과 여부 검증
- 선점 성공 시 TTL 기반 예약 슬롯 등록 

```lua
local pendingCount = redis.call('ZCARD', pendingKey)
local completedCount = redis.call('SCARD', completedKey)

if (pendingCount + completedCount) >= limit then
    return {-3, 0} -- SOLD_OUT
end
```

선점 성공 요청만 이후 DB Booking 생성 단계로 진입합니다.

### 이유
Redis는 단일 스레드 기반으로 명령을 직렬 처리하므로,
Lua Script 내부 로직 전체를 원자적으로 실행할 수 있습니다.

이를 통해 DB 레벨의 비관적 락이나 복잡한 분산 락 없이도
재고 초과를 방지할 수 있도록 설계했습니다.

또한 대부분의 실패 요청을 Redis 레벨에서 즉시 차단함으로써,
DB로 전달되는 실제 쓰기 요청 수를 극단적으로 줄일 수 있었습니다.

예를 들어 1000 TPS 상황에서도, 아래 구조로 동작하도록 설계했습니다.
- 대부분 요청 → Redis 에서 즉시 탈락
- 실제 성공 후보 요청만 → DB 진입

결과적으로 아래 효과를 기대했습니다.

- DB Connection Pool 보호
- Row Lock 경합 감소
- 순간 트래픽 흡수
- 재고 정합성 유지
- 빠른 실패(Fail Fast) 처리
- 애플리케이션 전체 안정성 확보

</details>


## 공정성(Fairness) 구조 검토
<details>

### 상황
초기에는 완전한 선착순 보장을 위해 아래와 같은 
Queue + Worker 기반 FIFO 구조를 고려했습니다.
```text
Client Request
      │
      ▼
Redis Queue (FIFO)
      │
      ▼
Worker
      │
      ▼
Redis Lua Gate
      │
      ▼
DB Write
```
해당 방식은 요청 순서 기반 처리로 높은 공정성을 제공할 수 있습니다.

### 선택
최종적으로는 Queue + Worker 구조를 도입하지 않고,
기존 Redis Lua 기반 즉시 처리 구조를 유지했습니다.
```text
Client Request
      │
      ▼
Redis Lua Gate
      │
 ┌────┴────┐
 ▼         ▼
FAIL     SUCCESS
            │
            ▼
         DB Write
```

### 이유
완전 FIFO를 보장하려면 사실상 단일 Worker 기반 직렬 처리가 필요합니다.
하지만 이 경우,
Worker 자체가 병목 지점이 될 수 있고
Queue 적체 가능성이 증가하며
순간적인 500~1000 TPS Burst Traffic 대응력이 낮아질 수 있다고 판단했습니다.

또한 Worker를 여러 대로 확장하면 처리 순서가 달라질 수 있어,
오히려 완전한 FIFO 보장이 어려워질 수 있습니다.

반면 Redis는 단일 스레드 기반으로 명령을 직렬 처리하며,
Lua Script 역시 원자적으로 실행됩니다.

따라서 완전 FIFO 수준은 아니더라도
- 재고 정합성 보장
- Race Condition 제거
- 빠른 실패 처리(Fail Fast)
- DB 부하 감소
측면에서는 충분히 합리적인 구조라고 판단했습니다.

이번 과제에서는 절대적 FIFO 공정성보다,
시스템 안정성과 고부하 대응을 우선 목표로 선택했습니다.

</details>