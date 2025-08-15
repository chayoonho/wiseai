package com.example.wiseai_dev.reservation.application.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationRequest {
    @NotNull(message = "회의실 ID는 필수입니다.")
    private Long meetingRoomId;
    @NotNull(message = "예약 시작 시간은 필수입니다.")
    private LocalDateTime startTime;
    @NotNull(message = "예약 종료 시간은 필수입니다.")
    private LocalDateTime endTime;
    @NotNull(message = "예약자 이름은 필수입니다.")
    private String bookerName;
}
