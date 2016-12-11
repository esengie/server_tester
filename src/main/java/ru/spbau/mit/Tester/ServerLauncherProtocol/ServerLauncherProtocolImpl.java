package ru.spbau.mit.Tester.ServerLauncherProtocol;

import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.ProtoMessage.Messages;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerLauncherProtocolImpl implements ServerLauncherProtocol {
    @Override
    public void startServerOnRemote(InputStream input, OutputStream output, ServerType type) throws IOException {
        Messages.ServerType.newBuilder()
                .setServerType(type.toString())
                .build()
                .writeDelimitedTo(output);
        Messages.Ack.parseDelimitedFrom(input);
    }

    @Override
    public void stopServerOnRemote(InputStream input, OutputStream output) throws IOException {
        startServerOnRemote(input, output, ServerType.MUTE);
    }

    @Override
    public RunResults getResults(InputStream input) throws IOException {
        Messages.MeasureResult res = Messages.MeasureResult.parseDelimitedFrom(input);
        return RunResults.builder()
                .perSort(res.getSortTime())
                .perRequest(res.getRequestTime())
                .build();
    }

    @Override
    public ServerType readRequest(InputStream input) throws IOException {
        Messages.ServerType type = Messages.ServerType.parseDelimitedFrom(input);
        if (type == null)
            throw new EOFException("Can't have null as serverType");
        return ServerType.valueOf(type.getServerType());
    }

    @Override
    public void sendResponse(OutputStream output, long timeSort, long timeCompleteRequest) throws IOException {
        Messages.MeasureResult.newBuilder()
                .setRequestTime(timeCompleteRequest)
                .setSortTime(timeSort)
                .build()
                .writeDelimitedTo(output);
    }

    @Override
    public void sendAck(OutputStream output) throws IOException {
        Messages.Ack.newBuilder()
                .build()
                .writeDelimitedTo(output);
    }
}
