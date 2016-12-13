package ru.spbau.mit.CreationAndConfigs;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A user config class, contains the parameters for testing architectures
 */
@Builder
public class UserConfig implements Cloneable {
    @Getter
    private int arraySize = 1;
    @Getter
    private int clientsSize = 1;
    @Getter
    private int nextReqDelta = 100;
    @Getter
    private final int requestsPerClient;
    @Getter
    @NonNull
    private final VaryingParameter varyingParameter;
    @Getter
    @NonNull
    private final ServerType serverType;

    public void addToVarying(int difference) {
        switch (varyingParameter) {
            case ELEMENTS_PER_REQ:
                arraySize += difference;
                break;
            case CLIENTS_PARALLEL:
                clientsSize += difference;
                break;
            case TIME_DELTA:
                nextReqDelta += difference;
                break;
        }
        checkConsistency();
    }

    public void checkConsistency() {
        if (!(nextReqDelta >= 0 && requestsPerClient > 0 && clientsSize > 0 && arraySize > 0)) {
            throw new IllegalStateException("Can't have the config in the negatives");
        }
    }

    public int getVarying() {
        switch (varyingParameter) {
            case ELEMENTS_PER_REQ:
                return arraySize;
            case CLIENTS_PARALLEL:
                return clientsSize;
            case TIME_DELTA:
                return nextReqDelta;
        }
        return 0;
    }

    public void setVarying(int val) {
        addToVarying(val - getVarying());
    }

    @Override
    public UserConfig clone() {
        try {
            return (UserConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            //
        }
        return null;
    }
}
