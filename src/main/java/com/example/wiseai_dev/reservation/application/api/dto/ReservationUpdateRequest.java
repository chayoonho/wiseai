package com.example.wiseai_dev.reservation.application.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationUpdateRequest {

    @NotNull(message = "시작 시간은 필수입니다.")
    @Future(message = "시작 시간은 현재 시각 이후여야 합니다.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    @Future(message = "종료 시간은 현재 시각 이후여야 합니다.")
    private LocalDateTime endTime;

    @NotBlank(message = "예약자 이름은 필수입니다.")
    private String bookerName;
}