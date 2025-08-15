package com.example.wiseai_dev.payment.infrastructure.persistence.repository;

import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import com.example.wiseai_dev.payment.domain.repository.PaymentProviderRepository;
import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentProviderEntity;
import com.example.wiseai_dev.payment.infrastructure.persistence.jpa.PaymentProviderJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentProviderRepositoryImpl implements PaymentProviderRepository {

    private final PaymentProviderJpaRepository jpaRepository;

    public PaymentProviderRepositoryImpl(PaymentProviderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PaymentProvider> findByName(String paymentProviderName) {
        return jpaRepository.findByName(paymentProviderName).map(this::fromEntity);
    }

    // PaymentProviderEntity를 PaymentProvider 도메인 모델로 변환
    private PaymentProvider fromEntity(PaymentProviderEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PaymentProvider(
                entity.getId(),
                entity.getName(),
                entity.getApiEndpoint(),
                entity.getAuthInfo()
        );
    }
}
