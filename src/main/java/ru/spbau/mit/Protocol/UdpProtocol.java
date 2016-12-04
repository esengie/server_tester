package ru.spbau.mit.Protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.ProtoMessage.Messages;
import sun.rmi.runtime.Log;

import java.net.DatagramPacket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol.formProtoMessage;

public class UdpProtocol {
    private static final Logger logger = Logger.getLogger(UdpProtocol.class.getName());

    public byte[] encodeArray(List<Integer> data){
        Messages.ArrayMessage msg = formProtoMessage(data);
        return msg.toByteArray();
    }

    public List<Integer> decodeArray(byte[] buf) {
        Messages.ArrayMessage msg = Messages.ArrayMessage.getDefaultInstance();
        try {
            msg = Messages.ArrayMessage.parseFrom(buf);
        } catch (InvalidProtocolBufferException e) {
            logger.log(Level.SEVERE, "This is an error in my usage", e);
        }
        return msg.getElemList();
    }
}
