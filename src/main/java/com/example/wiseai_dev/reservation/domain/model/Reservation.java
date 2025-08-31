package com.example.wiseai_dev.reservation.domain.model;

import com.example.wiseai_dev.user.domain.model.User;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    private Long id;

    @NotNull(message = "회의실 ID는 필수입니다.")
    private Long meetingRoomId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // User 객체 직접 참조 (userId 대신)
    @NotNull(message = "예약자는 필수입니다.")
    private User user;

    private ReservationStatus status;
    private double totalAmount;

    @Version
    private long version;

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
                .status(status != null ? status : ReservationStatus.PENDING_PAYMENT) // 기본값
                .version(0)
                .build();
    }

    /**
     * 예약 취소
     */
    public void cancel() {
        if (this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELLED;
        } else {
            throw new IllegalStateException("확정된 예약만 취소할 수 있습니다.");
        }
    }

    /**
     * 예약 정보 수정
     */
    public void update(LocalDateTime startTime, LocalDateTime endTime, User user, double totalAmount) {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약은 수정할 수 없습니다.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.totalAmount = totalAmount;
    }

    /**
     * 결제 확정
     */
    public void confirmPayment() {
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 중인 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }
}
