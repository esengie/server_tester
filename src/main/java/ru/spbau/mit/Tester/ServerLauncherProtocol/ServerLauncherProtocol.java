package ru.spbau.mit.Tester.ServerLauncherProtocol;

import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.ProtoMessage.Messages;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.*;

/**
 * Protocol for talking to remote server launcher.
 *
 * We send it the server type and wait for ack - means the server has started.
 *
 * Then we send stopServer, again wait for ack. Then the server sends us
 * the data - two medians of measurements results.
 *
 * Then we can loop again.
 */
public interface ServerLauncherProtocol {
    void startServerOnRemote(InputStream input, OutputStream output, ServerType type) throws IOException;
    void stopServerOnRemote(InputStream input, OutputStream output) throws IOException;

    RunResults getResults(InputStream input) throws IOException;

    ServerType readRequest(InputStream input) throws IOException;
    void sendResponse(OutputStream output, long timePerJob, long timePerClient) throws IOException;
    void sendAck(OutputStream output) throws IOException;
}