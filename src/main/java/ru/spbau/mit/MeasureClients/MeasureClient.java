package ru.spbau.mit.MeasureClients;

import java.io.IOException;
import java.util.List;


public interface MeasureClient {
    void connect(String hostName) throws IOException;
    void disconnect() throws IOException;

    List<Integer> executeRequest(List<Integer> lst) throws IOException;
}
