package com.thepathways.idempotencypaymentapi.infrastructure.persistence.mapper;

import com.thepathways.idempotencypaymentapi.domain.IdempotencyRecord;
import com.thepathways.idempotencypaymentapi.domain.Payment;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.entity.IdempotencyRecordEntity;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentEntityMapper {

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity paymentEntity = new PaymentEntity(
                payment.amount(),
                payment.currency(),
                payment.merchantReference(),
                payment.paymentStatus()
        );
        paymentEntity.setId(payment.id());
        paymentEntity.setCreatedAt(payment.createdAt());
        return paymentEntity;
    }

    public IdempotencyRecordEntity toEntity(IdempotencyRecord idempotencyRecord, PaymentEntity paymentEntity) {
        IdempotencyRecordEntity recordEntity = new IdempotencyRecordEntity(
                idempotencyRecord.idempotencyKey(),
                idempotencyRecord.requestHash(),
                paymentEntity
        );
        recordEntity.setId(idempotencyRecord.id());
        recordEntity.setCreatedAt(idempotencyRecord.createdAt());
        return recordEntity;
    }

    public Payment toDomain(PaymentEntity paymentEntity) {
        return new Payment(
                paymentEntity.getId(),
                paymentEntity.getPaymentAmount(),
                paymentEntity.getCurrency(),
                paymentEntity.getMerchantReference(),
                paymentEntity.getPaymentStatus(),
                paymentEntity.getCreatedAt()
        );
    }

    public IdempotencyRecord toDomain(IdempotencyRecordEntity recordEntity) {
        return new IdempotencyRecord(
                recordEntity.getId(),
                recordEntity.getIdempotencyKey(),
                recordEntity.getRequestHash(),
                toDomain(recordEntity.getPayment()),
                recordEntity.getCreatedAt()
        );
    }
}
