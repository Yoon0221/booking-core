package io.jiyoon.booking_core.api.booking.service;

import io.jiyoon.booking_core.api.booking.dto.BookingResponse;
import io.jiyoon.booking_core.api.booking.dto.ReserveResult;
import io.jiyoon.booking_core.apiPayload.code.exception.CustomException;
import io.jiyoon.booking_core.apiPayload.status.ErrorStatus;
import io.jiyoon.booking_core.domain.booking.converter.BookingConverter;
import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.entity.BookingStatus;
import io.jiyoon.booking_core.domain.booking.repository.BookingRepository;
import io.jiyoon.booking_core.domain.product.entity.Product;
import io.jiyoon.booking_core.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TicketingRedisService ticketingRedisService;
    private final ProductRepository productRepository;
    private final BookingRepository bookingRepository;
    private final BookingConverter bookingConverter;

    @Override
    @Transactional
    public BookingResponse reserve(Long productId, Long userId) {

        // Redis 선점 시도
        ReserveResult result = ticketingRedisService.tryReserve(productId, userId);

        return switch (result.status()) {

            case SUCCESS -> {
                // 상품 조회
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.PRODUCT_NOT_FOUND));

                // Booking 엔티티 생성, 저장 (INIT 상태)
                Booking booking = bookingConverter.toBooking(userId, product);
                bookingRepository.save(booking);

                // 응답 반환
                yield BookingResponse.builder()
                        .bookingId(booking.getId())
                        .bookingStatus(BookingStatus.INIT)
                        .remainMillis(result.remainMillis())
                        .build();
            }

            case ALREADY_RESERVED -> throw new CustomException(ErrorStatus.BOOKING_ALREADY_RESERVED);
            case ALREADY_PAID -> throw new CustomException(ErrorStatus.PAYMENT_ALREADY);
            case SOLD_OUT -> throw new CustomException(ErrorStatus.PRODUCT_SOLD_OUT);

            default -> throw new CustomException(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR);
        };
    }

}