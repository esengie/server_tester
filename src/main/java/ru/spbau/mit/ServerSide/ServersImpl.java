package ru.spbau.mit.ServerSide;

import ru.spbau.mit.Protocol.ServiceState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServersImpl extends Servers {
    private ServerSocket serverSocket = null;

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private Thread serverThread = new Thread(new ServerThread());

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new WorkerRunnable(clientSocket));
                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                }
            }
            threadPool.shutdownNow();
        }
    }


    @Override
    public void startHelper() throws IOException {
        openServerSocket();
        serverThread.start();
    }


    @Override
    public void stopHelper() throws IOException {
        serverSocket.close();
    }

    private void openServerSocket() throws IOException {
        serverSocket = new ServerSocket(ServersImpl.PORT_NUMBER);
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
                logger.log(Level.SEVERE, "Clients handler error", e);
            }
        }
    }
}



