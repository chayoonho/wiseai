//package com.example.wiseai_dev.meetingRoom.domain.service;
//
//import com.example.wiseai_dev.meetingRoom.application.service.MeetingRoomService;
//import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
//import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
//import com.example.wiseai_dev.meetingRoom.infrastructrue.presistence.entity.MeetingRoomEntity;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class MeetingRoomServiceTest {
//
//    @Mock
//    private MeetingRoomRepository meetingRoomRepository;
//
//    @InjectMocks
//    private MeetingRoomService meetingRoomService;
//
//    @Test
//    void 회의실을_생성할_수_있음() {
//        // given
//        MeetingRoom newRoom = new MeetingRoom(null, "회의실A", 10, 50000.0);
//        MeetingRoomEntity savedEntity = new MeetingRoomEntity(1L, "회의실A", 10, 50000.0);
//
//        // Mock: 리포지토리가 엔티티를 저장하고, 저장된 엔티티를 반환하도록 설정
//        when(meetingRoomRepository.save(any(MeetingRoom.class))).thenReturn(savedEntity);
//
//        // when (실행)
//        // 서비스의 create 메서드를 호출
//        MeetingRoom savedRoom = meetingRoomService.createMeetingRoom(newRoom);
//
//        // then (검증)
//        // 반환된 객체가 null이 아닌지, ID가 할당되었는지 확인
//        assertNotNull(savedRoom.getId());
//        assertEquals(1L, savedRoom.getId());
//        assertEquals("회의실A", savedRoom.getName());
//    }
//
//    @Test
//    void 특정_회의실을_조회할_수_있음() {
//        // given
//        MeetingRoomEntity findEntity = new MeetingRoomEntity(1L, "회의실A", 10, 50000.0);
//
//        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(findEntity));
//
//        // when
//        MeetingRoom foundRoom = meetingRoomService.findMeetingRoomById(1L);
//
//        // then
//        assertNotNull(foundRoom);
//        assertEquals("회의실A", foundRoom.getName());
//        assertEquals(1L, foundRoom.getId());
//    }
//
//    @Test
//    void 전체_회의실을_조회할_수_있음() {
//        // given
//        List<MeetingRoomEntity> allRoomsEntities = Arrays.asList(
//                new MeetingRoomEntity(1L, "회의실A", 10, 50000.0),
//                new MeetingRoomEntity(2L, "회의실B", 20, 100000.0),
//                new MeetingRoomEntity(3L, "회의실C", 5, 30000.0)
//        );
//        when(meetingRoomRepository.findAll()).thenReturn(allRoomsEntities);
//
//        // when
//        List<MeetingRoom> foundRooms = meetingRoomService.findAllMeetingRooms();
//
//        // then
//        assertFalse(foundRooms.isEmpty());
//        assertEquals(3, foundRooms.size());
//        assertEquals("회의실A", foundRooms.get(0).getName());
//        assertEquals("회의실B", foundRooms.get(1).getName());
//        assertEquals("회의실C", foundRooms.get(2).getName());
//    }
//
//    @Test
//    void 회의실_정보를_수정할_수_있음() {
//        // given
//        MeetingRoomEntity existingEntity = new MeetingRoomEntity(1L, "회의실A", 10, 50000.0);
//        MeetingRoom updatedRoomData = new MeetingRoom(1L, "새로운 회의실A", 15, 60000.0);
//
//        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
//        when(meetingRoomRepository.save(any(MeetingRoomEntity.class)))
//                .thenReturn(new MeetingRoomEntity(1L, "새로운 회의실A", 15, 60000.0));
//
//        // when
//        MeetingRoom updatedRoom = meetingRoomService.updateMeetingRoom(1L, updatedRoomData);
//
//        // then
//        assertNotNull(updatedRoom);
//        assertEquals("새로운 회의실A", updatedRoom.getName());
//        assertEquals(15, updatedRoom.getCapacity());
//        assertEquals(60000.0, updatedRoom.getPricePerDay());
//    }
//
//    @Test
//    void 회의실을_삭제할_수_있음() {
//        // given
//        MeetingRoomEntity existingEntity = new MeetingRoomEntity(1L, "회의실A", 10, 50000.0);
//
//        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
//
//        // when
//        meetingRoomService.deleteMeetingRoom(1L);
//
//        // then
//        verify(meetingRoomRepository).deleteById(1L);
//    }
//}
