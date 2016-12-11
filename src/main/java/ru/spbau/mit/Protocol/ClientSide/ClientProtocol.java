package ru.spbau.mit.Protocol.ClientSide;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface ClientProtocol {
    void sendRequest(DataOutputStream output, List<Integer> data) throws IOException;

    List<Integer> readResponse(DataInputStream input) throws IOException;
}
