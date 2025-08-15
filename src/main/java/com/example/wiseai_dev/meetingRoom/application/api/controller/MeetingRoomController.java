package com.example.wiseai_dev.meetingRoom.application.api.controller;

import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomRequest;
import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomResponse;

import com.example.wiseai_dev.meetingRoom.application.service.MeetingRoomService;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.entity.MeetingRoomEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting-rooms")
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    @PostMapping
    public ResponseEntity<MeetingRoomResponse> createMeetingRoom(@Valid @RequestBody MeetingRoomRequest request) {
        MeetingRoomResponse response = meetingRoomService.createMeetingRoom(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MeetingRoomResponse>> getAllMeetingRooms() {
        List<MeetingRoomResponse> meetingRooms = meetingRoomService.findAllMeetingRooms();
        return ResponseEntity.ok(meetingRooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingRoomResponse> getMeetingRoomById(@PathVariable Long id) {
        MeetingRoomResponse meetingRoom = meetingRoomService.findMeetingRoomById(id);
        return ResponseEntity.ok(meetingRoom);
    }
}
