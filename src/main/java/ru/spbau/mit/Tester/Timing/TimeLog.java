package ru.spbau.mit.Tester.Timing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TimeLog {
    private volatile Map<Integer, Long> starts = new ConcurrentHashMap<>();
    private volatile Map<Integer, Long> ends = new ConcurrentHashMap<>();

    public void logStart(int id) {
        starts.put(id, getTime());
    }

    public void logEnd(int id) {
        ends.put(id, getTime());
    }

    private static long getTime() {
        return System.nanoTime();
    }

    public long tallyMedian() {
        return tally(false);
    }

    public long tallyMean() {
        return tally(true);
    }

    private synchronized long tally(boolean tallyMean) {
        Map<Integer, Long> startsCopy = new HashMap<>(starts);
        Map<Integer, Long> endsCopy = new HashMap<>(ends);

        starts = new ConcurrentHashMap<>();
        ends = new ConcurrentHashMap<>();

        List<Long> results = new ArrayList<>();
        long total = 0;
        int cnt = 0;
        for (int key : startsCopy.keySet()) {
            if (endsCopy.containsKey(key)) {
                results.add(endsCopy.get(key) - startsCopy.get(key));
                total += endsCopy.get(key) - startsCopy.get(key);
                ++cnt;
            }
        }
        if (cnt == 0)
            return -1;
        Collections.sort(results);

        if (tallyMean)
            return total / cnt;
        else
            return results.get((results.size()) / 2);
    }
}
