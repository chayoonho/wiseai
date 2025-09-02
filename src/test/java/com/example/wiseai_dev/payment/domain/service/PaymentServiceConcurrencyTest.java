package com.example.wiseai_dev.payment.domain.service;

import com.example.wiseai_dev.WiseaiDevApplication;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import com.example.wiseai_dev.user.infrastructure.persistence.entity.UserEntity;
import com.example.wiseai_dev.user.infrastructure.persistence.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

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
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    private Long reservationId;
    private final String paymentProviderName = "Card";

    @BeforeEach
    @Transactional
    void setUp() {
        // 기존 데이터 정리
        paymentJpaRepository.deleteAll();
        reservationRepository.deleteAll();
        userJpaRepository.deleteAll();

        // 새로운 사용자 생성 및 저장
        UserEntity savedUserEntity = userJpaRepository.save(
                UserEntity.builder()
                        .name("Test User")
                        .email("test@example.com")
                        .build()
        );

        // 새로운 예약 생성 및 저장
        Reservation newReservation = Reservation.create(
                3L, // meetingRoomId
                LocalDateTime.now(), // startTime
                LocalDateTime.now().plusHours(1), // endTime
                savedUserEntity.toDomainModel(), // user
                100.0, // totalAmount
                ReservationStatus.PENDING_PAYMENT // status
        );

        Reservation saved = reservationRepository.save(newReservation);

        // Lazy 로딩된 user 강제 초기화
        saved.getUser().getName();

        // DB에 반영 (트랜잭션 안에서 flush)
        reservationRepository.flush();

        this.reservationId = saved.getId();

        // 로그 출력
        System.out.println("Test setup completed. Reservation ID: " + reservationId);
        System.out.println("Reservation status: " + saved.getStatus());
    }

    @Test
    @DisplayName("애플리케이션 컨텍스트 로드 테스트")
    void contextLoads() {
        assertThat(paymentService).isNotNull();
        assertThat(reservationRepository).isNotNull();
        assertThat(paymentJpaRepository).isNotNull();
        System.out.println("Context loaded successfully");
    }

    @Test
    @DisplayName("기본 결제 처리 테스트")
    void 기본_결제_처리_테스트() {
        paymentService.processReservationPayment(reservationId, paymentProviderName);

        Reservation updatedReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        System.out.println("Basic payment test completed successfully");
    }

    @Test
    @DisplayName("동일 예약 동시 결제 시 하나만 성공하고 나머지는 실패한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 동시성 테스트에서는 트랜잭션 끊기
    void 동시성_결제_테스트() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        System.out.println("Starting concurrency test with " + threadCount + " threads");

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    System.out.println("Thread " + threadIndex + " attempting payment");
                    paymentService.processReservationPayment(reservationId, paymentProviderName);
                    successCount.incrementAndGet();
                    System.out.println("Thread " + threadIndex + " succeeded");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("Thread " + threadIndex + " failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기 (최대 30초)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        if (!completed) {
            System.err.println("Test timed out!");
        }

        System.out.println("Concurrency test completed - Success: " + successCount.get() +
                ", Failure: " + failureCount.get());

        // 결과 검증
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);

        Reservation finalReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
