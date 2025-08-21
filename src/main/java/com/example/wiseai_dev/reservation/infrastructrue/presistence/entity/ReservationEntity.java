package com.example.wiseai_dev.reservation.infrastructure.persistence.entity;

import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reservationNo; // 우리가 사용할 접두사가 붙은 예약번호

    private Long meetingRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookerName;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private double totalAmount;

    @Version
    private long version;

}