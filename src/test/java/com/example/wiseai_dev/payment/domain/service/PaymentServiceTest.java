import com.example.wiseai_dev.WiseaiDevApplication;
import com.example.wiseai_dev.payment.application.service.PaymentService;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

// PaymentService에 필요한 Payment 엔티티 클래스 및 상태
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;

@SpringBootTest(classes = WiseaiDevApplication.class)
@ActiveProfiles("test")
class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private PaymentJpaRepository paymentJpaRepository;

    private Long reservationId;

    @BeforeEach
    void setup() {
        // 테스트용 예약 데이터 생성 및 저장
        Reservation newReservation = Reservation.builder()
                .meetingRoomId(1L)
                .bookerName("tester")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .totalAmount(100.0)
                .status(ReservationStatus.PENDING_PAYMENT)
                .version(0L)
                .build();
        Reservation savedReservation = reservationRepository.save(newReservation);
        this.reservationId = savedReservation.getId();

        // 🚨 새로 추가된 부분: Mock 객체 동작 정의 🚨
        // `PaymentService`가 `Payment` 엔티티를 조회할 때 유효한 객체를 반환하도록 설정
        Payment mockPayment = Payment.builder()
                .id(1L)
                .reservationId(this.reservationId)
                .provider("TEST_PROVIDER")
                .status(PaymentStatus.CREATED)
                .build();

        // `paymentJpaRepository.findByReservationId`가 호출될 때 Mock 객체를 반환하도록 설정
        when(paymentJpaRepository.findByReservationId(this.reservationId))
                .thenReturn(Optional.of(mockPayment));

        // `paymentJpaRepository.save`가 호출될 때 Mock 객체를 반환하도록 설정
        when(paymentJpaRepository.save(any(Payment.class))).thenReturn(mockPayment);
    }

    @Test
    void 낙관적_락_테스트_동시성_예외_발생() throws InterruptedException {
        // 1. 동시성 테스트를 위한 도구 준비
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // TransactionTemplate을 사용하여 각 스레드가 별도의 트랜잭션에서 실행되도록 합니다.
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // 2. 두 개의 스레드에서 동시에 결제 처리
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                transactionTemplate.execute(status -> {
                    try {
                        startLatch.await();

                        paymentService.processReservationPayment(reservationId);

                        successCount.incrementAndGet();
                    } catch (ObjectOptimisticLockingFailureException e) {
                        failureCount.incrementAndGet();
                        status.setRollbackOnly();
                    } catch (Exception e) {
                        System.err.println("Unexpected exception: " + e.getMessage());
                        status.setRollbackOnly();
                    } finally {
                        endLatch.countDown();
                    }
                    return null;
                });
            });
        }

        // 모든 스레드에게 동시에 시작하라는 신호를 보냄
        startLatch.countDown();

        // 모든 스레드가 종료될 때까지 대기
        endLatch.await();
        executorService.shutdown();

        // 3. 테스트 결과 검증
        // 낙관적 락 충돌 시나리오에서는 한 스레드는 성공하고 다른 스레드는 실패해야 합니다.
        assertEquals(1, successCount.get(), "하나의 트랜잭션만 성공해야 합니다.");
        assertEquals(1, failureCount.get(), "하나의 트랜잭션은 낙관적 락 충돌로 실패해야 합니다.");

        // 최종적으로 DB에 저장된 Reservation 엔티티의 상태와 버전을 확인
        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertEquals(ReservationStatus.CONFIRMED, finalReservation.getStatus(), "최종 상태는 CONFIRMED여야 합니다.");
        assertEquals(1, finalReservation.getVersion(), "최종 버전은 1로 증가해야 합니다.");
    }
}