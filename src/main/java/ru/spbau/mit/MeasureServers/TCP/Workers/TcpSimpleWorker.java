package ru.spbau.mit.MeasureServers.TCP.Workers;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.TcpServer;
import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServerSide.SyncTcpServerProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A worker used in simple TCP socket connections
 */
public class TcpSimpleWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(TcpServer.class.getName());

    private final MeasureServer server;
    private final Socket clientSocket;
    private final ServerProtocol protocol = new SyncTcpServerProtocol();

    public TcpSimpleWorker(MeasureServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        server.defaultLogClient(this::runOnce);
    }

    private void runOnce() {
        try {
            List<Integer> lst = protocol.readRequest(
                    new DataInputStream(clientSocket.getInputStream()));
            MeasureServer.Job job = server.createJob(lst);
            protocol.sendResponse(
                    new DataOutputStream(clientSocket.getOutputStream()),
                    job.call());
        } catch (IOException e) {
            logger.log(Level.FINER, "Client closed the connection", e);
        }
    }
}

