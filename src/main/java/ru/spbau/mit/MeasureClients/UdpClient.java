package ru.spbau.mit.MeasureClients;

import ru.spbau.mit.Protocol.ByteProtocol;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class UdpClient implements MeasureClient {
    private DatagramSocket socket = null;
    private SocketAddress serverAddress = null;
    private final ByteProtocol protocol = new ByteProtocol();

    @Override
    public void connect(String hostName) throws IOException {
        serverAddress = new InetSocketAddress(hostName, ProtocolConstants.SERVER_PORT);
        socket = new DatagramSocket();
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public List<Integer> executeRequest(List<Integer> lst) throws IOException {
        byte[] buf = protocol.encodeArray(lst);

        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress);
        socket.send(packet);

        packet = new DatagramPacket(buf, buf.length);
        socket.setSoTimeout(10);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e){
            // packet was dropped
            return new ArrayList<>();
        }

        return protocol.decodeArray(packet.getData());
    }
}
