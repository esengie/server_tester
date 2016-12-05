package ru.spbau.mit.MeasureServers.TCP;

import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServerSide.SyncTcpServerProtocol;
import ru.spbau.mit.MeasureServers.Job;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class TcpTempWorker implements Runnable {
    private Socket clientSocket;
    private ServerProtocol protocol = new SyncTcpServerProtocol();

    TcpTempWorker(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            List<Integer> lst = protocol.readRequest(
                    new DataInputStream(clientSocket.getInputStream()));
            Job job = new Job(lst);
            protocol.sendResponse(
                    new DataOutputStream(clientSocket.getOutputStream()),
                            job.call());
            clientSocket.close();

        } catch (IOException e) {
            //
        }
    }
}
