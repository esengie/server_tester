package ru.spbau.mit.Tester.Timing;

import lombok.Builder;

/**
 * The results of each test run are stored in this object
 */
@Builder
public class RunResults {
    public final long perClient;
    public final long perSort;
    public final long perRequest;
}
