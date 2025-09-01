package com.example.wiseai_dev.reservation.domain.service;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationResponse;
import com.example.wiseai_dev.reservation.application.service.ReservationService;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import com.example.wiseai_dev.user.domain.model.User;
import com.example.wiseai_dev.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private MeetingRoomRepository meetingRoomRepository;
    private UserRepository userRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        meetingRoomRepository = mock(MeetingRoomRepository.class);
        userRepository = mock(UserRepository.class);

        reservationService = new ReservationService(
                reservationRepository,
                meetingRoomRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        // given
        ReservationRequest request = new ReservationRequest();
        request.setMeetingRoomId(1L);
        request.setStartTime(LocalDateTime.of(2025, 8, 25, 10, 0));
        request.setEndTime(LocalDateTime.of(2025, 8, 25, 11, 0));
        request.setUserId(100L);

        MeetingRoom meetingRoom = new MeetingRoom(1L, "회의실A", 10, 10000);
        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(meetingRoom));

        User user = User.builder()
                .id(100L)
                .name("홍길동")
                .email("hong@test.com")
                .build();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReservationResponse response = reservationService.createReservation(request);

        // then
        assertThat(response.getMeetingRoomId()).isEqualTo(1L);
        assertThat(response.getBookerName()).isEqualTo("홍길동");
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);

        verify(reservationRepository, times(1)).save(any());
        verify(meetingRoomRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(100L);
    }
}
