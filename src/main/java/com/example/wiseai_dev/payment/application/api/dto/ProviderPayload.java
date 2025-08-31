package com.example.wiseai_dev.payment.application.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "PG사 Webhook 요청 DTO")
public class ProviderPayload {

    @Schema(description = "거래 ID (PG사에서 내려주는 결제 고유번호)",
            example = "CARD_a1b2c3d4")
    private String transactionId;

    @Schema(description = "결제 상태",
            example = "SUCCESS",
            allowableValues = {"PENDING_PAYMENT", "SUCCESS", "FAILED", "CANCELLED"})
    private String status;

    @Schema(description = "결제 금액 (원)",
            example = "30000")
    private double amount;

    @Schema(description = "주문 번호 (내부 예약번호 등)",
            example = "ORDER-20250901-0001")
    private String orderNo;

    @Schema(description = "추가 데이터 (가상계좌번호, 카드사명 등 PG사별 부가정보)",
            example = "{\"bank\":\"신한은행\", \"account\":\"123-456-789012\"}")
    private String extra;
}
