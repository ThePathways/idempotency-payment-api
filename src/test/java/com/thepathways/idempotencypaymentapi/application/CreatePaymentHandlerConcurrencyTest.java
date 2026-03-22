package com.thepathways.idempotencypaymentapi.application;

import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentCommand;
import com.thepathways.idempotencypaymentapi.application.dto.CreatePaymentResult;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository.JpaIdempotencyRecordRepository;
import com.thepathways.idempotencypaymentapi.infrastructure.persistence.repository.JpaPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CreatePaymentHandlerConcurrencyTest {

    @Autowired
    private CreatePaymentHandler createPaymentHandler;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private JpaIdempotencyRecordRepository jpaIdempotencyRecordRepository;

    @BeforeEach
    void setUp() {
        jpaIdempotencyRecordRepository.deleteAll();
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void createPayment_sameKeyConcurrentCalls_createOnePaymentAndReplayTheRest() throws Exception {
        String idempotencyKey = "concurrent-key";
        CreatePaymentCommand command = new CreatePaymentCommand(
                new BigDecimal("30.00"),
                "USD",
                "order-concurrent"
        );

        int threadCount = 4;
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<CreatePaymentResult>> futures = new ArrayList<>();

        Callable<CreatePaymentResult> task = () -> {
            readyLatch.countDown();
            startLatch.await();
            return createPaymentHandler.createPayment(idempotencyKey, command);
        };

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(task));
        }

        readyLatch.await();
        startLatch.countDown();

        List<CreatePaymentResult> results = new ArrayList<>();
        for (Future<CreatePaymentResult> future : futures) {
            results.add(future.get());
        }

        executorService.shutdownNow();

        long replayedCount = results.stream().filter(CreatePaymentResult::replayed).count();
        long createdCount = results.stream().filter(result -> !result.replayed()).count();
        long distinctPaymentIds = results.stream().map(CreatePaymentResult::paymentId).distinct().count();

        assertThat(createdCount).isEqualTo(1);
        assertThat(replayedCount).isEqualTo(threadCount - 1);
        assertThat(distinctPaymentIds).isEqualTo(1);
        assertThat(jpaPaymentRepository.count()).isEqualTo(1);
        assertThat(jpaIdempotencyRecordRepository.count()).isEqualTo(1);
    }
}
