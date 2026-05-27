-- PRODUCT
INSERT INTO product (
    id,
    name,
    price,
    remaining_stock,
    sale_start_at,
    check_in_at,
    check_out_at,
    status,
    created_at,
    updated_at
)
VALUES

-- 예약 가능 상품
(
    1,
    '제주 신라호텔 디럭스',
    250000.00,
    10,
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 7 DAY,
    NOW() + INTERVAL 8 DAY,
    'ACTIVE',
    NOW(),
    NOW()
),

-- 판매 시작 전 상품
(
    2,
    '부산 오션뷰 스위트',
    180000.00,
    10,
    NOW() + INTERVAL 1 DAY,
    NOW() + INTERVAL 10 DAY,
    NOW() + INTERVAL 11 DAY,
    'ACTIVE',
    NOW(),
    NOW()
),

-- 품절 상품
(
    3,
    '강릉 풀빌라',
    320000.00,
    0,
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 5 DAY,
    NOW() + INTERVAL 6 DAY,
    'SOLD_OUT',
    NOW(),
    NOW()
),

-- 비활성 상품
(
    4,
    '서울 시그니엘 프리미어',
    450000.00,
    5,
    NOW() - INTERVAL 2 DAY,
    NOW() + INTERVAL 14 DAY,
    NOW() + INTERVAL 15 DAY,
    'INACTIVE',
    NOW(),
    NOW()
);

-- USER POINT
INSERT INTO user_point (
    user_id,
    balance,
    created_at,
    updated_at
)
VALUES
    (
        1,
        500000.00,
        NOW(),
        NOW()
    ),
    (
        2,
        100000.00,
        NOW(),
        NOW()
    );

-- BOOKING
INSERT INTO booking (
    id,
    user_id,
    product_id,
    total_amount,
    status,
    created_at,
    updated_at
)
VALUES

-- 결제 대기 예약
(
    1,
    1,
    1,
    250000.00,
    'PAYMENT_PENDING',
    NOW(),
    NOW()
),

-- 예약 완료
(
    2,
    1,
    2,
    180000.00,
    'CONFIRMED',
    NOW(),
    NOW()
),

-- 실패 예약
(
    3,
    2,
    1,
    250000.00,
    'FAILED',
    NOW(),
    NOW()
);

-- PAYMENT
INSERT INTO payment (
    id,
    booking_id,
    payment_method,
    amount,
    status,
    created_at,
    updated_at
)
VALUES

-- 예약 1 : 포인트 일부 결제
(
    1,
    1,
    'POINT',
    50000.00,
    'SUCCESS',
    NOW(),
    NOW()
),

-- 예약 1 : 카드 결제 대기
(
    2,
    1,
    'CARD',
    200000.00,
    'READY',
    NOW(),
    NOW()
),

-- 예약 2 : YPAY 완료
(
    3,
    2,
    'YPAY',
    180000.00,
    'SUCCESS',
    NOW(),
    NOW()
),

-- 예약 3 : 카드 실패
(
    4,
    3,
    'CARD',
    250000.00,
    'FAILED',
    NOW(),
    NOW()
);