package com.example.wiseai_dev.meetingRoom.application.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingRoomRequest {
    @NotBlank(message = "회의실 이름은 필수입니다.")
    private String name;

    @Min(value = 1, message = "수용 인원은 최소 1명 이상이어야 합니다.")
    private int capacity;

    @Min(value = 0, message = "시간당 요금은 0원 이상이어야 합니다.")
    private double hourlyRate;
}
