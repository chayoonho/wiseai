package com.example.wiseai_dev.reservation.application.api.dto;

import com.example.wiseai_dev.reservation.domain.model.Reservation;
import com.example.wiseai_dev.reservation.domain.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(
        name = "ReservationResponse",
        description = "회의실 예약 응답 DTO",
        example = "{\n" +
                "  \"id\": 1001,\n" +
                "  \"reservationNo\": \"RES-1001\",\n" +
                "  \"meetingRoomId\": 1,\n" +
                "  \"startTime\": \"2025-08-21T21:00:00\",\n" +
                "  \"endTime\": \"2025-08-21T22:00:00\",\n" +
                "  \"bookerName\": \"홍길동\",\n" +
                "  \"status\": \"CONFIRMED\",\n" +
                "  \"totalAmount\": 30000\n" +
                "}"
)
public class ReservationResponse {

    @Schema(description = "예약 ID", example = "1001")
    private Long id;

    @Schema(description = "예약 번호", example = "RES-1001")
    private String reservationNo;

    @Schema(description = "회의실 ID", example = "1")
    private Long meetingRoomId;

    @Schema(description = "예약 시작 시간", example = "2025-08-18T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "예약 종료 시간", example = "2025-08-18T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "예약자 이름", example = "홍길동")
    private String bookerName;

    @Schema(
            description = "예약 상태",
            example = "CONFIRMED",
            allowableValues = {"PENDING_PAYMENT", "CONFIRMED", "CANCELLED", "EXPIRED"}
    )
    private ReservationStatus status;

    @Schema(description = "총 결제 금액 (원)", example = "30000")
    private double totalAmount;

    /**
     * Domain → Response 변환
     */
    public static ReservationResponse fromDomain(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setReservationNo("RES-" + reservation.getId()); // 예약번호 생성 규칙
        response.setMeetingRoomId(reservation.getMeetingRoomId());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());

        if (reservation.getUser() != null) {
            response.setBookerName(reservation.getUser().getName());
        }

        response.setStatus(reservation.getStatus());
        response.setTotalAmount(reservation.getTotalAmount());
        return response;
    }
}
