package ru.spbau.mit.TorrentServer;

import ru.spbau.mit.Protocol.ProtocolConstants;
import ru.spbau.mit.Protocol.Server.InetSocketAddressComparator;
import ru.spbau.mit.Protocol.Server.ServerProtocol;
import ru.spbau.mit.Protocol.Server.ServerProtocolImpl;
import ru.spbau.mit.Protocol.ServiceState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TorrentServerImpl implements TorrentServer {

    private static final Logger logger = Logger.getLogger(TorrentServerImpl.class.getName());

    public static final int PORT_NUMBER = 8081;
    private ServerSocket serverSocket = null;
    private volatile ServiceState serverState = ServiceState.PREINIT;

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private Thread serverThread = new Thread(new ServerThread());
    private Thread garbageCollectorThread = new Thread(new GarbageCollectorThread());


    private ServerProtocol protocol;
    private SortedMap<InetSocketAddress, Timestamp> timeToLive =
            new ConcurrentSkipListMap<>(InetSocketAddressComparator::compare);

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new WorkerRunnable(clientSocket, getNow()));
                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                    logger.log(Level.SEVERE, "Couldn't accept a client", e);
                }
            }
            threadPool.shutdownNow();
        }
    }

    private class GarbageCollectorThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    Set<InetSocketAddress> removed = new TreeSet<>(InetSocketAddressComparator::compare);
                    removed.addAll(
                            timeToLive.entrySet()
                                    .stream()
                                    .filter(entry -> checkElapsed(entry.getValue()) &&
                                            timeToLive.remove(entry.getKey(), entry.getValue()))
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList()));
                    protocol.removeExtras(removed);
                    Thread.sleep(ProtocolConstants.SERVER_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    logger.log(Level.FINE, "Interrupted the garbage collector", e);
                }
            }
        }
    }


    public boolean isStopped() {
        return serverState == ServiceState.STOPPED;
    }

    @Override
    public void start(File saveDir) throws IOException {
        if (serverState != ServiceState.PREINIT)
            return;

            protocol = new ServerProtocolImpl(saveDir);
        openServerSocket();
        serverThread.start();
        garbageCollectorThread.start();
        serverState = ServiceState.RUNNING;
    }

    public synchronized void stop() throws IOException {
        if (isStopped())
            return;

        serverState = ServiceState.STOPPED;
            serverSocket.close();
            garbageCollectorThread.interrupt();
            protocol.saveState();
    }

    private void openServerSocket() throws IOException {
            serverSocket = new ServerSocket(TorrentServerImpl.PORT_NUMBER);
    }

    private static Timestamp getNow() {
        return new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    private static boolean checkElapsed(Timestamp time) {
        long diff = getNow().getTime() - time.getTime();
        return diff > ProtocolConstants.SERVER_TIMEOUT_MILLIS;
    }

    private class WorkerRunnable implements Runnable {
        Logger logger = Logger.getLogger(WorkerRunnable.class.getName());

        private Socket clientSocket;
        private Timestamp time;
        private DataOutputStream netOut;
        private DataInputStream netIn;

        WorkerRunnable(Socket clientSocket, Timestamp time) {
            this.clientSocket = clientSocket;
            this.time = time;
        }

        public void run() {
            try {
                netOut = new DataOutputStream(clientSocket.getOutputStream());
                netIn = new DataInputStream(clientSocket.getInputStream());
                int port = protocol.formResponse(netIn, netOut, clientSocket.getInetAddress());
                netIn.close();
                if (port > 0) {
                    timeToLive.put(
                            new InetSocketAddress(clientSocket.getInetAddress(), port),
                            time);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "TorrentClient handler error", e);
            }
        }
    }
}



