package com.example.wiseai_dev.reservation.infrastructrue.presistence.repository;

import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.entity.ReservationEntity;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.jpa.ReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity savedEntity = jpaRepository.save(entity);
        return toDomainModel(savedEntity);
    }

    /**
     * 일반 조회 (락 없음)
     */
    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomainModel);
    }

    /**
     * 비관적 락 조회 (결제/취소 시 동시성 제어 용도)
     */
    public Optional<Reservation> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdWithPessimisticLock(id).map(this::toDomainModel);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByMeetingRoomIdAndTimeRange(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.findOverlappingReservations(meetingRoomId, startTime, endTime)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    // --- 변환 헬퍼 메서드 ---
    private Reservation toDomainModel(ReservationEntity entity) {
        if (entity == null) return null;
        return new Reservation(
                entity.getId(),
                entity.getMeetingRoomId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getBookerName(),
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getVersion()
        );
    }

    private ReservationEntity toEntity(Reservation domainModel) {
        if (domainModel == null) return null;
        return new ReservationEntity(
                domainModel.getId(),
                domainModel.getMeetingRoomId(),
                domainModel.getStartTime(),
                domainModel.getEndTime(),
                domainModel.getBookerName(),
                domainModel.getStatus(),
                domainModel.getTotalAmount(),
                domainModel.getVersion()
        );
    }
}
