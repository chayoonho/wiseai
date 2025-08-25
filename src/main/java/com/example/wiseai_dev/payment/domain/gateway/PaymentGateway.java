package com.example.wiseai_dev.payment.domain.gateway;

import com.example.wiseai_dev.payment.domain.model.Payment;
import com.example.wiseai_dev.payment.domain.model.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.Map;

// 모든 결제사가 준수해야 할 공통 인터페이스
@Component
public interface PaymentGateway {
    PaymentResult processPayment(Payment payment);
    void processWebhook(Map<String, Object> webhookData);
}
