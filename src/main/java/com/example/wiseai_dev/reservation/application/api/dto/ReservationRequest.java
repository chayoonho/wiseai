package com.example.wiseai_dev.reservation.application.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(
        description = "예약 생성 요청 DTO",
        example = "{\n" +
                "  \"meetingRoomId\": 1,\n" +
                "  \"startTime\": \"2025-08-18T10:00:00\",\n" +
                "  \"endTime\": \"2025-08-18T12:00:00\",\n" +
                "  \"userId\": 1\n" +
                "}"
)
@Getter
@Setter
public class ReservationRequest {

    @Schema(description = "회의실 ID", example = "1")
    @NotNull(message = "회의실 ID는 필수입니다.")
    private Long meetingRoomId;

    @Schema(description = "예약 시작 시간", example = "2025-08-18T10:00:00")
    @NotNull(message = "시작 시간은 필수입니다.")
    @Future(message = "시작 시간은 현재 시각 이후여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "예약 종료 시간", example = "2025-08-18T12:00:00")
    @NotNull(message = "종료 시간은 필수입니다.")
    @Future(message = "종료 시간은 현재 시각 이후여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "예약자(사용자) ID", example = "1")
    @NotNull(message = "예약자 ID는 필수입니다.")
    private Long userId;
}
