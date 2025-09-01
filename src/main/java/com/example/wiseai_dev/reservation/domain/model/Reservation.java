package com.example.wiseai_dev.reservation.domain.model;

import com.example.wiseai_dev.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class Reservation {

    private Long id;
    private Long meetingRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private User user;
    private ReservationStatus status;
    private double totalAmount;
    private Long version;

    public Reservation(Long id, Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime, User user, ReservationStatus status, double totalAmount, Long version) {
        this.id = id;
        this.meetingRoomId = meetingRoomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.status = status;
        this.totalAmount = totalAmount;
        this.version = version;
    }

    /**
     * 예약 생성 팩토리 메서드
     */
    public static Reservation create(Long meetingRoomId,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime,
                                     User user,
                                     double totalAmount,
                                     ReservationStatus status) {
        return Reservation.builder()
                .meetingRoomId(meetingRoomId)
                .startTime(startTime)
                .endTime(endTime)
                .user(user)
                .totalAmount(totalAmount)
                .status(status != null ? status : ReservationStatus.PENDING_PAYMENT)
                .version(0L)
                .build();
    }

    /**
     * User 객체 설정
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 결제 확정
     */
    public void confirmPayment() {
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 중인 예약만 확정할 수 없습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * 예약 정보 수정
     */
    public void update(LocalDateTime startTime, LocalDateTime endTime, User user, double totalAmount) {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약은 수정할 수 없습니다.");
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보는 필수입니다.");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.totalAmount = totalAmount;
    }
}
