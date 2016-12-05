package ru.spbau.mit.MeasureClients;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.TcpServer;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Interaction test
 */
public class MeasureClientImplTest {
    private MeasureClient cl1;
    private MeasureClient cl2;
    private final short port1 = 8001;
    private final short port2 = 8002;

    private final File res1 = new File("res");
    private final File res2 = new File("gradle/wrapper");
    private final String largeBin = "zbt.tar.gz";
    private MeasureServer server = new TcpServer();

    @Rule
    public final TemporaryFolder sDir = new TemporaryFolder();

    @Before
    public void start() throws IOException, InterruptedException {
        server.start();
        Thread.sleep(100);
//        cl1 = new TorrentClientImpl(fm1, port1);
//        cl2 = new TorrentClientImpl(fm2, port2);
//        cl1.connect("localhost");
//        cl2.connect("localhost");
    }

    @After
    public void stop() throws IOException, InterruptedException {
        cl1.disconnect();
        cl2.disconnect();
        Thread.sleep(200);
        server.stop();
    }


    @Test
    public void executeUpload() throws Exception {
//        cl1.executeUpload(new File(cDir1.curDir, largeBin));
//        assertEquals(1, cl2.executeList().size());
    }

    @Test(timeout = 20000)
    public void executeGet() throws Exception {
//        cl1.executeUpload(new File(cDir1.curDir, largeBin));
//        cl2.executeGet(cDir2.curDir, cl2.executeList().get(0));
//        while(!fm1.getTorrentFile(0).getParts().equals(fm2.getTorrentFile(0).getParts())){
//            ;
//        }
//        assertTrue(FileUtils.contentEquals(new File(cDir1.curDir, largeBin),
//                new File(cDir2.curDir, largeBin)));
    }

}