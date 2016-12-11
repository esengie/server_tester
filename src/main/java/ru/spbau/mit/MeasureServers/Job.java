package ru.spbau.mit.MeasureServers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Job implements Callable<List<Integer>> {
    private final ArrayList<Integer> data;

    Job(List<Integer> data){
        this.data = new ArrayList<>(data);
    }

    @Override
    public List<Integer> call() {
        for (int i = 1; i < data.size(); ++i){
            int key = data.get(i);
            int j;
            for (j = i - 1; j >= 0 && data.get(j) > key; --j){
                data.set(j + 1, data.get(j));
            }
            data.set(j + 1, key);
        }
        return data;
    }
}
