package com.thepathways.idempotencypaymentapi.api.mapper;

import com.thepathways.idempotencypaymentapi.api.dto.CreatePaymentRequest;
import com.thepathways.idempotencypaymentapi.api.dto.PaymentResponse;
import com.thepathways.idempotencypaymentapi.api.dto.PaymentStatusResponse;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;
import com.thepathways.idempotencypaymentapi.domain.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentHttpMapper {

    public CreatePaymentCommand toCreatePaymentCommand(CreatePaymentRequest request) {
        return new CreatePaymentCommand(
                request.getAmount(),
                request.getCurrency(),
                request.getMerchantReference()
        );
    }

    public PaymentResponse toPaymentResponse(CreatePaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.amount(),
                result.currency(),
                result.merchantReference(),
                toPaymentStatusResponse(result.paymentStatus()),
                result.createdAt()
        );
    }

    private PaymentStatusResponse toPaymentStatusResponse(PaymentStatus paymentStatus) {
        return PaymentStatusResponse.valueOf(paymentStatus.name());
    }
}
