package com.thepathways.idempotencypaymentapi.api;

import com.thepathways.idempotencypaymentapi.api.dto.CreatePaymentRequest;
import com.thepathways.idempotencypaymentapi.api.dto.PaymentResponse;
import com.thepathways.idempotencypaymentapi.api.mapper.PaymentHttpMapper;
import com.thepathways.idempotencypaymentapi.application.CreatePaymentHandler;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final CreatePaymentHandler createPaymentHandler;
    private final PaymentHttpMapper paymentHttpMapper;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentCommand command = paymentHttpMapper.toCreatePaymentCommand(request);
        CreatePaymentResult result = createPaymentHandler.createPayment(idempotencyKey, command);

        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(paymentHttpMapper.toPaymentResponse(result));
    }
}
