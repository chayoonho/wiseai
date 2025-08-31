package com.example.wiseai_dev.reservation.application.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationUpdateRequest {

    @Schema(description = "예약 시작 시간", example = "2025-09-01T14:00:00")
    private LocalDateTime startTime;

    @Schema(description = "예약 종료 시간", example = "2025-09-01T16:00:00")
    private LocalDateTime endTime;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
}
