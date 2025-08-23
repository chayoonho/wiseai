package com.example.wiseai_dev.reservation.domain.service;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.meetingRoom.domain.repository.MeetingRoomRepository;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.service.ReservationService;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private MeetingRoomRepository meetingRoomRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        meetingRoomRepository = mock(MeetingRoomRepository.class);
        reservationService = new ReservationService(reservationRepository, meetingRoomRepository);
    }

    @Test
    void 예약_생성_성공() {
        // given
        ReservationRequest request = new ReservationRequest();
        request.setMeetingRoomId(1L);
        request.setStartTime(LocalDateTime.of(2025, 8, 25, 10, 0));
        request.setEndTime(LocalDateTime.of(2025, 8, 25, 11, 0));
        request.setBookerName("홍길동");

        MeetingRoom meetingRoom = new MeetingRoom(1L, "회의실A", 10, 10000);
        when(meetingRoomRepository.findById(1L)).thenReturn(Optional.of(meetingRoom));

        // when
        var response = reservationService.createReservation(request);

        // then
        assertThat(response.getBookerName()).isEqualTo("홍길동");
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }
}
