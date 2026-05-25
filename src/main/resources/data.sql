INSERT INTO product (
    id,
    name,
    price,
    remaining_stock,
    sale_start_at,
    check_in_at,
    check_out_at,
    status,
    version,
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
    0,
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
    0,
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
    0,
    NOW(),
    NOW()
);

INSERT INTO user_point (
    user_id,
    balance,
    version,
    created_at,
    updated_at
)
VALUES
    (
        1,
        500000.00,
        0,
        NOW(),
        NOW()
    );