package ru.spbau.mit.Tester.Timing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerLogger {
    private volatile Map<Integer, Long> starts = new ConcurrentHashMap<>();
    private volatile Map<Integer, Long> ends = new ConcurrentHashMap<>();

    public void logStart(int id){
        starts.put(id, getTime());
    }

    public void logEnd(int id){
        ends.put(id, getTime());
    }

    private static long getTime(){
        return System.currentTimeMillis();
    }

    public synchronized long tally(){
        Map<Integer, Long> startsCopy = new HashMap<>(starts);
        Map<Integer, Long> endsCopy = new HashMap<>(ends);

        starts = new ConcurrentHashMap<>();
        ends = new ConcurrentHashMap<>();

        long total = 0;
        int cnt = 0;
        for (int key : startsCopy.keySet()){
            if (endsCopy.containsKey(key)){
                total += endsCopy.get(key) - startsCopy.get(key);
                ++cnt;
            }
        }

        return total;
    }
}
