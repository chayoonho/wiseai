package com.example.wiseai_dev.reservation.application.api.dto;

import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationResponse {
    private Long id;
    private Long meetingRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookerName;
    private ReservationStatus status;
    private double totalAmount;
}