package com.example.wiseai_dev.payment.domain.model;

public enum PaymentStatus {
    PENDING,   // 결제 대기
    SUCCESS,   // 결제 성공 (결제사 승인 완료)
    FAILED,    // 결제 실패
    CANCELED   // 결제 취소
}