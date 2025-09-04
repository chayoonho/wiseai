package com.example.wiseai_dev.meetingRoom.domain.model;

import lombok.Getter;

@Getter
public class MeetingRoom {
    private final Long id;
    private final String name;
    private final int capacity;
    private final double hourlyRate;

    public MeetingRoom(Long id, String name, int capacity, double hourlyRate) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("수용 인원은 0보다 커야 합니다.");
        }
        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("시간당 요금은 0보다 커야 합니다.");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
    }

    // 팩토리 메서드
    public static MeetingRoom create(String name, int capacity, double hourlyRate) {
        return new MeetingRoom(null, name, capacity, hourlyRate);
    }

    // ID 포함 생성자 (DB 조회 시)
    public static MeetingRoom of(Long id, String name, int capacity, double hourlyRate) {
        return new MeetingRoom(id, name, capacity, hourlyRate);
    }
}
