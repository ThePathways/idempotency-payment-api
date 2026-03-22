package com.thepathways.idempotencypaymentapi.application.port.out;

import com.thepathways.idempotencypaymentapi.domain.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRecordPort {
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    IdempotencyRecord save(IdempotencyRecord idempotencyRecord);
}
