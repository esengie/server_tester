package ru.spbau.mit.Protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpProtocol {
    public static final int MAX_PACKET = 65508;
    private static final Logger logger = Logger.getLogger(UdpProtocol.class.getName());
    private final ClientProtocol protocol = new TcpClientProtocol();

    public byte[] encodeArray(List<Integer> data) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            protocol.sendRequest(new DataOutputStream(buf), data);
        } catch (IOException e) {
            //
        }
        return buf.toByteArray();
    }

    public List<Integer> decodeArray(byte[] buf) {
        List<Integer> res = new ArrayList<>();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);
        try {
            res = protocol.readResponse(new DataInputStream(inputStream));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "This is an error in my usage", e);
        }
        return res;
    }
}
