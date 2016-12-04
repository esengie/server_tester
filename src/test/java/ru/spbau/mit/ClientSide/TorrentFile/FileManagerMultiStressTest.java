package ru.spbau.mit.ClientSide.TorrentFile;

import org.junit.*;
import ru.spbau.mit.Common.WithFileManager;
import ru.spbau.mit.Protocol.RemoteFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileManagerMultiStressTest {

    private FileManager fm;
    private final File res = new File("res");
    int fileId = 123;

    private ExecutorService exec = Executors.newFixedThreadPool(12);
    private CountDownLatch latch = new CountDownLatch(10);
    private BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
    private Map<Integer, List<Reads>> threadResults = new ConcurrentHashMap<>();
    private List<Reads> reference = new ArrayList<>();

    static class Reads {
        int part;
        byte[] res;
        int length;

        Reads(int part, byte[] res, int length) {
            this.part = part;
            this.res = res;
            this.length = length;
        }
    }

    @Rule
    public final WithFileManager dir = new WithFileManager(res);


    @Before
    public void setUpStreams() throws IOException {
        fm = dir.getFileManager();
    }

    private static void readToBuf(byte[] buf, int length, RandomAccessFile rf, long offset) throws IOException {
        rf.seek(offset);
        rf.read(buf, 0, length);
    }

    private void readWrite(RandomAccessFile rf, int i) throws IOException {
        byte[] buf = new byte[RemoteFile.PART_SIZE];
        int length = fm.getTorrentFile(fileId).partSize(i);
        readToBuf(buf, length, rf, i);
        fm.getTorrentFile(fileId).write(buf, i);
        reference.add(new Reads(i, buf, length));
    }

    public class Reader implements Runnable {

        CountDownLatch latch = null;
        int id = 0;

        public Reader(CountDownLatch latch, int i) {
            this.latch = latch;
            id = i;
        }

        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                ;
            }
            int cnt = 5;
            while (!Thread.currentThread().isInterrupted() || cnt > 0) {
                Integer part = 0;
                try {
                    part = queue.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    continue;
                }
                if (part == null)
                    continue;
//                System.out.println("Im'here");
                byte[] buf = new byte[RemoteFile.PART_SIZE];
                int length = 0;
                try {
                    length = fm.getTorrentFile(fileId).read(buf, part);
                } catch (IOException e) {
                    break;
                }
//                System.out.println("And here new");
                List<Reads> lst = threadResults.getOrDefault(id, new CopyOnWriteArrayList<>());
                lst.add(new Reads(part, buf, length));
                threadResults.put(id, lst);
//                System.out.println("And here");
                cnt--;
            }
        }
    }

    @Ignore
    @Test
    public void readWrite() throws Exception {
        File f = new File(dir.curDir, "zbt.tar.gz");
        RandomAccessFile rf = new RandomAccessFile(f, "rwd");

        fm.createTorrentFile(f.getParentFile(), new RemoteFile(fileId, f.getName() + "23", f.length()));

        for (int i = 0; i < 12; ++i) {
            exec.submit(new Reader(latch, i));
            latch.countDown();
        }

        for (int i = 0; i < fm.getTorrentFile(fileId).totalParts(); ++i) {
            readWrite(rf, i);
            populateQueue();
            Thread.sleep(1000);
        }
        System.out.println("Done");
        exec.awaitTermination(10, TimeUnit.SECONDS);
        exec.shutdownNow();

        int i = 0;
        for (List<Reads> lst : threadResults.values()) {
            System.out.print(i);
            System.out.print(": ");
            i++;
            int j = 0;
            for (Reads r : lst) {
                j++;
                Assert.assertArrayEquals(reference.get(r.part).res, r.res);
            }
            System.out.println(j);
        }
    }

    private void populateQueue() {
        for (int i : fm.getTorrentFile(fileId).getParts()) {
            for (int j = 0; j < 20; ++j) {
                queue.add(i);
            }
        }

    }
}