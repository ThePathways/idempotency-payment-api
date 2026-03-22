package com.thepathways.idempotencypaymentapi.application;

import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;
import com.thepathways.idempotencypaymentapi.application.exception.IdempotencyConflictException;
import com.thepathways.idempotencypaymentapi.application.mapper.PaymentDomainMapper;
import com.thepathways.idempotencypaymentapi.application.port.out.IdempotencyRecordPort;
import com.thepathways.idempotencypaymentapi.application.port.out.SavePaymentPort;
import com.thepathways.idempotencypaymentapi.domain.IdempotencyRecord;
import com.thepathways.idempotencypaymentapi.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreatePaymentHandlerImpl implements CreatePaymentHandler {

    private final IdempotencyKeyLockManager idempotencyKeyLockManager;
    private final TransactionTemplate transactionTemplate;
    private final SavePaymentPort savePaymentPort;
    private final IdempotencyRecordPort idempotencyRecordPort;
    private final PaymentDomainMapper paymentDomainMapper;

    @Override
    public CreatePaymentResult createPayment(String idempotencyKey, CreatePaymentCommand command) {
        return idempotencyKeyLockManager.withLock(idempotencyKey, () -> {
            return Objects.requireNonNull(transactionTemplate.execute(transactionStatus -> {
                String requestHash = RequestHashGenerator.generateHash(command);
                Optional<IdempotencyRecord> existingRecord = idempotencyRecordPort.findByIdempotencyKey(idempotencyKey);

                if (existingRecord.isPresent()) {
                    return handleExistingRequest(existingRecord.get(), requestHash);
                }

                return handleNewRequest(idempotencyKey, command, requestHash);
            }));
        });
    }

    private CreatePaymentResult handleExistingRequest(IdempotencyRecord record, String requestHash) {
        validateRequest(record, requestHash);
        return paymentDomainMapper.toPaymentResult(record.payment(), true);
    }

    private void validateRequest(IdempotencyRecord record, String currentHash) {
        if (!record.requestHash().equals(currentHash)) {
            throw new IdempotencyConflictException(
                    "The same idempotency key was used with a different request."
            );
        }
    }

    private CreatePaymentResult handleNewRequest(String idempotencyKey, CreatePaymentCommand command, String requestHash) {
        Payment payment = paymentDomainMapper.toPayment(command);
        Payment savedPayment = savePaymentPort.save(payment);

        IdempotencyRecord idempotencyRecord =
                paymentDomainMapper.toIdempotencyRecord(idempotencyKey, requestHash, savedPayment);

        idempotencyRecordPort.save(idempotencyRecord);

        return paymentDomainMapper.toPaymentResult(savedPayment, false);
    }

}
