package ru.spbau.mit.MeasureServers;

import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServiceState;
import ru.spbau.mit.Tester.Timing.ServerLogger;
import ru.spbau.mit.Tester.Timing.Uid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Always starts on port 8082
 */
public abstract class MeasureServer {
    private final ServerLogger jobLogger = new ServerLogger();
    private final Uid jobId = new Uid();
    public final ServerLogger clientLogger = new ServerLogger();
    public final Uid clientID = new Uid();

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
        int id = clientID.getAndIncrement();
        clientLogger.logStart(id);
        r.run();
        clientLogger.logEnd(id);
    }

    public long tallyJobs() {
        if (!isStopped()) {
            throw new IllegalStateException("Can't tally jobs if I'm not stopped");
        }
        return jobLogger.tally();
    }

    public long tallyClients() {
        if (!isStopped()) {
            throw new IllegalStateException("Can't tally clients if I'm not stopped");
        }
        return clientLogger.tally();
    }

    protected boolean isStopped() {
        return serverState == ServiceState.STOPPED;
    }

    public class Job implements Callable<List<Integer>> {
        private final ArrayList<Integer> data;

        private Job(List<Integer> data) {
            this.data = new ArrayList<>(data);
        }

        @Override
        public List<Integer> call() {
            int uid = jobId.getAndIncrement();
            jobLogger.logStart(uid);
            List<Integer> res = actualWork();
            jobLogger.logEnd(uid);
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
