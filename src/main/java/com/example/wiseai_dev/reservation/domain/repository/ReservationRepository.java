package com.example.wiseai_dev.reservation.domain.repository;

import com.example.wiseai_dev.reservation.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);   // 락 없음 (조회용)
    Optional<Reservation> findByIdForUpdate(Long id); //  락 있음 (결제/취소 시)
    List<Reservation> findAll();
    void deleteById(Long id);
    List<Reservation> findByMeetingRoomIdAndTimeRange(Long meetingRoomId,
                                                      LocalDateTime startTime,
                                                      LocalDateTime endTime);
    void flush();
    void deleteAll();
}

