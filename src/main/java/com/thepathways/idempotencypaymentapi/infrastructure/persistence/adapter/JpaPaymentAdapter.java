package com.thepathways.idempotencypaymentapi.infrastructure.persistence.adapter;

import com.thepathways.idempotencypaymentapi.application.port.out.SavePaymentPort;
import com.thepathways.idempotencypaymentapi.domain.Payment;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentAdapter implements SavePaymentPort {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public Payment save(Payment payment) {
        return paymentEntityMapper.toDomain(
                jpaPaymentRepository.save(paymentEntityMapper.toEntity(payment))
        );
    }
}
