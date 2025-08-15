package com.example.wiseai_dev.meetingRoom.application.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomResponse {
    private Long id;
    private String name;
    private int capacity;
    private double hourlyRate;
}