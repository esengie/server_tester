package ru.spbau.mit.Common;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.ClientSide.TorrentFile.FileManager;

import java.io.File;

/**
 * Creates a directory under the filemanager
 */
public class WithFileManager extends TemporaryFolder {
    private final boolean populate;
    private FileManager fm;
    private File resources;
    public File curDir;

    public WithFileManager(File resources) {
        this.resources = resources;
        populate = true;
    }

    public WithFileManager(File resources, boolean populate) {
        this.resources = resources;
        this.populate = populate;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        File tmp = newFolder();
        curDir = tmp.getParentFile();
        FileUtils.deleteDirectory(tmp);
        fm = new FileManager(curDir);
        FileUtils.copyDirectory(resources, curDir);
        if (populate) {
            int i = 0;
            for (File f : curDir.listFiles()) {
                fm.addTorrentFile(f, new RemoteFile(i, f.getName(), f.length()));
                ++i;
            }
        }
    }

    public FileManager getFileManager() {
        return fm;
    }

}
