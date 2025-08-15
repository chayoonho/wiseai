package com.example.wiseai_dev.payment.infrastructure.persistence.jpa;

import com.example.wiseai_dev.payment.infrastructure.persistence.entity.PaymentProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentProviderJpaRepository extends JpaRepository<PaymentProviderEntity, Long> {
    Optional<PaymentProviderEntity> findByName(String name);
}