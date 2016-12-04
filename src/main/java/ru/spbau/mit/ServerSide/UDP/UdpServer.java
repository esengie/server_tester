package ru.spbau.mit.ServerSide.UDP;

import ru.spbau.mit.Protocol.ProtocolConstants;
import ru.spbau.mit.ServerSide.ServerType;
import ru.spbau.mit.ServerSide.Servers;
import ru.spbau.mit.ServerSide.TCP.TcpPermWorker;
import ru.spbau.mit.ServerSide.TCP.TcpTempWorker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All the udp server types except for async one
 */
public class UdpServer extends Servers {
    private DatagramSocket serverSocket = null;

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private Thread serverThread = new Thread(new ServerThread());
    private ServerType type;

    public UdpServer(ServerType type) {
        switch (type){
            case UDP_THREAD_PER_REQUEST:
            case UDP_FIXED_THREAD_POOL:
            case UDP_ASYNC:
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
                    byte[] buffer = new byte[65508];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    submit(packet);
                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                }
            }
            threadPool.shutdownNow();
        }
    }

    private void submit(DatagramPacket packet) {
        switch (type){
            case UDP_THREAD_PER_REQUEST:
                Thread t = new Thread(new UdpWorkerSimple(serverSocket, packet));
                t.start();
                break;
            case UDP_FIXED_THREAD_POOL:
                threadPool.execute(new UdpWorkerSimple(serverSocket, packet));
                break;
            case UDP_ASYNC:
//                new UdpWorkerAsync();
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
    }

}



