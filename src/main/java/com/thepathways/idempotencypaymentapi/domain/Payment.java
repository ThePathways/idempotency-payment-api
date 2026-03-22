package com.thepathways.idempotencypaymentapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        BigDecimal amount,
        String currency,
        String merchantReference,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt
) {
}
