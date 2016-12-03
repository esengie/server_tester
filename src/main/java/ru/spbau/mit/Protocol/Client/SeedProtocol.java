package ru.spbau.mit.Protocol.Client;

import ru.spbau.mit.TorrentClient.TorrentFile.FileManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Handles the client requests from here: http://hwproj.me/tasks/5785
 * <p>
 * Basucally stat requests (list parts of a file that I have) and get requests (get part of a file)
 */
public interface SeedProtocol {
    void formResponse(DataInputStream in, DataOutputStream out, FileManager manager) throws IOException;
}
