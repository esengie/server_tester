package ru.spbau.mit.Protocol;

import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.Protocol.ClientSide.ClientProtocol;
import ru.spbau.mit.Protocol.ClientSide.TcpClientProtocol;
import ru.spbau.mit.Protocol.ServerSide.ServerProtocol;
import ru.spbau.mit.Protocol.ServerSide.SyncTcpServerProtocol;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ServerToClientProtocolImplTest {
    private ByteArrayOutputStream outContent;
    private DataInputStream outIn;

    ClientProtocol clientP = new TcpClientProtocol();
    ServerProtocol serverP = new SyncTcpServerProtocol();

    @Before
    public void setUpStreams() throws IOException {
        outContent = new ByteArrayOutputStream();
    }

    @Test
    public void toServer() throws Exception {
        List<Integer> msg = Arrays.asList(1, 2, 3);
        clientP.sendRequest(new DataOutputStream(outContent), msg);
        outIn = new DataInputStream(new ByteArrayInputStream(outContent.toByteArray()));
        List<Integer> toServer = serverP.readRequest(outIn);
        assertEquals(msg, toServer);
    }

    @Test
    public void toClient() throws Exception {
        List<Integer> msg = Arrays.asList(1, 2, 3);
        serverP.sendResponse(new DataOutputStream(outContent), msg);
        outIn = new DataInputStream(new ByteArrayInputStream(outContent.toByteArray()));
        List<Integer> toClient = clientP.readResponse(outIn);
        assertEquals(msg, toClient);
    }

}