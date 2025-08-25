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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final String paymentProviderName = "Card";

    @BeforeEach
    void 테스트_데이터_세팅() {
        // 이전 테스트 데이터 정리
        paymentJpaRepository.deleteAll();
        reservationRepository.deleteAll();

        // 예약 데이터 초기화
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

        Reservation savedReservation = reservationRepository.save(newReservation);
        this.reservationId = savedReservation.getId();
    }

    @Test
    @DisplayName("동일 예약 동시 결제 시 하나만 성공하고 나머지는 실패한다")
    void 동일_예약_동시_결제_하나만_성공() throws InterruptedException {
        // Given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When - 여러 스레드가 동시에 결제 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작
                    paymentService.processReservationPayment(reservationId, paymentProviderName);
                    successCount.incrementAndGet();
                } catch (IllegalStateException | org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("예외 발생: " + e.getClass().getName() + " - " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 모든 스레드 시작
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // Then
        assertThat(successCount.get())
                .as("성공 횟수는 반드시 1이어야 한다")
                .isEqualTo(1);

        assertThat(failureCount.get())
                .as("실패 횟수는 나머지 스레드 수와 같아야 한다")
                .isEqualTo(threadCount - 1);

        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
