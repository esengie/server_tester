package ru.spbau.mit.Protocol.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * A server protocol, described here: http://hwproj.me/tasks/5785
 * <p>
 * Basically list files it knows about, get sources of the file by its ID, update seed info,
 * and handle upload requests - give UIDs to files
 */
public interface ServerProtocol {
    int formResponse(DataInputStream in, DataOutputStream out, InetAddress client) throws IOException;

    void removeExtras(Set<InetSocketAddress> removed);

    void saveState() throws IOException;
}

