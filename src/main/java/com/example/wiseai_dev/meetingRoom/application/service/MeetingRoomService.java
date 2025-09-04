package com.example.wiseai_dev.meetingRoom.application.service;

import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomRequest;
import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomResponse;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;

    /**
     * 회의실 생성
     */
    @Transactional
    public MeetingRoomResponse createMeetingRoom(MeetingRoomRequest meetingRoomRequest) {
        MeetingRoom meetingRoom = MeetingRoom.create(
                meetingRoomRequest.getName(),
                meetingRoomRequest.getCapacity(),
                meetingRoomRequest.getHourlyRate()
        );

        MeetingRoom savedMeetingRoom = meetingRoomRepository.save(meetingRoom);
        return MeetingRoomResponse.fromDomain(savedMeetingRoom);
    }

    /**
     * 회의실 단건 조회
     */
    @Transactional(readOnly = true)
    public MeetingRoomResponse findMeetingRoomById(Long id) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));
        return MeetingRoomResponse.fromDomain(meetingRoom);
    }

    /**
     * 회의실 전체 조회
     */
    @Transactional(readOnly = true)
    public List<MeetingRoomResponse> findAllMeetingRooms() {
        return meetingRoomRepository.findAll().stream()
                .map(MeetingRoomResponse::fromDomain)
                .toList();
    }
}
