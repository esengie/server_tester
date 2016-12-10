package ru.spbau.mit;


import lombok.Getter;
import lombok.Setter;
import ru.spbau.mit.MeasureServers.ServerType;

public class UserConfig {
    @Getter @Setter
    private int arraySize;
    @Getter @Setter
    private int clientsSize;
    @Getter @Setter
    private long nextReqDelta;
    @Getter
    private final int requestsPerClient;
    @Getter
    private ServerType serverType;

    public UserConfig(int arraySize, int clientsSize,
                      long nextReqDelta, int requestsPerClient,
                      ServerType serverType){
        this.arraySize = arraySize;
        this.clientsSize = clientsSize;
        this.nextReqDelta = nextReqDelta;
        this.requestsPerClient = requestsPerClient;
        this.serverType = serverType;
    }
}
