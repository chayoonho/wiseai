package com.example.wiseai_dev.payment.domain.service;

import com.example.wiseai_dev.WiseaiDevApplication;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import com.example.wiseai_dev.user.infrastructure.persistence.entity.UserEntity;
import com.example.wiseai_dev.user.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = WiseaiDevApplication.class)
@ActiveProfiles("test")
@Slf4j
class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long reservationId;
    private final String paymentProviderName = "Card";

    @BeforeEach
    void setUp() {
        // TransactionTemplate을 사용해서 초기 데이터 설정
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        this.reservationId = transactionTemplate.execute(status -> {
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

            log.info("테스트 셋업 완료. Reservation ID: {}, Status: {}, Version: {}",
                    saved.getId(), saved.getStatus(), saved.getVersion());

            return saved.getId();
        });
    }

    @Test
    @DisplayName("애플리케이션 컨텍스트 로드 테스트")
    void contextLoads() {
        assertThat(paymentService).isNotNull();
        assertThat(reservationRepository).isNotNull();
        assertThat(paymentJpaRepository).isNotNull();
        log.info("Context loaded successfully");
    }

    @Test
    @DisplayName("기본 결제 처리 테스트")
    void 기본_결제_처리_테스트() {
        log.info("=== 기본 결제 처리 테스트 시작 ===");

        // 결제 처리
        paymentService.processReservationPayment(reservationId, paymentProviderName);

        // 결과 검증
        Reservation updatedReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        log.info("기본 결제 처리 완료. 최종 상태: {}, 버전: {}",
                updatedReservation.getStatus(), updatedReservation.getVersion());
    }

    @Test
    @DisplayName("낙관적 락 동시성 테스트 - 동일 예약 동시 결제")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 낙관적_락_동시성_테스트() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(threadCount); // 동시 시작을 위한 래치
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockFailureCount = new AtomicInteger(0);
        AtomicInteger duplicatePaymentCount = new AtomicInteger(0);
        AtomicInteger otherFailureCount = new AtomicInteger(0);

        log.info("=== 낙관적 락 동시성 테스트 시작 ===");
        log.info("스레드 수: {}, 예약 ID: {}", threadCount, reservationId);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                // 각 스레드는 별도의 트랜잭션에서 실행
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                try {
                    startLatch.countDown();
                    startLatch.await(); // 모든 스레드가 동시에 시작되도록 대기

                    log.info("[스레드 {}] 결제 시도 시작", threadIndex);

                    // 약간의 랜덤 지연으로 실제 동시성 상황 시뮬레이션
                    Thread.sleep((long) (Math.random() * 50));

                    transactionTemplate.execute(status -> {
                        paymentService.processReservationPayment(reservationId, paymentProviderName);
                        return null;
                    });

                    successCount.incrementAndGet();
                    log.info("[스레드 {}] ✅ 결제 성공!", threadIndex);

                } catch (Exception e) {
                    log.error("[스레드 {}] ❌ 결제 실패: {}", threadIndex, e.getMessage());

                    // 예외 타입별로 세분화하여 분류
                    String errorMessage = e.getMessage();
                    Throwable rootCause = getRootCause(e);

                    if (errorMessage.contains("동시 결제 요청이 감지") ||
                            rootCause instanceof ObjectOptimisticLockingFailureException) {
                        optimisticLockFailureCount.incrementAndGet();
                        log.info("[스레드 {}] 🔒 낙관적 락 충돌 감지", threadIndex);

                    } else if (errorMessage.contains("이미 결제 정보가 존재") ||
                            errorMessage.contains("이미 처리된 결제")) {
                        duplicatePaymentCount.incrementAndGet();
                        log.info("[스레드 {}] 🚫 중복 결제 방지", threadIndex);

                    } else {
                        otherFailureCount.incrementAndGet();
                        log.warn("[스레드 {}] ⚠️ 기타 오류: {}", threadIndex, errorMessage);
                    }
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기 (최대 30초)
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // 결과 출력
        log.info("=== 동시성 테스트 결과 ===");
        log.info("✅ 성공: {}", successCount.get());
        log.info("🔒 낙관적 락 충돌: {}", optimisticLockFailureCount.get());
        log.info("🚫 중복 결제 방지: {}", duplicatePaymentCount.get());
        log.info("⚠️ 기타 실패: {}", otherFailureCount.get());
        log.info("완료 여부: {}", completed);

        if (!completed) {
            log.error("❌ 테스트 타임아웃 발생!");
        }

        // 검증
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1); // 정확히 1개만 성공해야 함
        assertThat(successCount.get() + optimisticLockFailureCount.get() +
                duplicatePaymentCount.get() + otherFailureCount.get()).isEqualTo(threadCount);

        // 최종 예약 상태 확인
        Reservation finalReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        log.info("최종 예약 상태: {}, 최종 버전: {}",
                finalReservation.getStatus(), finalReservation.getVersion());

        // Payment 개수 확인 (정확히 1개만 생성되어야 함)
        long paymentCount = paymentJpaRepository.count();
        assertThat(paymentCount).isEqualTo(1);
        log.info("생성된 결제 건수: {}", paymentCount);
    }

    @Test
    @DisplayName("수동 낙관적 락 시나리오 테스트")
    void 수동_낙관적_락_시나리오_테스트() {
        log.info("=== 수동 낙관적 락 시나리오 테스트 시작 ===");

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 1. 두 개의 서로 다른 트랜잭션에서 동일한 예약을 조회
        Reservation reservation1 = transactionTemplate.execute(status -> {
            Reservation r = reservationRepository.findById(reservationId).orElseThrow();
            log.info("트랜잭션1 - 예약 조회: ID={}, Version={}, Status={}",
                    r.getId(), r.getVersion(), r.getStatus());
            return r;
        });

        Reservation reservation2 = transactionTemplate.execute(status -> {
            Reservation r = reservationRepository.findById(reservationId).orElseThrow();
            log.info("트랜잭션2 - 예약 조회: ID={}, Version={}, Status={}",
                    r.getId(), r.getVersion(), r.getStatus());
            return r;
        });

        // 두 예약 객체는 동일한 버전을 가져야 함
        assertThat(reservation1.getVersion()).isEqualTo(reservation2.getVersion());
        log.info("✅ 두 트랜잭션 모두 동일한 버전({})을 조회함", reservation1.getVersion());

        // 2. 첫 번째 트랜잭션에서 상태 변경 및 저장
        transactionTemplate.execute(status -> {
            log.info("트랜잭션1 - 상태 변경 시도: {} -> {}",
                    reservation1.getStatus(), ReservationStatus.CONFIRMED);
            reservation1.setStatus(ReservationStatus.CONFIRMED);
            Reservation saved = reservationRepository.save(reservation1);
            log.info("트랜잭션1 - 저장 성공: 새 버전={}", saved.getVersion());
            return null;
        });

        // 3. 두 번째 트랜잭션에서 상태 변경 시도 (낙관적 락 충돌 발생해야 함)
        log.info("트랜잭션2 - 충돌 상황 시뮬레이션 시작");

        assertThatThrownBy(() ->
                transactionTemplate.execute(status -> {
                    log.info("트랜잭션2 - 상태 변경 시도: {} -> {} (구 버전: {})",
                            reservation2.getStatus(), ReservationStatus.CANCELLED, reservation2.getVersion());
                    reservation2.setStatus(ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation2); // 여기서 OptimisticLockingFailureException 발생
                    return null;
                })
        ).hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);

        log.info("✅ 낙관적 락 충돌이 정상적으로 감지됨");

        // 4. 최종 상태 확인
        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        log.info("최종 확인 - 상태: {}, 버전: {}", finalReservation.getStatus(), finalReservation.getVersion());
    }

    @Test
    @DisplayName("재시도 로직 동작 테스트")
    void 재시도_로직_테스트() throws InterruptedException {
        log.info("=== 재시도 로직 동작 테스트 시작 ===");

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await(); // 동시 시작

                    log.info("[재시도 테스트 스레드 {}] 시작", threadIndex);

                    // PaymentService의 @Retryable 어노테이션이 동작하도록 호출
                    paymentService.processReservationPayment(reservationId, paymentProviderName);
                    successCount.incrementAndGet();
                    log.info("[재시도 테스트 스레드 {}] 성공", threadIndex);

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.info("[재시도 테스트 스레드 {}] 실패: {}", threadIndex, e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        log.info("재시도 테스트 완료 - 성공: {}, 실패: {}, 완료: {}",
                successCount.get(), failureCount.get(), completed);

        // 검증: 정확히 1개만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }

    /**
     * 예외의 근본 원인을 찾는 헬퍼 메서드
     */
    private Throwable getRootCause(Throwable throwable) {
        if (throwable.getCause() != null) {
            return getRootCause(throwable.getCause());
        }
        return throwable;
    }
}