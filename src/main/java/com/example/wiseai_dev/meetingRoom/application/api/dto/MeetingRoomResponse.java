package com.example.wiseai_dev.meetingRoom.application.api.dto;

import com.example.wiseai_dev.meetingRoom.domain.model.MeetingRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingRoomResponse {
    @Schema(description = "회의실 ID", example = "1")
    private Long id;

    @Schema(description = "회의실 이름", example = "회의실 A")
    private String name;

    @Schema(description = "수용 인원", example = "10")
    private int capacity;

    @Schema(description = "시간당 요금", example = "50000")
    private double hourlyRate;

    public static MeetingRoomResponse fromDomain(MeetingRoom meetingRoom) {
        return new MeetingRoomResponse(
                meetingRoom.getId(),
                meetingRoom.getName(),
                meetingRoom.getCapacity(),
                meetingRoom.getHourlyRate()
        );
    }
}
