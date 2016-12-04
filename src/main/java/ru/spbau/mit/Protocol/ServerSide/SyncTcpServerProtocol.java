package ru.spbau.mit.Protocol.ServerSide;

import java.io.*;
import java.util.List;

import static ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol.formProtoMessage;
import static ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol.readProtoMessage;

public class SyncTcpServerProtocol implements ServerProtocol {

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
