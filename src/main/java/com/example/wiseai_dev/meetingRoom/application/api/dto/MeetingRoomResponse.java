package com.example.wiseai_dev.meetingRoom.application.api.dto;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import com.example.wiseai_dev.payment.domain.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomResponse {

    @Schema(description = "회의실 ID", example = "1")
    private Long id;

    @Schema(description = "회의실 이름", example = "ROOM NO.1")
    private String name;

    @Schema(description = "수용 인원", example = "6")
    private int capacity;

    @Schema(description = "시간당 요금 (원)", example = "15000")
    private double hourlyRate;

    public static MeetingRoomResponse fromEntity(MeetingRoom meetingRoom) {
        return MeetingRoomResponse.builder()
                .id(meetingRoom.getId())
                .name(meetingRoom.getName())
                .capacity(meetingRoom.getCapacity())
                .hourlyRate(meetingRoom.getHourlyRate())
                .build();
    }
}
