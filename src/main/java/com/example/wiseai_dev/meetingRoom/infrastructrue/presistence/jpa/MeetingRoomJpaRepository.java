package com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.jpa;

import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.entity.MeetingRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomJpaRepository extends JpaRepository<MeetingRoomEntity, Long> {
}
