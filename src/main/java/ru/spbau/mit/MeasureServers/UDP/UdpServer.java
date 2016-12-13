package ru.spbau.mit.MeasureServers.UDP;

import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.UDP.Workers.UdpWorker;
import ru.spbau.mit.Protocol.ByteProtocol;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All the udp server types live here
 */
public class UdpServer extends MeasureServer {
    private DatagramSocket serverSocket = null;

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private Thread serverThread = new Thread(new ServerThread());
    private ServerType type;

    public UdpServer(ServerType type) {
        switch (type) {
            case UDP_THREAD_PER_REQUEST:
            case UDP_FIXED_THREAD_POOL:
                this.type = type;
                return;
            default:
                throw new IllegalArgumentException("This is a strictly udp server");
        }
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    byte[] buffer = new byte[ByteProtocol.MAX_PACKET];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    int id = clientIdGen.getAndIncrement();
                    clientLog.logStart(id);
                    submit(packet, id);
                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                }
            }
            threadPool.shutdown();
        }
    }

    private void submit(DatagramPacket packet, int id) {
        switch (type) {
            case UDP_THREAD_PER_REQUEST:
                Thread t = new Thread(new UdpWorker(this, serverSocket, packet, id));
                t.start();
                break;
            case UDP_FIXED_THREAD_POOL:
                threadPool.execute(new UdpWorker(this, serverSocket, packet, id));
                break;
        }
    }

    @Override
    public void startHelper() throws IOException {
        serverSocket = new DatagramSocket(ProtocolConstants.SERVER_PORT);
        serverThread.start();
    }

    @Override
    public void stopHelper() throws IOException {
        serverSocket.close();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            //
        }
    }

}



