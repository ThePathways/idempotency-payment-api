package com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository;

import com.thepathways.idempotencypaymentapi.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
