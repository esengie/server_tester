package ru.spbau.mit.TorrentClient;

import ru.spbau.mit.Protocol.Client.SeedProtocol;
import ru.spbau.mit.Protocol.Client.SeedProtocolImpl;
import ru.spbau.mit.Protocol.ServiceState;
import ru.spbau.mit.TorrentClient.TorrentFile.FileManager;
import ru.spbau.mit.TorrentServer.TorrentIOException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Does the job of a seed, basically implements the SeedProtocol
 */
class TorrentSeed {
    private static final Logger logger = Logger.getLogger(TorrentSeed.class.getName());

    private ServerSocket serverSocket = null;
    private volatile ServiceState clientState = ServiceState.PREINIT;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    private SeedProtocol protocol;
    private short seedPort;
    private final FileManager fileManager;
    private InetSocketAddress mySocketAddress;

    TorrentSeed(short port, FileManager fileManager) {
        seedPort = port;
        this.fileManager = fileManager;
        mySocketAddress = new InetSocketAddress("127.0.0.1", seedPort);
    }

    InetSocketAddress getMySocketAddress() {
        return mySocketAddress;
    }

    private class SeedThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new WorkerRunnable(clientSocket));
                } catch (IOException e) {
                    if (isStopped()) {
                        continue;
                    }
                    logger.log(Level.SEVERE, "Couldn't accept a leech", e);
                }
            }
            threadPool.shutdown();
        }
    }

    private boolean isStopped() {
        return clientState == ServiceState.STOPPED;
    }

    void start() throws TorrentIOException {
        if (clientState != ServiceState.PREINIT)
            return;
        clientState = ServiceState.RUNNING;
        protocol = new SeedProtocolImpl();
        openServerSocket();
        Thread serverThread = new Thread(new SeedThread());
        serverThread.start();
    }

    synchronized void stop() throws TorrentIOException {
        if (isStopped())
            return;
        clientState = ServiceState.STOPPED;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new TorrentIOException("Error closing server", e);
        }
    }

    private void openServerSocket() throws TorrentIOException {
        try {
            this.serverSocket = new ServerSocket(seedPort);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot open port " + String.valueOf(seedPort), e);
        }
    }

    private class WorkerRunnable implements Runnable {
        Logger logger = Logger.getLogger(TorrentSeed.class.getName());

        private Socket clientSocket;
        private DataOutputStream netOut;
        private DataInputStream netIn;

        WorkerRunnable(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                netOut = new DataOutputStream(clientSocket.getOutputStream());
                netIn = new DataInputStream(clientSocket.getInputStream());
                protocol.formResponse(netIn, netOut, fileManager);
                netOut.close();
                netIn.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "TorrentClient handler error", e);
            }
        }
    }
}



