package com.example.wiseai_dev.payment.domain.model;


import com.example.wiseai_dev.meetingRoom.application.api.dto.MeetingRoomRequest;
import com.example.wiseai_dev.reservation.domain.model.Reservation;
import lombok.*;
import jakarta.persistence.Version;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private Reservation reservation;
    private PaymentProvider paymentProvider;
    private PaymentStatus status;
    private double amount;
    private String transactionId;
    @Version
    private long version;

}
