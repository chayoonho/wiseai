package com.example.wiseai_dev.reservation.domain.repository;

import com.example.wiseai_dev.reservation.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    List<Reservation> findAll();
    void deleteById(Long id);
    List<Reservation> findByMeetingRoomIdAndTimeRange(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime);
}
