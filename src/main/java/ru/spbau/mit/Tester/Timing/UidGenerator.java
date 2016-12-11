package ru.spbau.mit.Tester.Timing;

import java.util.concurrent.atomic.AtomicInteger;

public class UidGenerator {
    private AtomicInteger id = new AtomicInteger(0);

    public int getAndIncrement() {
        return id.getAndIncrement();
    }
}
