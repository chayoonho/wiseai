package com.example.wiseai_dev.reservation.domain.model;

import com.example.wiseai_dev.reservation.application.api.dto.ReservationUpdateRequest;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class Reservation {
    private Long id;
    private String reservationNo;
    private Long meetingRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookerName;
    private ReservationStatus status;
    private double totalAmount;
    @Version
    private long version;

    public Reservation(Long id,
                       String  reservationNo,
                       @NotNull(message = "회의실 ID는 필수입니다.") Long meetingRoomId,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       @NotNull(message = "예약자 이름은 필수입니다.") String bookerName,
                       ReservationStatus reservationStatus,
                       double totalAmount)  {
        this.id = id;
        this.meetingRoomId = meetingRoomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookerName = bookerName;
        this.totalAmount = totalAmount;
        this.status = ReservationStatus.PENDING_PAYMENT; // 초기 상태는 항상 '결제 대기'
        this.version = 0;
    }

    public Reservation(
            Long meetingRoomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String bookerName,
            ReservationStatus status,
            double totalAmount
    ) {
        this.meetingRoomId = meetingRoomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookerName = bookerName;
        this.status = status;
        this.totalAmount = totalAmount;
    }

//    public Reservation(Long id,
//                       @NotNull(message = "회의실 ID는 필수입니다.") Long meetingRoomId,
//                       LocalDateTime startTime,
//                       LocalDateTime endTime,
//                       @NotNull(message = "예약자 이름은 필수입니다.") String bookerName,
//                       ReservationStatus reservationStatus,
//                       double totalAmount) {}

    public Reservation(Long id, String reservationNo, Long meetingRoomId, LocalDateTime startTime, LocalDateTime endTime, String bookerName, ReservationStatus status, double totalAmount, long version) {
    }


    public void cancel() {
        if (this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELLED;
        } else {
            throw new IllegalStateException("확정된 예약만 취소할 수 있습니다.");
        }
    }

    public void update(LocalDateTime startTime, LocalDateTime endTime, String bookerName, double totalAmount) {
        // 예약 상태가 '확정'일 때만 수정 가능
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약은 수정할 수 없습니다.");
        }

        this.startTime = startTime;
        this.endTime = endTime;
        this.bookerName = bookerName;
        this.totalAmount = totalAmount;
    }

    public void confirmPayment() {
        if (this.status != ReservationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 중인 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }
}