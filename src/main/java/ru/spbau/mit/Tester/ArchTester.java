package ru.spbau.mit.Tester;

import ru.spbau.mit.MeasureClients.ClientFactory;
import ru.spbau.mit.MeasureClients.MeasureClient;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.ServerFactory;
import ru.spbau.mit.UserConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArchTester {
    private static final Logger logger = Logger.getLogger(ArchTester.class.getName());

    private final UserConfig config;
    private final List<MeasureClient> clients = new ArrayList<>();

    public ArchTester(UserConfig config) {
        this.config = config;
    }

    public void testOnce() throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();
        MeasureServer server = ServerFactory.createServer(config.getServerType());
        server.start();
        clients.clear();

        for (int i = 0; i < config.getClientsSize(); ++i) {
            clients.add(ClientFactory.createClient(config.getServerType()));
        }

        for (MeasureClient cl : clients) {
            pool.execute(() -> {
                try {
                    cl.connect("localhost");
                    List<Integer> send = generateArray(config.getArraySize());
                    for (int i = 0; i < config.getRequestsPerClient(); ++i) {
                        cl.executeRequest(send);
                        Thread.sleep(config.getNextReqDelta());
                    }
                    cl.disconnect();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Problems with IO on client \nArchitecture: "
                            + config.getServerType().toString(), e);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted a thread", e);
                }
            });
        }

        try {
            pool.shutdown();
            while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("lalala");
            }
            server.stop();
        } catch (InterruptedException e) {
            //
        }
    }

    private static List<Integer> generateArray(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = -size / 2; list.size() < size; ++i) {
            list.add(i);
        }

        Collections.shuffle(list);
        return list;
    }

}
