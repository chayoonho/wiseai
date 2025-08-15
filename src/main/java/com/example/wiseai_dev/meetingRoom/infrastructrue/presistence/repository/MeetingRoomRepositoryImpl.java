package com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.repository;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.entity.MeetingRoomEntity;
import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.jpa.MeetingRoomJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MeetingRoomRepositoryImpl implements MeetingRoomRepository {

    private final MeetingRoomJpaRepository jpaRepository;

    public MeetingRoomRepositoryImpl(MeetingRoomJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MeetingRoom save(MeetingRoom meetingRoom) {
        MeetingRoomEntity entity = toEntity(meetingRoom);

        MeetingRoomEntity savedEntity = jpaRepository.save(entity);

        return toDomainModel(savedEntity);
    }

    @Override
    public Optional<MeetingRoom> findById(Long id) {
        Optional<MeetingRoomEntity> entity = jpaRepository.findById(id);

        return entity.map(this::toDomainModel);
    }

    @Override
    public List<MeetingRoom> findAll() {
        List<MeetingRoomEntity> entities = jpaRepository.findAll();

        return entities.stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    // --- 변환 헬퍼 메서드 ---
    private MeetingRoom toDomainModel(MeetingRoomEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MeetingRoom(entity.getId(), entity.getName(), entity.getCapacity(), entity.getHourlyRate());
    }

    private MeetingRoomEntity toEntity(MeetingRoom domainModel) {
        if (domainModel == null) {
            return null;
        }
        return new MeetingRoomEntity(domainModel.getId(), domainModel.getName(), domainModel.getCapacity(), domainModel.getHourlyRate());
    }
}