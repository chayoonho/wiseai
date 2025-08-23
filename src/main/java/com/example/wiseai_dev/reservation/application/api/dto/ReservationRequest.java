package com.example.wiseai_dev.reservation.application.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Schema(
        description = "예약 생성 요청 DTO",
        example = "{ \"meetingRoomId\": 1, \"startTime\": \"2025-08-18T10:00:00\", \"endTime\": \"2025-08-18T12:00:00\", \"bookerName\": \"홍길동\" }"
)
@Getter
@Setter
public class ReservationRequest {

    @Schema(description = "회의실 ID", example = "1")
    @NotNull(message = "회의실 ID는 필수입니다.")
    private Long meetingRoomId;

    @Schema(description = "예약 시작 시간", example = "2025-08-18T10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "예약 종료 시간", example = "2025-08-18T12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "예약자 이름", example = "홍길동")
    @NotNull(message = "예약자 이름은 필수입니다.")
    private String bookerName;

    private String reservationNo;
}
