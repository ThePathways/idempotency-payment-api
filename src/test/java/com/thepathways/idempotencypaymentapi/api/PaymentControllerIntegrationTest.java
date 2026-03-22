package com.thepathways.idempotencypaymentapi.api;

import com.thepathways.idempotencypaymentapi.api.dto.CreatePaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_firstRequest_returnsCreated() throws Exception {
        CreatePaymentRequest request = createRequest("10.50", "USD", "order-first");

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "first-request-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").isNumber())
                .andExpect(jsonPath("$.amount").value(10.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.merchantReference").value("order-first"))
                .andExpect(jsonPath("$.paymentStatus").value("ACCEPTED"));
    }

    @Test
    void createPayment_sameKeySamePayload_returnsReplay() throws Exception {
        CreatePaymentRequest request = createRequest("12.00", "USD", "order-replay");

        MvcResult firstResult = mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "replay-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "replay-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> firstBody = objectMapper.readValue(firstResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> secondBody = objectMapper.readValue(secondResult.getResponse().getContentAsString(), Map.class);

        assertThat(secondBody).isEqualTo(firstBody);
    }

    @Test
    void createPayment_sameKeyDifferentPayload_returnsConflict() throws Exception {
        CreatePaymentRequest originalRequest = createRequest("15.00", "USD", "order-conflict");
        CreatePaymentRequest changedRequest = createRequest("20.00", "USD", "order-conflict");

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "conflict-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "conflict-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changedRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Idempotency conflict"))
                .andExpect(jsonPath("$.detail").value("The same idempotency key was used with a different request."));
    }

    private CreatePaymentRequest createRequest(String amount, String currency, String merchantReference) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(new BigDecimal(amount));
        request.setCurrency(currency);
        request.setMerchantReference(merchantReference);
        return request;
    }
}
