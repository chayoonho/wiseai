package com.example.wiseai_dev.reservation.infrastructrue.presistence.entity;

import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_meetingroom_time", columnList = "meetingRoomId, startTime, endTime")
        }
)
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
    private String reservationNo;

    @Column(nullable = false)
    private Long meetingRoomId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String bookerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private double totalAmount;

    @Version
    private long version;
}
