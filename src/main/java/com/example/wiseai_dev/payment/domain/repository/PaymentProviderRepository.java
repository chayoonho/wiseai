package com.example.wiseai_dev.payment.domain.repository;

import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.reservation.domain.model.Reservation;

import java.util.Optional;

public interface PaymentProviderRepository {
    Optional<PaymentProvider> findByName(String paymentProviderName);
}
