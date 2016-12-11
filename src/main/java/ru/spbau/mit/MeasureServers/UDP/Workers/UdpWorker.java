package ru.spbau.mit.MeasureServers.UDP.Workers;


import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.Protocol.ByteProtocol;
import ru.spbau.mit.MeasureServers.Job;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpWorker implements Runnable {
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final ByteProtocol protocol = new ByteProtocol();

    public UdpWorker(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        Job job = MeasureServer.createJob(protocol.decodeArray(packet.getData()));
        byte[] msg = protocol.encodeArray(job.call());

        DatagramPacket response = new DatagramPacket(msg, msg.length, packet.getSocketAddress());
        try {
            socket.send(response);
        } catch (IOException e) {
            //
        }
    }
}
