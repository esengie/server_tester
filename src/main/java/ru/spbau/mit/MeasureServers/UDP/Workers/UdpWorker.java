package ru.spbau.mit.MeasureServers.UDP.Workers;


import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.UDP.UdpServer;
import ru.spbau.mit.Protocol.ByteProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(UdpServer.class.getName());

    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final MeasureServer server;
    private final ByteProtocol protocol = new ByteProtocol();
    private final int logID;

    public UdpWorker(MeasureServer server, DatagramSocket socket, DatagramPacket packet, int logID) {
        this.server = server;
        this.socket = socket;
        this.packet = packet;
        this.logID = logID;
    }

    @Override
    public void run() {
        MeasureServer.Job job = server.createJob(protocol.decodeArray(packet.getData()));
        byte[] msg = protocol.encodeArray(job.call());

        DatagramPacket response = new DatagramPacket(msg, msg.length, packet.getSocketAddress());
        try {
            socket.send(response);
        } catch (IOException e) {
            logger.log(Level.FINER, "Udp send error", e);
        }
        server.clientLogger.logEnd(logID);
    }
}
