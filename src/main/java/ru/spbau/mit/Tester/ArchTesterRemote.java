package ru.spbau.mit.Tester;

import ru.spbau.mit.CreationAndConfigs.ClientServerFactory;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.Protocol.ProtocolConstants;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocol;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocolImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A single(!) client server
 */
public class ArchTesterRemote {
    private static final Logger logger = Logger.getLogger(ArchTesterRemote.class.getName());

    public static void main(String[] args) {
        ArchTesterRemote tester = new ArchTesterRemote();
        try {
            tester.run();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected error with server", e);
        }
    }

    private ServerLauncherProtocol protocol = new ServerLauncherProtocolImpl();
    private MeasureServer server = null;
    private Socket client = null;

    private void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(ProtocolConstants.SERVER_PORT);
        while (true) {
            client = serverSocket.accept();
            try {
                serverLoop();
            } catch (IOException e){
                logger.log(Level.SEVERE, "Error in the server loop", e);
                if (server != null){
                    server.stop();
                    server = null;
                }
            }
        }
    }

    private void serverLoop() throws IOException {
        while (true) {
            try {
                ServerType type = protocol.readRequest(client.getInputStream());
                if (type == ServerType.MUTE) {
                    handleStop();
                    server = null;
                    continue;
                }
                handleStart(type);
            } catch (SocketException e) {
                // this is fine
            }
        }
    }

    private void handleStart(ServerType type) throws IOException {
        if (server != null){
            throw new IllegalStateException(
                    "Can't start a server before stopping the previous one!");
        }
        server = ClientServerFactory.createServer(type);
        server.start();
        protocol.sendAck(client.getOutputStream());
    }

    private void handleStop() throws IOException {
        if (server == null)
            return;
        server.stop();
        protocol.sendAck(client.getOutputStream());
        protocol.sendResponse(client.getOutputStream(),
                server.tallySortTimes(), server.tallyRequestTimes());
    }
}
