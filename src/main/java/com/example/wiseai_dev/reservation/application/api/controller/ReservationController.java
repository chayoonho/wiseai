package com.example.wiseai_dev.reservation.application.api.controller;

import com.example.wiseai_dev.global.ApiResponse;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationRequest;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationResponse;
import com.example.wiseai_dev.reservation.application.api.dto.ReservationUpdateRequest;
import com.example.wiseai_dev.reservation.application.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "회의실 예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성", description = "새로운 회의실 예약을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "예약 단건 조회", description = "예약 ID로 특정 예약을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(@PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "예약 전체 조회", description = "모든 예약을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAll() {
        List<ReservationResponse> responses = reservationService.getReservations();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @Operation(summary = "예약 수정", description = "예약 정보를 변경합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationResponse response = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "예약 취소", description = "예약 상태를 CANCELLED로 변경합니다.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(@PathVariable Long id) {
        ReservationResponse response = reservationService.updateReservationStatusToCancelled(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
