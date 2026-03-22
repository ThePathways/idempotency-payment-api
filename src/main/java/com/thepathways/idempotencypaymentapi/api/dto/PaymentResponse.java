package com.thepathways.idempotencypaymentapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private BigDecimal amount;
    private String currency;
    private String merchantReference;
    private PaymentStatusResponse paymentStatus;
    private LocalDateTime createdAt;
}
