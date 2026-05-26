local pendingKey = KEYS[1]
local completedKey = KEYS[2]
local userId = ARGV[1]
local limit = tonumber(ARGV[2])
local ttlMillis = tonumber(ARGV[3])

-- Redis 내부 서버 시간 구하기 (밀리초 변환)
local redisTime = redis.call('TIME')
local now = (tonumber(redisTime[1]) * 1000) + math.floor(tonumber(redisTime[2]) / 1000)

-- 1. 만료된 슬롯 자동 제거
redis.call('ZREMRANGEBYSCORE', pendingKey, '-inf', now)

-- 2. 이미 결제 완료된 유저 검증
if redis.call('SISMEMBER', completedKey, userId) == 1 then
    return {-2, 0} -- ALREADY_PAID
end

-- 3. 이미 예약 선점 중인 유저라면 남은 시간 계산해서 반환
local existingExpire = redis.call('ZSCORE', pendingKey, userId)
if existingExpire then
    local remain = tonumber(existingExpire) - now
    return {-1, remain} -- ALREADY_RESERVED
end

-- 4. 10명 제한 인원 검증
local pendingCount = redis.call('ZCARD', pendingKey)
local completedCount = redis.call('SCARD', completedKey)

if (pendingCount + completedCount) >= limit then
    return {-3, 0} -- SOLD_OUT
end

-- 5. 선점 성공
local expireAt = now + ttlMillis
redis.call('ZADD', pendingKey, expireAt, userId)
return {0, ttlMillis} -- SUCCESS