package com.thepathways.idempotencypaymentapi.application.mapper;

import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;
import com.thepathways.idempotencypaymentapi.domain.IdempotencyRecord;
import com.thepathways.idempotencypaymentapi.domain.Payment;
import com.thepathways.idempotencypaymentapi.domain.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentDomainMapper {

    public Payment toPayment(CreatePaymentCommand command) {
        return new Payment(
                null,
                command.amount(),
                normalizeCurrency(command.currency()),
                normalizeMerchantReference(command.merchantReference()),
                PaymentStatus.ACCEPTED,
                null
        );
    }

    public IdempotencyRecord toIdempotencyRecord(String idempotencyKey, String requestHash, Payment payment) {
        return new IdempotencyRecord(null, idempotencyKey, requestHash, payment, null);
    }

    public CreatePaymentResult toPaymentResult(Payment payment, boolean replayed) {
        return new CreatePaymentResult(
                payment.id(),
                payment.amount(),
                payment.currency(),
                payment.merchantReference(),
                payment.paymentStatus(),
                payment.createdAt(),
                replayed
        );
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase();
    }

    private String normalizeMerchantReference(String merchantReference) {
        return merchantReference.trim();
    }
}
