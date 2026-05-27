package io.jiyoon.booking_core.api.booking.event;

import io.jiyoon.booking_core.api.booking.service.TicketingRedisService;
import io.jiyoon.booking_core.domain.booking.entity.Booking;
import io.jiyoon.booking_core.domain.booking.repository.BookingRepository;
import io.jiyoon.booking_core.domain.payment.entity.Payment;
import io.jiyoon.booking_core.domain.payment.entity.PaymentMethod;
import io.jiyoon.booking_core.domain.payment.entity.PaymentStatus;
import io.jiyoon.booking_core.pg.PgPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final BookingRepository bookingRepository;
    private final Map<PaymentMethod, PgPaymentClient> pgPaymentClients;
    private final TicketingRedisService ticketingRedisService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleBookingFailed(BookingFailedEvent event) {
        log.info("보상 이벤트 리스너 실행: Booking ID: {}", event.bookingId());

        Booking booking = bookingRepository.findById(event.bookingId()).orElse(null);
        if (booking == null) return;

        // 1. 상태 변경 (Booking & Payments)
        booking.fail();

        for (Payment payment : booking.getPayments()) {
            // A. 외부 결제 취소
            if (payment.getStatus() == PaymentStatus.SUCCESS &&
                    (payment.getPaymentMethod() == PaymentMethod.CARD || payment.getPaymentMethod() == PaymentMethod.YPAY)) {
                try {
                    PgPaymentClient client = pgPaymentClients.get(payment.getPaymentMethod());
                    if (client != null) {
                        client.cancel(payment.getId(), payment.getAmount());
                        payment.cancel();
                    }
                } catch (Exception e) {
                    log.error("PG 취소 실패, 수동 확인 필요: {}", payment.getId(), e);
                }
            }

            // B. 포인트 결제 상태를 CANCELLED 로 정정
            if (payment.getPaymentMethod() == PaymentMethod.POINT && payment.getStatus() == PaymentStatus.READY) {
                payment.cancel();
            }
        }

        // 2. Redis 복구
        try {
            ticketingRedisService.removeCompleted(booking.getProduct().getId(), event.userId());
            log.info("Redis 복구 완료: User ID: {}", event.userId());
        } catch (Exception e) {
            log.error("Redis 복구 실패: {}", event.userId(), e);
        }
    }

}