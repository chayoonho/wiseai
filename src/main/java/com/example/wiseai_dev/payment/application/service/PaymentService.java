package com.example.wiseai_dev.payment.application.service;

import com.example.wiseai_dev.payment.application.api.dto.PaymentResponse;
import com.example.wiseai_dev.payment.application.api.dto.ProviderPayload;
import com.example.wiseai_dev.payment.domain.gateway.PaymentGateway;
import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import com.example.wiseai_dev.payment.domain.model.PaymentStatus;
import com.example.wiseai_dev.payment.domain.repository.PaymentProviderRepository;
import com.example.wiseai_dev.payment.domain.repository.PaymentRepository;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentGateway> paymentGateways;

    public PaymentService(ReservationRepository reservationRepository,
                          PaymentProviderRepository paymentProviderRepository,
                          PaymentRepository paymentRepository,
                          List<PaymentGateway> gateways) {
        this.reservationRepository = reservationRepository;
        this.paymentProviderRepository = paymentProviderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateways = gateways.stream()
                .collect(Collectors.toMap(
                        gateway -> gateway.getClass().getSimpleName().replace("PaymentGateway", ""),
                        Function.identity()
                ));
    }

    /**
     * 예약 결제 처리 (낙관적 락 적용 + 재시도 로직)
     */
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    @Transactional
    public PaymentResponse processReservationPayment(Long reservationId, String paymentProviderName) {
        try {
            return processPaymentInternal(reservationId, paymentProviderName);

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("결제 처리 중 낙관적 락 충돌 발생. reservationId: {}, provider: {}",
                    reservationId, paymentProviderName);
            throw new IllegalStateException("동시 결제 요청이 감지되었습니다. 잠시 후 다시 시도해주세요.");

        } catch (DataIntegrityViolationException e) {
            log.warn("중복 결제 시도 감지. reservationId: {}", reservationId);
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }
    }

    private PaymentResponse processPaymentInternal(Long reservationId, String paymentProviderName) {
        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 2. 예약 상태 검증
        validateReservationForPayment(reservation);

        // 3. 중복 결제 방지
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            throw new IllegalStateException("이미 결제 정보가 존재하는 예약입니다.");
        }

        // 4. 결제사 및 게이트웨이 확인
        PaymentProvider paymentProvider = getPaymentProvider(paymentProviderName);
        PaymentGateway gateway = getPaymentGateway(paymentProviderName);

        // 5. 거래 ID 생성
        String transactionId = generateTransactionId();

        // 6. 결제 엔티티 생성
        Payment payment = createPayment(reservation, paymentProvider, transactionId);

        try {
            // 7. 외부 PG사 호출
            PaymentResult paymentResult = gateway.processPayment(payment);

            // 8. 결제 및 예약 상태 업데이트
            return updatePaymentAndReservation(payment, reservation, paymentResult);

        } catch (Exception e) {
            // PG사 오류 시 실패 처리
            log.error("PG사 결제 처리 실패. reservationId: {}, error: {}", reservationId, e.getMessage());
            handlePaymentFailure(payment, reservation, e);
            throw new IllegalStateException("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 예약 상태 검증
     */
    private void validateReservationForPayment(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약입니다.");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태가 아닌 예약입니다. 현재 상태: " + reservation.getStatus());
        }
    }

    /**
     * 결제사 조회
     */
    private PaymentProvider getPaymentProvider(String paymentProviderName) {
        return paymentProviderRepository.findByName(paymentProviderName)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제사입니다: " + paymentProviderName));
    }

    /**
     * 게이트웨이 조회
     */
    private PaymentGateway getPaymentGateway(String paymentProviderName) {
        PaymentGateway gateway = paymentGateways.get(paymentProviderName);
        if (gateway == null) {
            throw new IllegalArgumentException("등록되지 않은 결제 게이트웨이: " + paymentProviderName);
        }
        return gateway;
    }

    /**
     * 거래 ID 생성
     */
    private String generateTransactionId() {
        return "TXN-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 결제 엔티티 생성
     */
    private Payment createPayment(Reservation reservation, PaymentProvider paymentProvider, String transactionId) {
        return new Payment(
                null,
                reservation,
                paymentProvider,
                PaymentStatus.PENDING,
                reservation.getTotalAmount(),
                transactionId,
                0L
        );
    }

    /**
     * 결제 및 예약 상태 업데이트
     */
    private PaymentResponse updatePaymentAndReservation(Payment payment, Reservation reservation, PaymentResult paymentResult) {
        // 결제 정보 업데이트
        payment.setTransactionId(paymentResult.getTransactionId());
        payment.setStatus(paymentResult.getStatus());

        // 예약 상태 업데이트
        switch (paymentResult.getStatus()) {
            case SUCCESS:
                reservation.setStatus(ReservationStatus.CONFIRMED);
                log.info("결제 성공 - 예약 확정. reservationId: {}, transactionId: {}",
                        reservation.getId(), paymentResult.getTransactionId());
                break;

            case PENDING:
                // 가상계좌 발급 완료 → 예약 상태는 여전히 결제 대기 유지
                reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
                log.info("가상계좌 발급 - 예약 결제 대기 유지. reservationId: {}, transactionId: {}",
                        reservation.getId(), paymentResult.getTransactionId());
                break;

            default: // FAILED, CANCELED 등
                reservation.setStatus(ReservationStatus.CANCELLED);
                log.warn("결제 실패/취소 - 예약 취소. reservationId: {}, transactionId: {}, status: {}",
                        reservation.getId(), paymentResult.getTransactionId(), paymentResult.getStatus());
                break;
        }

        // 동시 저장 (낙관적 락 체크)
        Payment savedPayment = paymentRepository.save(payment);
        Reservation savedReservation = reservationRepository.save(reservation);

        return PaymentResponse.from(savedPayment, savedReservation);
    }

    /**
     * 결제 실패 처리
     */
    private void handlePaymentFailure(Payment payment, Reservation reservation, Exception e) {
        payment.setStatus(PaymentStatus.FAILED);
        reservation.setStatus(ReservationStatus.CANCELLED);

        paymentRepository.save(payment);
        reservationRepository.save(reservation);

        log.error("결제 실패 처리 완료. reservationId: {}, error: {}",
                reservation.getId(), e.getMessage());
    }

    /**
     * 결제 상태 조회
     */
    @Transactional(readOnly = true)
    public PaymentStatus getPaymentStatus(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(Payment::getStatus)
                .orElseThrow(() ->
                        new IllegalArgumentException("결제 내역이 존재하지 않습니다. reservationId=" + reservationId)
                );
    }

    /**
     * 웹훅 처리 (낙관적 락 적용)
     */
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public void handleWebhook(String providerName, ProviderPayload payload) {
        try {
            handleWebhookInternal(providerName, payload);

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("웹훅 처리 중 낙관적 락 충돌. transactionId: {}, provider: {}",
                    payload.getTransactionId(), providerName);
            throw e; // 재시도 처리
        }
    }

    private void handleWebhookInternal(String providerName, ProviderPayload payload) {
        // 결제 정보 조회
        Payment payment = paymentRepository.findByTransactionId(payload.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("해당 거래 ID의 결제가 없습니다: " + payload.getTransactionId()));

        // 상태 검증 및 변환
        PaymentStatus newStatus = validateAndConvertStatus(payload.getStatus());

        // 이미 최종 상태인 경우 스킵
        if (isAlreadyFinalStatus(payment.getStatus(), newStatus)) {
            log.info("이미 최종 상태인 결제 - 웹훅 스킵. transactionId: {}, currentStatus: {}, newStatus: {}",
                    payload.getTransactionId(), payment.getStatus(), newStatus);
            return;
        }

        // 상태 업데이트
        payment.setStatus(newStatus);

        // 예약 상태 동기화
        Reservation reservation = payment.getReservation();
        updateReservationStatusFromPayment(reservation, newStatus);

        // 저장 (낙관적 락 체크)
        paymentRepository.save(payment);
        reservationRepository.save(reservation);

        log.info("웹훅 처리 완료. transactionId: {}, status: {} -> {}, reservationId: {}",
                payload.getTransactionId(), payment.getStatus(), newStatus, reservation.getId());
    }

    /**
     * 상태 검증 및 변환
     */
    private PaymentStatus validateAndConvertStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 결제 상태: {}", status);
            throw new IllegalArgumentException("지원하지 않는 결제 상태입니다: " + status);
        }
    }

    /**
     * 이미 최종 상태인지 확인
     */
    private boolean isAlreadyFinalStatus(PaymentStatus currentStatus, PaymentStatus newStatus) {
        return (currentStatus == PaymentStatus.SUCCESS || currentStatus == PaymentStatus.FAILED)
                && currentStatus == newStatus;
    }

    /**
     * 결제 상태에 따른 예약 상태 동기화
     */
    private void updateReservationStatusFromPayment(Reservation reservation, PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case SUCCESS:
                if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    log.info("예약 상태 확정으로 변경. reservationId: {}", reservation.getId());
                }
                break;

            case FAILED:
            case CANCELED:
                if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                    log.info("예약 상태 취소로 변경. reservationId: {}, paymentStatus: {}",
                            reservation.getId(), paymentStatus);
                }
                break;

            case PENDING:
                // 대기 상태 유지
                log.debug("결제 대기 상태 - 예약 상태 변경 없음. reservationId: {}", reservation.getId());
                break;

            default:
                log.warn("처리되지 않은 결제 상태. paymentStatus: {}, reservationId: {}",
                        paymentStatus, reservation.getId());
                break;
        }
    }
}
