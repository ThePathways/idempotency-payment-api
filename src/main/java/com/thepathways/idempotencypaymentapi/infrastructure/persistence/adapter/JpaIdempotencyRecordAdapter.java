package com.thepathways.idempotencypaymentapi.infrastructure.persistence.adapter;

import com.thepathways.idempotencypaymentapi.application.port.out.IdempotencyRecordPort;
import com.thepathways.idempotencypaymentapi.domain.IdempotencyRecord;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.entity.PaymentEntity;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository.JpaIdempotencyRecordRepository;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaIdempotencyRecordAdapter implements IdempotencyRecordPort {

    private final JpaIdempotencyRecordRepository jpaIdempotencyRecordRepository;
    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey) {
        return jpaIdempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentEntityMapper::toDomain);
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord idempotencyRecord) {
        PaymentEntity paymentEntity = jpaPaymentRepository.getReferenceById(idempotencyRecord.payment().id());
        return paymentEntityMapper.toDomain(
                jpaIdempotencyRecordRepository.save(
                        paymentEntityMapper.toEntity(idempotencyRecord, paymentEntity)
                )
        );
    }
}
