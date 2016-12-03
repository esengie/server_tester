package ru.spbau.mit.TorrentServer;

import java.io.File;
import java.io.IOException;

/**
 * This is the torrent tracker interface, run on port 8081, may timeout its users,
 * and handle all of the request to ServerProtocol
 * <p>
 * Also needs to be able to save the file ids between launches
 */
public interface TorrentServer {
    void start(File saveDir) throws IOException;

    void stop() throws TorrentIOException;

    boolean isStopped();
}
