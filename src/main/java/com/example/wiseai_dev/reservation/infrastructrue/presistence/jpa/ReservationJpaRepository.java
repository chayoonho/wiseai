package com.example.wiseai_dev.reservation.infrastructrue.presistence.jpa;

import com.example.wiseai_dev.reservation.infrastructure.persistence.entity.ReservationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity,Long> {
    List<ReservationEntity> findByMeetingRoomIdAndStartTimeBetween(long meetingRoomId, LocalDateTime start, LocalDateTime end);
    // 겹치는 시간대 예약 조회 쿼리
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.meetingRoomId = :meetingRoomId " +
            "AND r.status IN ('CONFIRMED', 'PENDING_PAYMENT') " +
            "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    List<ReservationEntity> findOverlappingReservations(@Param("meetingRoomId") Long meetingRoomId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    @Query("select r from ReservationEntity r where r.id = :id")
    Optional<ReservationEntity> findByIdWithPessimisticLock(@Param("id") Long id);
}
