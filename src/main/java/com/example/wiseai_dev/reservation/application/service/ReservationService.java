package com.example.wiseai_dev.reservation.application.service;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationResponse;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationUpdateRequest;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MeetingRoomRepository meetingRoomRepository;

    public ReservationService(ReservationRepository reservationRepository, MeetingRoomRepository meetingRoomRepository) {
        this.reservationRepository = reservationRepository;
        this.meetingRoomRepository = meetingRoomRepository;
    }

    // 예약 생성
    public ReservationResponse createReservation(ReservationRequest request) {
        // 1. 비즈니스 규칙 검증: 30분 단위, 시작 시간 < 종료 시간 등
        validateReservationTime(request.getStartTime(), request.getEndTime());

        // 2. 중복 예약 확인
        checkTimeAvailability(request.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

        // 3. 결제 금액 계산
        double totalAmount = calculateTotalAmount(request.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

        // 4. DTO -> 도메인 모델 변환 및 초기 상태 설정
        Reservation newReservation = new Reservation(
                request.getReservationNo(),
                request.getMeetingRoomId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBookerName(),
                ReservationStatus.PENDING_PAYMENT,
                totalAmount
        );

        // 저장 전에 임시 reservationNo 세팅
        newReservation.setReservationNo("TEMP");

        // 5. 도메인 모델 저장 (id 할당됨)
        Reservation savedReservation = reservationRepository.save(newReservation);

        // 6. 채번 로직 (id 기반 예약번호 생성)
        String reservationNo = "RES-" + savedReservation.getId();
        savedReservation.setReservationNo(reservationNo);

        // 7. 다시 저장하여 예약번호 반영
        Reservation updatedReservation = reservationRepository.save(savedReservation);

        // 8. 저장된 도메인 모델 -> 응답 DTO 변환 및 반환
        return toDto(updatedReservation);
    }


    // 단일 예약 조회
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        return toDto(reservation);
    }

    // 전체 예약 조회
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 예약변경
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        // 1. 기존 예약 정보 조회
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 예약 정보를 찾을 수 없습니다."));

        // 2. 비즈니스 규칙 검증 및 중복 예약 확인
        validateReservationTime(request.getStartTime(), request.getEndTime());
        checkTimeAvailability(reservation.getMeetingRoomId(), request.getStartTime(), request.getEndTime(), id);

        // 3. 결제 금액 재계산
        double newTotalAmount = calculateTotalAmount(reservation.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

        // 4. 도메인 모델 업데이트
        reservation.update(
                request.getStartTime(),
                request.getEndTime(),
                request.getBookerName(),
                newTotalAmount
        );

        // 5. 업데이트된 도메인 모델 저장
        Reservation updatedReservation = reservationRepository.save(reservation);
        return toDto(updatedReservation);
    }
    
    // 예약 취소(상태변경)
    public ReservationResponse updateReservationStatusToCancelled(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("취소하려는 예약 정보를 찾을 수 없습니다."));

        // 예약 상태를 CANCELLED로 변경
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        return toDto(reservation);
    }

    private ReservationResponse toDto(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setMeetingRoomId(reservation.getMeetingRoomId());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setBookerName(reservation.getBookerName());
        response.setStatus(reservation.getStatus());
        response.setTotalAmount(reservation.getTotalAmount());
        return response;
    }

    // --- 비즈니스 로직 헬퍼 메서드 ---
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

    private void checkTimeAvailability(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime, Long currentReservationId) {
        List<Reservation> existingReservations = reservationRepository.findByMeetingRoomIdAndTimeRange(meetingRoomId, startTime, endTime);
        if (!existingReservations.isEmpty()) {
            if (currentReservationId != null && existingReservations.size() == 1 && existingReservations.get(0).getId().equals(currentReservationId)) {
                // 수정하려는 예약이 자신일 경우 통과
                return;
            }
            throw new IllegalStateException("해당 시간대에 이미 예약이 존재합니다.");
        }
    }

    private double calculateTotalAmount(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        long hours = (minutes + 59) / 60; // 올림 계산
        return hours * meetingRoom.getHourlyRate();
    }
}