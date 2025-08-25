package com.example.wiseai_dev.reservation.infrastructrue.presistence.repository;

import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.entity.ReservationEntity;
import com.example.wiseai_dev.reservation.infrastructrue.presistence.jpa.ReservationJpaRepository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity savedEntity = jpaRepository.save(entity);
        return toDomainModel(savedEntity);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
//        Optional<ReservationEntity> entity = jpaRepository.findById(id);
        Optional<ReservationEntity> entity = jpaRepository.findByIdWithPessimisticLock(id);
        return entity.map(this::toDomainModel);
    }

    @Override
    public List<Reservation> findAll() {
        List<ReservationEntity> entities = jpaRepository.findAll();
        return entities.stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByMeetingRoomIdAndTimeRange(Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ReservationEntity> entities = jpaRepository.findOverlappingReservations(meetingRoomId, startTime, endTime);
        return entities.stream()
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
        if (entity == null) {
            return null;
        }
        return new Reservation(entity.getId(), entity.getReservationNo(), entity.getMeetingRoomId(), entity.getStartTime(), entity.getEndTime(), entity.getBookerName(), entity.getStatus(), entity.getTotalAmount());
    }

    private ReservationEntity toEntity(Reservation domainModel) {
        if (domainModel == null) {
            return null;
        }
        return new ReservationEntity(
                domainModel.getId(),
                domainModel.getReservationNo(),
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