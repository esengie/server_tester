package ru.spbau.mit.Tester.Protocol;

import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.ProtoMessage.Messages;

import java.io.*;

interface MeasureProtocol {
    void startServerOnRemote(InputStream input, OutputStream output, ServerType type) throws IOException;
    void stopServerOnRemote(InputStream input, OutputStream output) throws IOException;

    Messages.MeasureResult getResults(InputStream input) throws IOException;

    ServerType readRequest(InputStream input) throws IOException;
    void sendResponse(OutputStream output, long timePerJob, long timePerClient) throws IOException;
    void sendAck(OutputStream output) throws IOException;
}
