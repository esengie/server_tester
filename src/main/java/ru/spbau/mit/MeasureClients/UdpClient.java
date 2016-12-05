package ru.spbau.mit.MeasureClients;

import ru.spbau.mit.Protocol.ProtocolConstants;
import ru.spbau.mit.Protocol.UdpProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

public class UdpClient implements MeasureClient {
    private DatagramSocket socket = null;
    private SocketAddress serverAddress = null;
    private final UdpProtocol protocol = new UdpProtocol();

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
        socket.receive(packet);

        return protocol.decodeArray(packet.getData());
    }
}
