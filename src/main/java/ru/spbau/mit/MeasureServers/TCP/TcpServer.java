package ru.spbau.mit.MeasureServers.TCP;

import ru.spbau.mit.MeasureServers.TCP.Workers.TcpPermWorker;
import ru.spbau.mit.MeasureServers.TCP.Workers.TcpTempWorker;
import ru.spbau.mit.Protocol.ProtocolConstants;
import ru.spbau.mit.MeasureServers.ServerType;
import ru.spbau.mit.MeasureServers.MeasureServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All the tcp server types except for nonblocking one
 */
public class TcpServer extends MeasureServer {
    private ServerSocket serverSocket = null;

    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Thread serverThread = new Thread(new ServerThread());
    private List<Thread> threads = new ArrayList<>();
    private ServerType type;

    public TcpServer(ServerType type) {
        switch (type){
            case TCP_PERM_THREADS:
            case TCP_PERM_CACHED_POOL:
            case TCP_TEMP_SINGLE_THREAD:
                this.type = type;
                return;
            default:
                throw new IllegalArgumentException("This is a strictly tcp server");
        }
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    submit(clientSocket);
                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                }
            }
            threadPool.shutdownNow();
            threads.forEach(Thread::interrupt);
        }
    }

    private void submit(Socket clientSocket) {
        switch (type){
            case TCP_PERM_THREADS:
                Thread t = new Thread(new TcpPermWorker(clientSocket));
                t.start();
                threads.add(t);
                break;
            case TCP_PERM_CACHED_POOL:
                threadPool.execute(new TcpPermWorker(clientSocket));
                break;
            case TCP_TEMP_SINGLE_THREAD:
                new TcpTempWorker(clientSocket).run();
        }
    }

    @Override
    public void startHelper() throws IOException {
        serverSocket = new ServerSocket(ProtocolConstants.SERVER_PORT);
        serverThread.start();
    }

    @Override
    public void stopHelper() throws IOException {
        serverSocket.close();
    }

}


