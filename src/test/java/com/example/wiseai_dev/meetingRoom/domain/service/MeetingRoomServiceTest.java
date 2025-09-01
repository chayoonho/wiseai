package com.example.wiseai_dev.meetingRoom.domain.service;

import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomRequest;
import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomResponse;
import com.example.wiseai_dev.meetingRoom.application.service.MeetingRoomService;
import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingRoomServiceTest {

    @Mock
    private MeetingRoomRepository meetingRoomRepository;

    @InjectMocks
    private MeetingRoomService meetingRoomService;

    @Test
    @DisplayName("회의실을 생성할 수 있음")
    void 회의실을_생성할_수_있음() {
        // given
        MeetingRoomRequest newRoom = new MeetingRoomRequest("회의실A", 10, 50000.0);
        MeetingRoom savedRoom = new MeetingRoom(1L, "회의실A", 10, 50000.0);

        when(meetingRoomRepository.save(any(MeetingRoom.class))).thenReturn(savedRoom);

        // when
        MeetingRoomResponse resultRoom = meetingRoomService.createMeetingRoom(newRoom);

        // then
        assertThat(resultRoom).isNotNull();
        assertThat(resultRoom.getId()).isEqualTo(1L);
        assertThat(resultRoom.getName()).isEqualTo("회의실A");
        verify(meetingRoomRepository, times(1)).save(any(MeetingRoom.class));
    }

    @Test
    @DisplayName("특정 회의실을 조회할 수 있음")
    void 특정_회의실을_조회할_수_있음() {
        // given
        MeetingRoom findRoom = new MeetingRoom(1L, "회의실A", 10, 50000.0);
        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(findRoom));

        // when
        MeetingRoomResponse foundRoom = meetingRoomService.findMeetingRoomById(1L);

        // then
        assertThat(foundRoom).isNotNull();
        assertThat(foundRoom.getId()).isEqualTo(1L);
        assertThat(foundRoom.getName()).isEqualTo("회의실A");
        verify(meetingRoomRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("전체 회의실을 조회할 수 있음")
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
        assertThat(foundRooms).hasSize(3);
        assertThat(foundRooms).extracting("name")
                .containsExactly("회의실A", "회의실B", "회의실C");
        verify(meetingRoomRepository, times(1)).findAll();
    }

}
