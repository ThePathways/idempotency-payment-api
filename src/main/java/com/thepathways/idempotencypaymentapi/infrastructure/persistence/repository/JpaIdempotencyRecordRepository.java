package com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository;

import com.thepathways.idempotencypaymentapi.infrastructure.persistence.entity.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaIdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, Long> {
    Optional<IdempotencyRecordEntity> findByIdempotencyKey(String idempotencyKey);
}
