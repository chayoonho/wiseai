package com.example.wiseai_dev.meetingRoom.application.service;

import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomRequest;
import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomResponse;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;

    public MeetingRoomService(MeetingRoomRepository meetingRoomRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public MeetingRoomResponse createMeetingRoom(MeetingRoomRequest meetingRoomRequest) {
        MeetingRoom meetingRoom = new MeetingRoom(
                null,
                meetingRoomRequest.getName(),
                meetingRoomRequest.getCapacity(),
                meetingRoomRequest.getHourlyRate()
        );

        MeetingRoom savedMeetingRoom = meetingRoomRepository.save(meetingRoom);

        return new MeetingRoomResponse(
                savedMeetingRoom.getId(),
                savedMeetingRoom.getName(),
                savedMeetingRoom.getCapacity(),
                savedMeetingRoom.getHourlyRate()
        );
    }

    @Transactional(readOnly = true)
    public MeetingRoomResponse findMeetingRoomById(Long id) {
        // ID로 도메인 모델 조회
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));

        // 조회된 도메인 모델 -> DTO 변환
        return new MeetingRoomResponse(
                meetingRoom.getId(),
                meetingRoom.getName(),
                meetingRoom.getCapacity(),
                meetingRoom.getHourlyRate()
        );
    }

    @Transactional(readOnly = true)
    public List<MeetingRoomResponse> findAllMeetingRooms() {
        // 모든 도메인 모델 조회
        return meetingRoomRepository.findAll().stream()
                // 도메인 모델을 DTO로 변환
                .map(meetingRoom -> new MeetingRoomResponse(
                        meetingRoom.getId(),
                        meetingRoom.getName(),
                        meetingRoom.getCapacity(),
                        meetingRoom.getHourlyRate()))
                .collect(Collectors.toList());
    }
}