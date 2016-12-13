package ru.spbau.mit.Tester.ServerLauncherProtocol;

import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ServerLauncherProtocolImplTest {

    private ByteArrayOutputStream clientOutContent;
    private ByteArrayOutputStream servOutContent;

    private ServerLauncherProtocolImpl protocol = new ServerLauncherProtocolImpl();

    @Before
    public void setUpStreams() throws IOException {
        clientOutContent = new ByteArrayOutputStream();
        servOutContent = new ByteArrayOutputStream();
    }

    @Test
    public void startServerOnRemote() throws Exception {
        protocol.sendAck(servOutContent); //single threaded cheat

        protocol.startServerOnRemote(new ByteArrayInputStream(servOutContent.toByteArray()),
                clientOutContent, ServerType.TCP_PERM_NON_BLOCK);

        ServerType type = protocol.readRequest(new ByteArrayInputStream(
                clientOutContent.toByteArray())
        );

        assertEquals(ServerType.TCP_PERM_NON_BLOCK, type);
    }

    @Test
    public void getResults() throws Exception {
        protocol.sendResponse(servOutContent, 12, 23);
        RunResults mr = protocol.getResults(new ByteArrayInputStream(
                servOutContent.toByteArray()));
        assertEquals(mr.perRequest, 23);
        assertEquals(mr.perSort, 12);
    }

}