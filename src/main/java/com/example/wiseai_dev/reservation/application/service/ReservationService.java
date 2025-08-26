package com.example.wiseai_dev.reservation.application.service;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationResponse;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationUpdateRequest;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    /**
     * 예약 생성
     */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        validateReservationTime(request.getStartTime(), request.getEndTime());
        checkTimeAvailability(request.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

        double totalAmount = calculateTotalAmount(
                request.getMeetingRoomId(),
                request.getStartTime(),
                request.getEndTime()
        );

        // 1. 저장
        Reservation newReservation = Reservation.create(
                request.getMeetingRoomId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBookerName(),
                totalAmount,
                ReservationStatus.PENDING_PAYMENT
        );

        Reservation saved = reservationRepository.save(newReservation);


        // 2. 최종 응답 반환
        return ReservationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        return ReservationResponse.fromEntity(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 예약 변경 - 낙관적 락 적용
     */
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        try {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

            validateReservationTime(request.getStartTime(), request.getEndTime());
            checkTimeAvailability(reservation.getMeetingRoomId(), request.getStartTime(), request.getEndTime(), id);

            double newTotalAmount = calculateTotalAmount(
                    reservation.getMeetingRoomId(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            reservation.update(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getBookerName(),
                    newTotalAmount
            );

            return ReservationResponse.fromEntity(reservationRepository.save(reservation));

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 이미 예약을 변경했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * 예약 취소 (낙관적 락)
     */
    @Transactional
    public ReservationResponse updateReservationStatusToCancelled(Long id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("취소하려는 예약 정보를 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }


    // ====== 내부 비즈니스 로직 ======
    private void validateReservationTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        if (startTime.getMinute() % 30 != 0 || endTime.getMinute() % 30 != 0) {
            throw new IllegalArgumentException("예약 시작/종료 시간은 30분 단위여야 합니다.");
        }
    }

    private void checkTimeAvailability(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        checkTimeAvailability(meetingRoomId, startTime, endTime, null);
    }

    private void checkTimeAvailability(Long meetingRoomId, LocalDateTime startTime,
                                       LocalDateTime endTime, Long currentReservationId) {
        List<Reservation> existing = reservationRepository.findByMeetingRoomIdAndTimeRange(meetingRoomId, startTime, endTime);
        if (!existing.isEmpty()) {
            if (currentReservationId != null &&
                    existing.size() == 1 &&
                    existing.get(0).getId().equals(currentReservationId)) {
                return;
            }
            throw new IllegalStateException("해당 시간대에 이미 예약이 존재합니다.");
        }
    }

    private double calculateTotalAmount(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        long hours = (minutes + 59) / 60; // 올림
        return hours * meetingRoom.getHourlyRate();
    }
}
