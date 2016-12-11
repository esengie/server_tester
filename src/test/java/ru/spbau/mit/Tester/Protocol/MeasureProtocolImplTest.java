package ru.spbau.mit.Tester.Protocol;

import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.ProtoMessage.Messages;
import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;
import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServerSide.SyncTcpServerProtocol;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MeasureProtocolImplTest {

    private ByteArrayOutputStream clientOutContent;
    private ByteArrayOutputStream servOutContent;

    private MeasureProtocolImpl protocol = new MeasureProtocolImpl();

    @Before
    public void setUpStreams() throws IOException {
        clientOutContent = new ByteArrayOutputStream();
        servOutContent = new ByteArrayOutputStream();
    }

    @Test
    public void startServerOnRemote() throws Exception {
        protocol.sendAck(servOutContent); //single threaded cheat

        protocol.startServerOnRemote(new ByteArrayInputStream(servOutContent.toByteArray()),
                clientOutContent, ServerType.TCP_PERM_CACHED_POOL);

        ServerType type = protocol.readRequest(new ByteArrayInputStream(
                clientOutContent.toByteArray())
        );

        assertEquals(ServerType.TCP_PERM_CACHED_POOL, type);
    }

    @Test
    public void getResults() throws Exception {
        protocol.sendResponse(servOutContent, 12, 23);
        Messages.MeasureResult mr = protocol.getResults(new ByteArrayInputStream(
                servOutContent.toByteArray()));
        assertEquals(mr.getTimePerClient(), 23);
        assertEquals(mr.getTimePerJob(), 12);
    }

}