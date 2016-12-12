package ru.spbau.mit.Tester;

import ru.spbau.mit.CreationAndConfigs.ClientServerFactory;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.MeasureClients.MeasureClient;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocol;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocolConstants;
import ru.spbau.mit.Tester.ServerLauncherProtocol.ServerLauncherProtocolImpl;
import ru.spbau.mit.Tester.Timing.RunResults;
import ru.spbau.mit.Tester.Timing.TimeLog;
import ru.spbau.mit.Tester.Timing.UidGenerator;

import java.io.IOException;
import java.net.Socket;
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

    private final String hostName;
    private final ServerLauncherProtocol protocol = new ServerLauncherProtocolImpl();
    private Socket toServer = null;

    public ArchTester(UserConfig config, String hostName) {
        config.checkConsistency();
        this.config = config;
        this.hostName = hostName;
    }

    public RunResults testOnce() throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        startServer(config.getServerType());

        clients.clear();
        for (int i = 0; i < config.getClientsSize(); ++i) {
            clients.add(ClientServerFactory.createClient(config.getServerType()));
        }

        TimeLog clientTimeLog = new TimeLog();
        UidGenerator idGen = new UidGenerator();
        for (MeasureClient cl : clients) {
            int id = idGen.getAndIncrement();
            pool.execute(() -> {
                try {
                    clientTimeLog.logStart(id);
                    executeRequests(cl);
                    clientTimeLog.logEnd(id);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Problems with IO on client \nArchitecture: "
                            + config.getServerType().toString(), e);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted a thread", e);
                }
            });
        }

        RunResults res = null;
        try {
            pool.shutdown();
            while (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                ;
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Logic error, was interrupted", e);
        }

        stopServer();
        RunResults remoteRes = gatherData();

        res = RunResults.builder()
                .perSort(remoteRes.perSort)
                .perRequest(remoteRes.perRequest)
                .perClient(clientTimeLog.tallyMean())
                .build();

//        System.out.println(config.getServerType());
//        System.out.println("Median per sorting: " + Long.toString(res.perSort));
//        System.out.println("       per client request: " + Long.toString(res.perRequest));
//        System.out.println("       per client: " + Long.toString(res.perClient));
        return res;
    }

    private RunResults gatherData() throws IOException {
        RunResults res = protocol.getResults(toServer.getInputStream());
        toServer.close();
        toServer = null;
        return res;
    }

    private void stopServer() throws IOException {
        protocol.stopServerOnRemote(toServer.getInputStream(),
                toServer.getOutputStream());
    }

    private void startServer(ServerType serverType) throws IOException {
        if (toServer == null) {
            toServer = new Socket(hostName, ServerLauncherProtocolConstants.SERVER_PORT);
        } else {
            throw new IllegalStateException(
                    "Socket to server needs to be closed after each run");
        }
        protocol.startServerOnRemote(toServer.getInputStream(),
                toServer.getOutputStream(), serverType);
    }

    private void executeRequests(MeasureClient cl) throws IOException, InterruptedException {
        cl.connect(hostName);
        List<Integer> send = generateArray(config.getArraySize());
        for (int i = 0; i < config.getRequestsPerClient(); ++i) {
            cl.executeRequest(send);
            Thread.sleep(config.getNextReqDelta());
        }
        cl.disconnect();
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
