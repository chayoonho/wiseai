package com.example.wiseai_dev.payment.infrastructure.persistence.entity;

import com.example.wiseai_dev.payment.domain.model.PaymentProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_providers")
@Getter
@Setter
@NoArgsConstructor
public class PaymentProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String apiEndpoint;
    private String authInfo;

    public PaymentProvider toDomainModel() {
        return PaymentProvider.builder()
                .id(this.id)
                .name(this.name)
                .apiEndpoint(this.apiEndpoint)
                .authInfo(this.authInfo)
                .build();
    }
}