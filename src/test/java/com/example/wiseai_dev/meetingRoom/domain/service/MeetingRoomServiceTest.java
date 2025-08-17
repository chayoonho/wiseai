package com.example.wiseai_dev.meetingRoom.domain.service;

import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomResponse;
import com.example.wiseai_dev.meetingRoom.application.service.MeetingRoomService;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.entity.MeetingRoomEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeetingRoomServiceTest {

    @Mock
    private MeetingRoomRepository meetingRoomRepository;

    @InjectMocks
    private MeetingRoomService meetingRoomService;

    @Test
    void 회의실을_생성할_수_있음() {
        // given
        MeetingRoom newRoom = new MeetingRoom(null, "회의실A", 10, 50000.0);
        // Mocking 시 반환될 MeetingRoom 객체를 생성
        MeetingRoom savedRoom = new MeetingRoom(1L, "회의실A", 10, 50000.0);

        // Mock: 리포지토리가 도메인 모델(MeetingRoom)을 저장하고, 저장된 도메인 모델을 반환하도록 설정
        when(meetingRoomRepository.save(any(MeetingRoom.class))).thenReturn(savedRoom);

        // when
        MeetingRoomResponse resultRoom = meetingRoomService.createMeetingRoom(newRoom);

        // then
        assertNotNull(resultRoom.getId());
        assertEquals(1L, resultRoom.getId());
        assertEquals("회의실A", resultRoom.getName());
    }

    @Test
    void 특정_회의실을_조회할_수_있음() {
        // given
        MeetingRoom findRoom = new MeetingRoom(1L, "회의실A", 10, 50000.0);

        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(findRoom));

        // when
        MeetingRoomResponse foundRoom = meetingRoomService.findMeetingRoomById(1L);

        // then
        assertNotNull(foundRoom);
        assertEquals("회의실A", foundRoom.getName());
        assertEquals(1L, foundRoom.getId());
    }

    @Test
    void 전체_회의실을_조회할_수_있음() {
        // given
        List<MeetingRoom> allRooms = Arrays.asList(
                new MeetingRoom(1L, "회의실A", 10, 50000.0),
                new MeetingRoom(2L, "회의실B", 20, 100000.0),
                new MeetingRoom(3L, "회의실C", 5, 30000.0)
        );
        when(meetingRoomRepository.findAll()).thenReturn(allRooms);

        // when
        List<MeetingRoomResponse> foundRooms = meetingRoomService.findAllMeetingRooms();

        // then
        assertFalse(foundRooms.isEmpty());
        assertEquals(3, foundRooms.size());
        assertEquals("회의실A", foundRooms.get(0).getName());
        assertEquals("회의실B", foundRooms.get(1).getName());
        assertEquals("회의실C", foundRooms.get(2).getName());
    }


    @Test
    void 회의실을_삭제할_수_있음() {
        // given
        MeetingRoom existingRoom = new MeetingRoom(1L, "회의실A", 10, 50000.0);

        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));


        // then
        verify(meetingRoomRepository).deleteById(1L);
    }
}
