package com.thepathways.idempotencypaymentapi.application.dto;

import com.thepathways.idempotencypaymentapi.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePaymentResult(
        Long paymentId,
        BigDecimal amount,
        String currency,
        String merchantReference,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt,
        boolean replayed
) {
}
