package com.example.wiseai_dev.reservation.application.service;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationResponse;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationUpdateRequest;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    /**
     * 예약 생성
     */
    public ReservationResponse createReservation(ReservationRequest request) {
        // 1. 시간 검증
        validateReservationTime(request.getStartTime(), request.getEndTime());

        // 2. 중복 예약 확인
        checkTimeAvailability(request.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

        // 3. 결제 금액 계산
        double totalAmount = calculateTotalAmount(
                request.getMeetingRoomId(),
                request.getStartTime(),
                request.getEndTime()
        );

        // 4. 예약 생성 및 저장 (한 번만 저장)
        Reservation newReservation = Reservation.builder()
                .meetingRoomId(request.getMeetingRoomId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookerName(request.getBookerName())
                .status(ReservationStatus.PENDING_PAYMENT)
                .totalAmount(totalAmount)
                .build();

        Reservation savedReservation = reservationRepository.save(newReservation);
        savedReservation.setReservationNo("RES-" + savedReservation.getId());

        return ReservationResponse.fromEntity(savedReservation);
    }

    /**
     * 단일 예약 조회
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        return ReservationResponse.fromEntity(reservation);
    }

    /**
     * 전체 예약 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 예약 변경
     */
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 예약 정보를 찾을 수 없습니다."));

        // 시간 검증 및 중복 체크
        validateReservationTime(request.getStartTime(), request.getEndTime());
        checkTimeAvailability(reservation.getMeetingRoomId(), request.getStartTime(), request.getEndTime(), id);

        double newTotalAmount = calculateTotalAmount(
                reservation.getMeetingRoomId(),
                request.getStartTime(),
                request.getEndTime()
        );

        // 엔티티 메서드를 통해 업데이트
        reservation.update(
                request.getStartTime(),
                request.getEndTime(),
                request.getBookerName(),
                newTotalAmount
        );

        return ReservationResponse.fromEntity(reservation);
    }

    /**
     * 예약 취소 (상태 변경)
     */
    public ReservationResponse updateReservationStatusToCancelled(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("취소하려는 예약 정보를 찾을 수 없습니다."));

        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.fromEntity(reservation);
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
        List<Reservation> existingReservations =
                reservationRepository.findByMeetingRoomIdAndTimeRange(meetingRoomId, startTime, endTime);

        if (!existingReservations.isEmpty()) {
            if (currentReservationId != null
                    && existingReservations.size() == 1
                    && existingReservations.get(0).getId().equals(currentReservationId)) {
                return; // 자기 자신이면 통과
            }
            throw new IllegalStateException("해당 시간대에 이미 예약이 존재합니다.");
        }
    }

    private double calculateTotalAmount(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        long hours = (minutes + 59) / 60; // 올림 처리
        return hours * meetingRoom.getHourlyRate();
    }
}
