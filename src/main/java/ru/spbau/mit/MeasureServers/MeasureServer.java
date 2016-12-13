package ru.spbau.mit.MeasureServers;

import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServiceState;
import ru.spbau.mit.Tester.Timing.TimeLog;
import ru.spbau.mit.Tester.Timing.UidGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Always starts on port 8082, contains all the common stuff
 * <p>
 * Like all the logging things are here and the server state too
 */
public abstract class MeasureServer {
    private final TimeLog jobLog = new TimeLog();
    private final UidGenerator jobIdGen = new UidGenerator();
    public final TimeLog clientLog = new TimeLog();
    public final UidGenerator clientIdGen = new UidGenerator();

    private volatile ServiceState serverState = ServiceState.PREINIT;
    protected ServerProtocol protocol;

    public void start() throws IOException {
        if (serverState != ServiceState.PREINIT)
            return;
        startHelper();
        serverState = ServiceState.RUNNING;
    }

    public synchronized void stop() throws IOException {
        if (isStopped())
            return;
        serverState = ServiceState.STOPPED;
        stopHelper();
    }

    protected abstract void startHelper() throws IOException;

    protected abstract void stopHelper() throws IOException;

    public Job createJob(List<Integer> data) {
        return new Job(data);
    }

    public void defaultLogClient(Runnable r) {
        int id = clientIdGen.getAndIncrement();
        clientLog.logStart(id);
        r.run();
        clientLog.logEnd(id);
    }

    public long tallySortTimes() {
        if (!isStopped()) {
            throw new IllegalStateException("Can't tallyMedian jobs if I'm not stopped");
        }
        return jobLog.tallyMedian();
    }

    public long tallyRequestTimes() {
        if (!isStopped()) {
            throw new IllegalStateException("Can't tallyMedian clients if I'm not stopped");
        }
        return clientLog.tallyMedian();
    }

    protected boolean isStopped() {
        return serverState == ServiceState.STOPPED;
    }

    /**
     * The job class, once free, now lives here with his mum.
     */
    public class Job implements Callable<List<Integer>> {
        private final ArrayList<Integer> data;

        private Job(List<Integer> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public List<Integer> call() {
            int uid = jobIdGen.getAndIncrement();
            jobLog.logStart(uid);
            List<Integer> res = actualWork();
            jobLog.logEnd(uid);
            return res;
        }

        private List<Integer> actualWork() {
            for (int i = 1; i < data.size(); ++i) {
                int key = data.get(i);
                int j;
                for (j = i - 1; j >= 0 && data.get(j) > key; --j) {
                    data.set(j + 1, data.get(j));
                }
                data.set(j + 1, key);
            }
            return data;
        }
    }
}
