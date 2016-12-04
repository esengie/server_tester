package ru.spbau.mit.ClientSide.TorrentFile;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileManagerSingleTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final String s1 = "adm";
    private final String DIR = "adm/adm";

    private final String F1 = "long.txt";
    private final String F2 = "short.txt";
    private final String F2DIR = DIR + "/1";

    private File f;
    private File f2;
    private File dir;
    private FileManager fm;

    @Before
    public void setUpStreams() throws IOException {
        folder.newFolder(DIR.split("/")[0]);
        folder.newFolder(DIR);
        f = folder.newFile(DIR + "/" + F1);
        dir = folder.newFolder(F2DIR);
        f2 = folder.newFile(F2DIR + "/" + F2);
        FileUtils.writeByteArrayToFile(f2, F2DIR.getBytes());
        fm = new FileManager(folder.getRoot().getAbsoluteFile());
    }

    @Test
    public void addTorrentFile() throws Exception {
        fm.addTorrentFile(f2.getAbsoluteFile(), new RemoteFile(0, f2.getName(), f2.length()));
        TorrentFileLocal file = fm.getTorrentFile(0);
        assertEquals(f2, file.getFile());
    }

    @Test
    public void createTorrentFile() throws Exception {
        fm.createTorrentFile(f2.getParentFile(), new RemoteFile(0, f2.getName() + "23", f2.length()));
        TorrentFileLocal file = fm.getTorrentFile(0);
        assertEquals(f2.length(), file.getFile().length());
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void createExistingTorrentFile() throws Exception {
        fm.createTorrentFile(f2.getParentFile(), new RemoteFile(0, f2.getName(), f2.length()));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void addSameID() throws Exception {
        fm.createTorrentFile(f2.getParentFile(), new RemoteFile(0, f2.getName() + "23", f2.length()));
        fm.addTorrentFile(f2.getAbsoluteFile(), new RemoteFile(0, f2.getName(), f2.length()));
    }

    @Test
    public void readWrite() throws Exception {
        fm.addTorrentFile(f2.getAbsoluteFile(), new RemoteFile(0, f2.getName(), f2.length()));
        fm.createTorrentFile(f2.getParentFile(), new RemoteFile(1, f2.getName() + "23", f2.length()));
        byte[] buf = new byte[RemoteFile.PART_SIZE];
        fm.getTorrentFile(0).read(buf, 0);
        fm.getTorrentFile(1).write(buf, 0);
        assertTrue("The files differ!",
                FileUtils.contentEquals(fm.getTorrentFile(0).getFile(),
                        fm.getTorrentFile(1).getFile()));
    }

    @Test
    public void saveState() throws IOException {
        fm.createTorrentFile(f2.getParentFile(), new RemoteFile(0, f2.getName() + "23", f2.length()));
        fm.addTorrentFile(f2.getAbsoluteFile(), new RemoteFile(1, f2.getName(), f2.length()));
        fm.saveToDisk();
        fm = new FileManager(folder.getRoot().getAbsoluteFile());
        assertEquals(Arrays.asList(0, 1), fm.getFileIds());
        assertEquals(f2, fm.getTorrentFile(1).getFile());
    }

}