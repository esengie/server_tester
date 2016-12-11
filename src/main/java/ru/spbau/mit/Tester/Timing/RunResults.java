package ru.spbau.mit.Tester.Timing;

import lombok.Builder;

@Builder
public class RunResults {
    public final long perClient;
    public final long perSort;
    public final long perRequest;
}
