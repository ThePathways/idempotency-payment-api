package com.thepathways.idempotencypaymentapi.application;

import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class RequestHashGenerator {

    private RequestHashGenerator() {
    }

    public static String generateHash(CreatePaymentCommand command) {
        String rawValue =
                command.amount().toPlainString() + "|" +
                command.currency().trim().toUpperCase() + "|" +
                command.merchantReference().trim();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate request hash", e);
        }
    }
}
