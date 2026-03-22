package com.thepathways.idempotencypaymentapi.domain;

import java.time.LocalDateTime;

public record IdempotencyRecord(
        Long id,
        String idempotencyKey,
        String requestHash,
        Payment payment,
        LocalDateTime createdAt
) {
}
