package ru.spbau.mit.Protocol.ServerSide;


import java.io.*;
import java.util.List;

public interface ServerProtocol {
    List<Integer> readRequest(DataInputStream input) throws IOException;
    void sendResponse(DataOutputStream output, List<Integer> data) throws IOException;
}
