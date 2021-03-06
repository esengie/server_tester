package ru.spbau.mit.CreationAndConfigs;

import lombok.Builder;
import lombok.Getter;

/**
 * Contains an interval (something like [left, right) with steps)
 */
@Builder
public class IntervalWithStep {
    @Getter
    private int start = 0;
    @Getter
    private int end = 1;
    @Getter
    private int step = 1;
}
