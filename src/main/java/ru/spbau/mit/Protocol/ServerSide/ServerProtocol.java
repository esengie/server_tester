package ru.spbau.mit.Protocol.ServerSide;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A server part, for IO TCP sockets
 */
public interface ServerProtocol {
    List<Integer> readRequest(DataInputStream input) throws IOException;

    void sendResponse(DataOutputStream output, List<Integer> data) throws IOException;
}
