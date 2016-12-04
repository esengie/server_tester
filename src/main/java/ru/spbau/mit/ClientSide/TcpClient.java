package ru.spbau.mit.ClientSide;

import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class TcpClient implements Clients {
    private Socket socket = null;
    private String host = null;
    private final ClientProtocol protocol = new TcpClientProtocol();

    @Override
    public void connect(String hostName) throws IOException {
        host = hostName;
        socket = new Socket(host, ProtocolConstants.SERVER_PORT);
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public List<Integer> executeRequest(List<Integer> lst) throws IOException {
        try {
            protocol.sendRequest(new DataOutputStream(socket.getOutputStream()), lst);
        } catch (SocketException e) {
            connect(host);
            protocol.sendRequest(new DataOutputStream(socket.getOutputStream()), lst);
        }
        return protocol.readResponse(new DataInputStream(socket.getInputStream()));
    }
}
