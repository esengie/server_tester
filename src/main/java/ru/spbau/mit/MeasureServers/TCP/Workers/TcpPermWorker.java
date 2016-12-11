package ru.spbau.mit.MeasureServers.TCP.Workers;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.TcpServer;
import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServerSide.SyncTcpServerProtocol;
import ru.spbau.mit.MeasureServers.Job;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpPermWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(TcpServer.class.getName());

    private Socket clientSocket;
    private ServerProtocol protocol = new SyncTcpServerProtocol();

    public TcpPermWorker(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                List<Integer> lst = protocol.readRequest(
                        new DataInputStream(clientSocket.getInputStream()));
                Job job = MeasureServer.createJob(lst);
                protocol.sendResponse(
                        new DataOutputStream(clientSocket.getOutputStream()),
                        job.call());
            }

        } catch (IOException e) {
            logger.log(Level.FINER, "Client closed the connection", e);
        }
    }
}
