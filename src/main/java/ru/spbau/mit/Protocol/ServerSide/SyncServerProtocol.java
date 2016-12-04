package ru.spbau.mit.Protocol.ServerSide;

import java.io.*;
import java.util.List;

import static ru.spbau.mit.Protocol.ClientSide.SyncTcpClientProtocol.formProtoMessage;
import static ru.spbau.mit.Protocol.ClientSide.SyncTcpClientProtocol.readProtoMessage;

public class SyncServerProtocol implements ServerProtocol {

    @Override
    public List<Integer> readRequest(DataInputStream input) throws IOException {
        return readProtoMessage(input).getElemList();
    }

    @Override
    public void sendResponse(DataOutputStream output, List<Integer> data) throws IOException {
        byte[] msg = formProtoMessage(data).toByteArray();
        output.writeInt(msg.length);
        output.write(msg, 0, msg.length);
    }
}
