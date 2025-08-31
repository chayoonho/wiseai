package com.example.wiseai_dev.payment.domain.service;

import com.example.wiseai_dev.WiseaiDevApplication;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WiseaiDevApplication.class)
@ActiveProfiles("test")
class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    private Long reservationId;
    private static final String PAYMENT_PROVIDER = "Card";

    @BeforeEach
    void 테스트_데이터_준비() {
        // 테스트 데이터 정리
        paymentJpaRepository.deleteAll();
        reservationRepository.deleteAll();

        // 예약 데이터 생성
        Reservation newReservation = new Reservation(
                null,
                "RES-1",
                3L,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "tester",
                ReservationStatus.PENDING_PAYMENT,
                100.0,
                0L
        );

        this.reservationId = reservationRepository.save(newReservation).getId();
    }

    @Test
    @DisplayName("동시에 여러 결제 요청이 들어오면 하나만 성공하고 나머지는 실패한다")
    void 동시에_여러_결제_요청시_하나만_성공한다() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when - 여러 스레드가 동시에 결제 요청
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown(); // 준비 완료
                try {
                    startLatch.await(); // 동시에 시작
                    paymentService.processReservationPayment(reservationId, PAYMENT_PROVIDER);
                    successCount.incrementAndGet();
                } catch (IllegalStateException | org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    failureCount.incrementAndGet(); // 결제 실패 (동시성 충돌)
                } catch (Exception e) {
                    System.err.printf("예외 발생: %s - %s%n", e.getClass().getName(), e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드 준비될 때까지 대기
        readyLatch.await();
        // 동시에 시작
        startLatch.countDown();
        // 모든 스레드 종료 대기
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get())
                .as("결제 성공은 정확히 1건이어야 한다")
                .isEqualTo(1);

        assertThat(failureCount.get())
                .as("결제 실패는 나머지 스레드 수와 같아야 한다")
                .isEqualTo(threadCount - 1);

        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus())
                .as("최종 예약 상태는 CONFIRMED 이어야 한다")
                .isEqualTo(ReservationStatus.CONFIRMED);
    }
}
