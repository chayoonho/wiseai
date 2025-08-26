package com.example.wiseai_dev.payment.application.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "PG사 Webhook 요청 DTO")
public class ProviderPayload {

    @Schema(description = "거래 ID", example = "TXN-20250825-0001")
    private String transactionId;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private String status;

    @Schema(description = "결제 금액", example = "30000")
    private double amount;
}
