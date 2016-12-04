package ru.spbau.mit;

public class UserConfig {
    public final int elemSize;
    public final int clientsSize;
    public final long nextReqDelta;
    public final int requestsPerClient;
    public UserConfig(int elemSize, int clientsSize, long nextReqDelta, int requestsPerClient){
        this.elemSize = elemSize;
        this.clientsSize = clientsSize;
        this.nextReqDelta = nextReqDelta;
        this.requestsPerClient = requestsPerClient;
    }
}
