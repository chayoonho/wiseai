package com.example.wiseai_dev.meetingRoom.domain.repository;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;

public interface MeetingRoomRepository {
    MeetingRoom save(MeetingRoom meetingRoom);
    List<MeetingRoom> findAll();
    Optional<MeetingRoom> findById(Long id);
    void deleteById(Long id);
}
