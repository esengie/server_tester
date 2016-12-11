package ru.spbau.mit.MeasureServers.UDP.Workers;


import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.Protocol.ByteProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpWorker implements Runnable {
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final MeasureServer server;
    private final ByteProtocol protocol = new ByteProtocol();

    public UdpWorker(MeasureServer server, DatagramSocket socket, DatagramPacket packet) {
        this.server = server;
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        MeasureServer.Job job = server.createJob(protocol.decodeArray(packet.getData()));
        byte[] msg = protocol.encodeArray(job.call());

        DatagramPacket response = new DatagramPacket(msg, msg.length, packet.getSocketAddress());
        try {
            socket.send(response);
        } catch (IOException e) {
            //
        }
    }
}
