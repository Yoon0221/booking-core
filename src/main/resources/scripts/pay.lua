local pendingKey = KEYS[1]
local completedKey = KEYS[2]
local userId = ARGV[1]

-- Redis 내부 서버 시간 구하기 (밀리초 변환)
local redisTime = redis.call('TIME')
local now = (tonumber(redisTime[1]) * 1000) + math.floor(tonumber(redisTime[2]) / 1000)

-- 1. 유효한 예약 선점 상태인지 검증
local expireTime = redis.call('ZSCORE', pendingKey, userId)
if not expireTime or tonumber(expireTime) < now then
    return 1 -- EXPIRED_OR_NOT_RESERVED
end

-- 2. 자격이 유효하면 정산 완료 처리
redis.call('ZREM', pendingKey, userId)
redis.call('SADD', completedKey, userId)
return 0 -- SUCCESS