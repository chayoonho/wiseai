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
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = WiseaiDevApplication.class)
@ActiveProfiles("test")
public class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    // 실제 JPA Repository를 사용하여 실제 DB 접근 테스트
    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    private Long reservationId;
    private final String paymentProviderName = "Card";

    @BeforeEach
    @Transactional
    void setup() {
        // 테스트 전용 예약 데이터 생성 및 초기화
        Reservation newReservation = Reservation.builder()
                .reservationNo("RES-1")
                .meetingRoomId(3L)
                .bookerName("tester")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .totalAmount(100.0)
                .status(ReservationStatus.PENDING_PAYMENT)
                .version(0L)
                .build();
        Reservation savedReservation = reservationRepository.save(newReservation);
        this.reservationId = savedReservation.getId();

        // 이전 테스트의 데이터 잔여를 제거하여 테스트 간 독립성 보장
        paymentJpaRepository.deleteByReservationId(this.reservationId);
    }

    @Test
    @DisplayName("동일한 예약을 여러 스레드가 동시에 결제 시도 시, 중복 결제 실패 테스트")
    void processReservationPayment_concurrentAccess_shouldFailOnDuplicate() throws InterruptedException {
        // 1. 동시성 테스트를 위한 도구 준비
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 2. 여러 스레드에서 동시에 결제 처리
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기

                    paymentService.processReservationPayment(reservationId, paymentProviderName);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    if ("이미 결제 정보가 존재하는 예약입니다.".equals(e.getMessage())) {
                        failureCount.incrementAndGet();
                    } else {
                        // 예상치 못한 예외가 발생하면 실패 처리
                        System.err.println("예상치 못한 IllegalStateException: " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("예외 발생: " + e.getClass().getName() + " - " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 모든 스레드에게 동시에 시작하라는 신호를 보냄
        startLatch.countDown();

        // 모든 스레드가 종료될 때까지 대기
        endLatch.await();
        executorService.shutdown();

        // 3. 테스트 결과 검증
        // 비관적 락으로 인해 첫 번째 스레드가 결제 정보를 저장하면,
        // 나머지 스레드는 중복 체크 로직에 의해 실패해야 함
        assertThat(successCount.get()).as("성공 횟수는 1이어야 합니다.").isEqualTo(1);
        assertThat(failureCount.get()).as("실패 횟수는 총 스레드 수 - 1 이어야 합니다.").isEqualTo(threadCount - 1);

        // 최종적으로 DB에 저장된 Reservation 엔티티의 상태와 버전을 확인
        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}