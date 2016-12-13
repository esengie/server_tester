package ru.spbau.mit.Protocol.ClientSide;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A client part, for IO TCP sockets
 */
public interface ClientProtocol {
    void sendRequest(DataOutputStream output, List<Integer> data) throws IOException;

    List<Integer> readResponse(DataInputStream input) throws IOException;
}
