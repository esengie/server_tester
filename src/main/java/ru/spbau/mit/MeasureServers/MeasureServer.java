package ru.spbau.mit.MeasureServers;

import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServiceState;

import java.io.IOException;
import java.util.List;

/**
 * Always starts on port 8082
 */
public abstract class MeasureServer {
    private volatile ServiceState serverState = ServiceState.PREINIT;
    protected ServerProtocol protocol;

    public void start() throws IOException{
        if (serverState != ServiceState.PREINIT)
            return;
        startHelper();
        serverState = ServiceState.RUNNING;
    }

    public synchronized void stop() throws IOException{
        if (isStopped())
            return;
        serverState = ServiceState.STOPPED;
        stopHelper();
    }

    protected abstract void startHelper() throws IOException;
    protected abstract void stopHelper() throws IOException;

    public static Job createJob(List<Integer> data) {
        return new Job(data);
    }
    protected boolean isStopped() {
        return serverState == ServiceState.STOPPED;
    }
}
