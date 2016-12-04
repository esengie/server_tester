package ru.spbau.mit.ClientSide;

import java.io.IOException;
import java.util.List;


public interface Clients {
    void connect(String hostName) throws IOException;
    void disconnect() throws IOException;

    List<Integer> executeRequest() throws IOException;
}
