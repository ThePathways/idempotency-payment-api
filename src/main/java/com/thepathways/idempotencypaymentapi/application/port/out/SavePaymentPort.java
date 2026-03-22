package com.thepathways.idempotencypaymentapi.application.port.out;

import com.thepathways.idempotencypaymentapi.domain.Payment;

public interface SavePaymentPort {
    Payment save(Payment payment);
}
