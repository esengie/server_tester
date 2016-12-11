package ru.spbau.mit;

import org.junit.After;
import org.junit.Test;
import ru.spbau.mit.MeasureClients.MeasureClient;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.CreationAndConfigs.ClientServerFactory;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.Tester.Timing.ServerLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Interaction test
 */
public class MeasureClientServerTest {
    private MeasureClient client1;
    private MeasureClient client2;
    private MeasureServer server;

    private void start(ServerType type) throws IOException, InterruptedException {
        server = ClientServerFactory.createServer(type);
        server.start();
        Thread.sleep(100);
        client1 = ClientServerFactory.createClient(type);
        client1.connect("localhost");
        client2 = ClientServerFactory.createClient(type);
        client2.connect("localhost");
    }

    @After
    public void stop() throws IOException {
        client1.disconnect();
        server.stop();
    }

    @Test(timeout = 2000)
    public void tcpTemp() throws IOException, InterruptedException {
        ServerType type = ServerType.TCP_TEMP_SINGLE_THREAD;
        start(type);
        executeGet(type);
        executeGet(type);
    }

    @Test(timeout = 2000)
    public void tcpPerm() throws IOException, InterruptedException {
        ServerType type = ServerType.TCP_PERM_THREADS;
        start(type);
        executeGet(type);
        executeGet(type);
    }

    @Test(timeout = 2000)
    public void tcpNonBlock() throws IOException, InterruptedException {
        ServerType type = ServerType.TCP_PERM_NON_BLOCK;
        start(type);
        executeGet(type);
        executeGet(type);
    }

    @Test(timeout = 2000)
    public void tcpAsync() throws IOException, InterruptedException {
        ServerType type = ServerType.TCP_PERM_ASYNC;
        start(type);
        executeGet(type);
        executeGet(type);
    }

    @Test(timeout = 2000)
    public void udp() throws IOException, InterruptedException {
        ServerType type = ServerType.UDP_THREAD_PER_REQUEST;
        start(type);
        executeGet(type);
    }

    private void executeGet(ServerType type) throws IOException {
        List<Integer> msg = Arrays.asList(3,1,45,-123);
        List<Integer> res1 = client1.executeRequest(msg);
        List<Integer> res2 = client2.executeRequest(msg);

        Collections.sort(msg);
        assertEquals(msg, res1);
        assertEquals(msg, res2);
    }
}