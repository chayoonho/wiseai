package com.example.wiseai_dev.reservation.infrastructrue.presistence.entity;

import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.example.wiseai_dev.user.infrastructure.persistence.entity.UserEntity;
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

    @Column(nullable = false)
    private Long meetingRoomId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private double totalAmount;

    @Version
    private long version;

    public ReservationEntity(Long id, Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime, String userId, ReservationStatus status, double totalAmount, long version) {
    }
}
