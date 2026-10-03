package com.examplatform.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_FAILS = 10;
    private static final long WINDOW_MS = 10 * 60_000L;
    private static final long LOCK_MS = 5 * 60_000L;

    private static class Entry {
        final Deque<Long> fails = new ArrayDeque<>();
        long lockedUntil = 0;
    }

    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Entry e = map.get(key);
        if (e == null) return;
        long remainingMs;
        synchronized (e) {
            remainingMs = e.lockedUntil - System.currentTimeMillis();
        }
        if (remainingMs > 0) {
            long min = Math.max(1, (remainingMs + 59_999) / 60_000);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "অনেকবার ভুল চেষ্টা হয়েছে। " + min + " মিনিট পরে আবার চেষ্টা করুন।");
        }
    }

    public void recordFailure(String key) {
        long now = System.currentTimeMillis();
        Entry e = map.computeIfAbsent(key, k -> new Entry());
        synchronized (e) {
            while (!e.fails.isEmpty() && now - e.fails.peekFirst() > WINDOW_MS) {
                e.fails.pollFirst();
            }
            e.fails.addLast(now);
            if (e.fails.size() >= MAX_FAILS) {
                e.lockedUntil = now + LOCK_MS;
                e.fails.clear();
            }
        }
    }

    public void recordSuccess(String key) {
        map.remove(key);
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        map.entrySet().removeIf(en -> {
            Entry e = en.getValue();
            synchronized (e) {
                while (!e.fails.isEmpty() && now - e.fails.peekFirst() > WINDOW_MS) {
                    e.fails.pollFirst();
                }
                return e.fails.isEmpty() && e.lockedUntil < now;
            }
        });
    }
}
