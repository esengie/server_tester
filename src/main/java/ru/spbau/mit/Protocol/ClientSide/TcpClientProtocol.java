package ru.spbau.mit.Protocol.ClientSide;

import ru.spbau.mit.ProtoMessage.Messages.ArrayMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TcpClientProtocol implements ClientProtocol {

    public static ArrayMessage formProtoMessage(List<Integer> data) {
        return ArrayMessage.newBuilder()
                .addAllElem(data)
                .build();
    }

    public static ArrayMessage readProtoMessage(DataInputStream input) throws IOException {
        int size = input.readInt();
        byte[] msg = new byte[size];
        int cnt = 0;
        while (cnt != size) {
            msg[cnt] = input.readByte();
            ++cnt;
        }
        return ArrayMessage.parseFrom(msg);
    }

    @Override
    public void sendRequest(DataOutputStream output, List<Integer> data) throws IOException {
        byte[] msg = formProtoMessage(data).toByteArray();
        output.writeInt(msg.length);
        output.write(msg, 0, msg.length);
    }

    @Override
    public List<Integer> readResponse(DataInputStream input) throws IOException {
        return readProtoMessage(input).getElemList();
    }
}
