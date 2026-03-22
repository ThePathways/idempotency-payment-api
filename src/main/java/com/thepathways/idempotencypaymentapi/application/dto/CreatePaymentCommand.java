package com.thepathways.idempotencypaymentapi.application.dto;

import java.math.BigDecimal;

public record CreatePaymentCommand(
        BigDecimal amount,
        String currency,
        String merchantReference
) {
}
