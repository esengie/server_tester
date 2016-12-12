package ru.spbau.mit.CreationAndConfigs;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
public class UserConfig {
    @Getter
    @Setter
    private int arraySize = 1;
    @Getter
    @Setter
    private int clientsSize = 1;
    @Getter
    @Setter
    private long nextReqDelta = 100;
    @Getter
    private final int requestsPerClient;
    @Getter
    @NonNull
    private final VaryingParameter varyingParameter;
    @Getter
    @NonNull
    private final ServerType serverType;
}
