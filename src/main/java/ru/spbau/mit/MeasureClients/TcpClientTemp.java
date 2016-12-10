package ru.spbau.mit.MeasureClients;

import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class TcpClientTemp implements MeasureClient {
    private String host = null;
    private final ClientProtocol protocol = new TcpClientProtocol();

    @Override
    public void connect(String hostName) throws IOException {
        host = hostName;
    }

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public List<Integer> executeRequest(List<Integer> lst) throws IOException {
        Socket socket = new Socket(host, ProtocolConstants.SERVER_PORT);
        protocol.sendRequest(new DataOutputStream(socket.getOutputStream()), lst);
        List<Integer> reply = protocol.readResponse(new DataInputStream(socket.getInputStream()));
        socket.close();
        return reply;
    }
}
