package ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp;

import java.nio.channels.SocketChannel;

public class ServerDataEvent {
    public final TcpNonBlockServer server;
    public final SocketChannel socket;
    public final byte[] data;

    public ServerDataEvent(TcpNonBlockServer server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}