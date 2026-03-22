package com.thepathways.idempotencypaymentapi.application;

import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;

public interface CreatePaymentHandler {
    CreatePaymentResult createPayment(String idempotencyKey, CreatePaymentCommand command);
}
