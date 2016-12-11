package ru.spbau.mit.Tester.ServerLauncherProtocol;

import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.ProtoMessage.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerLauncherProtocolImpl implements ServerLauncherProtocol {
    @Override
    public void startServerOnRemote(InputStream input, OutputStream output, ServerType type) throws IOException {
        Messages.ServerType.newBuilder()
                .setServerType(type.toString())
                .build()
                .writeTo(output);
        Messages.Ack.parseFrom(input);
    }

    @Override
    public void stopServerOnRemote(InputStream input, OutputStream output) throws IOException {
        startServerOnRemote(input, output, ServerType.MUTE);
    }

    @Override
    public Messages.MeasureResult getResults(InputStream input) throws IOException {
        return Messages.MeasureResult.parseFrom(input);
    }

    @Override
    public ServerType readRequest(InputStream input) throws IOException {
        Messages.ServerType type = Messages.ServerType.parseFrom(input);
        return ServerType.valueOf(type.getServerType());
    }

    @Override
    public void sendResponse(OutputStream output, long timePerJob, long timePerClient) throws IOException {
        Messages.MeasureResult.newBuilder()
                .setTimePerClient(timePerClient)
                .setTimePerJob(timePerJob)
                .build()
                .writeTo(output);
    }

    @Override
    public void sendAck(OutputStream output) throws IOException {
        Messages.Ack.newBuilder()
                .build()
                .writeTo(output);
    }
}
