package ru.spbau.mit.Tester;

import ru.spbau.mit.CreationAndConfigs.ClientServerFactory;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocol;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocolConstants;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocolImpl;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A single client server - one measurement, wait for next client etc.
 */
public class ArchTesterRemote implements Runnable {
    private static final Logger logger = Logger.getLogger(ArchTesterRemote.class.getName());

    public static void main(String[] args) {
        ArchTesterRemote tester = new ArchTesterRemote();
        tester.run();
    }

    private ServerLauncherProtocol protocol = new ServerLauncherProtocolImpl();
    private MeasureServer server = null;
    private Socket client = null;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(ServerLauncherProtocolConstants.SERVER_PORT);
            acceptLoop(serverSocket);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unexpected error with server", e);
        }
    }

    private void acceptLoop(ServerSocket serverSocket) throws IOException {
        while (true) {
            client = serverSocket.accept();
            try {
                serverActions();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error in the server loop", e);
                if (server != null) {
                    server.stop();
                    server = null;
                }
            }
        }
    }

    private void serverActions() throws IOException {
        try {
            ServerType type = protocol.readRequest(client.getInputStream());
            if (type == ServerType.MUTE) {
                throw new IllegalStateException("Need to start first");
            }
            handleStart(type);

            type = protocol.readRequest(client.getInputStream());
            if (type != ServerType.MUTE) {
                throw new IllegalStateException("Need to stop first");
            }
            handleStop();
            server = null;
        } catch (SocketException | EOFException e) {
            // this is fine
        }
    }

    private void handleStart(ServerType type) throws IOException {
        if (server != null) {
            server.stop();
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
