package com.thepathways.idempotencypaymentapi.application;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class IdempotencyKeyLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(String idempotencyKey, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(idempotencyKey, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(idempotencyKey, lock);
            }
        }
    }
}
