package ru.spbau.mit.Protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.ProtoMessage.Messages;

import java.net.DatagramPacket;
import java.util.List;

import static ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol.formProtoMessage;

public class UdpProtocol {
    public byte[] encodeArray(List<Integer> data){
        Messages.ArrayMessage msg = formProtoMessage(data);
        return msg.toByteArray();
    }

    public List<Integer> decodeArray(byte[] buf) throws InvalidProtocolBufferException {
        Messages.ArrayMessage msg = Messages.ArrayMessage.parseFrom(buf);
        return msg.getElemList();
    }
}
