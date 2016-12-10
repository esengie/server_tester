package ru.spbau.mit.MeasureClients;

import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class TcpClientPerm implements MeasureClient {
    private Socket socket = null;
    private final ClientProtocol protocol = new TcpClientProtocol();

    @Override
    public void connect(String hostName) throws IOException {
        socket = new Socket(hostName, ProtocolConstants.SERVER_PORT);
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public List<Integer> executeRequest(List<Integer> lst) throws IOException {
        protocol.sendRequest(new DataOutputStream(socket.getOutputStream()), lst);
        return protocol.readResponse(new DataInputStream(socket.getInputStream()));
    }
}
