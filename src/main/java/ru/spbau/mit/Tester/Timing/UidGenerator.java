package ru.spbau.mit.Tester.Timing;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple lock-free uid generator
 */
public class UidGenerator {
    private final AtomicInteger id = new AtomicInteger(0);

    public int getAndIncrement() {
        return id.getAndIncrement();
    }
}
