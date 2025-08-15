package com.example.wiseai_dev.meetingRoom.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.query.internal.ResultMementoEntityJpa;

@Getter
@NoArgsConstructor
public class MeetingRoom {
    private Long id;
    private String name;
    private int capacity;
    private double hourlyRate;

    public MeetingRoom(Long id, String name, int capacity, double hourlyRate) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
    }

}
