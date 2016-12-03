package ru.spbau.mit.CLIApps;

import ru.spbau.mit.TorrentServer.TorrentServer;
import ru.spbau.mit.TorrentServer.TorrentServerImpl;
import ru.spbau.mit.TorrentServer.TorrentIOException;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApp {
    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        logger.log(Level.FINE, "Starting the server on port 8081");
        TorrentServer s = new TorrentServerImpl();
        try {
            if (args.length < 1) {
                s.start(new File(".").getAbsoluteFile());
                runServer(s);
                return;
            }
            if (args.length > 1)
                throw new IllegalArgumentException("Too many args specified");
            File config = new File(args[0]);
            s.start(config);
            runServer(s);
        } catch (IllegalArgumentException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private static void runServer(TorrentServer s) throws TorrentIOException {
        while (!getUserInput().equals("q")) {
        }
        s.stop();
    }

    private static String getUserInput() {
        return new Scanner(System.in).nextLine();
    }
}
