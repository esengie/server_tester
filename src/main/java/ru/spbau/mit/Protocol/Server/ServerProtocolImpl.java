package ru.spbau.mit.Protocol.Server;

import ru.spbau.mit.Protocol.Exceptions.BadInputException;
import ru.spbau.mit.Protocol.Exceptions.ServerDirectoryException;
import ru.spbau.mit.Protocol.RemoteFile;
import ru.spbau.mit.Protocol.ServerRequestID;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProtocolImpl implements ServerProtocol {
    private static final Logger logger = Logger.getLogger(ServerProtocol.class.getName());

    private Map<Integer, RemoteFile> idToFile = new ConcurrentHashMap<>();
    private Map<Integer, Set<InetSocketAddress>> fileToSeedIPs = new ConcurrentHashMap<>();
    // Needs a way to disconnect guys
    private final Boolean writerLockIDToFile = false;
    private final Boolean writerLockIPs = false;
    private File saveDir;

    @Override
    public int formResponse(DataInputStream in, DataOutputStream out, InetAddress client) throws IOException {
        ServerRequestID request = ServerRequestID.fromInt(in.readByte());
        switch (request) {
            case LIST:
                logger.log(Level.FINE, "Serving list request");
                formListResponse(out);
                return -1;
            case UPLOAD:
                logger.log(Level.FINE, "Serving upload request");
                formUploadResponse(in.readUTF(), in.readLong(), out);
                return -1;
            case SOURCES:
                logger.log(Level.FINE, "Serving sources request");
                formSourcesResponse(in.readInt(), out);
                return -1;
            case UPDATE:
                logger.log(Level.FINE, "Serving update request");
                return formUpdateResponse(in, out, client);
            case ERROR:
                logger.log(Level.SEVERE, "Unknown request");
                throw new BadInputException(MessageFormat.format("Unknown Command {0}", request));
        }
        throw new BadInputException(MessageFormat.format("Unknown Command {0}", request));
    }

    @Override
    public void removeExtras(Set<InetSocketAddress> removed) {
        fileToSeedIPs.forEach((id, set) -> set.removeIf(removed::contains));
    }

    private void formListResponse(DataOutputStream out) throws IOException {
        int count = idToFile.size();
        out.writeInt(count);
        for (int i = 0; i < count; ++i) {
            RemoteFile f = idToFile.get(i);
            out.writeInt(f.id);
            out.writeUTF(f.name);
            out.writeLong(f.size);
        }
    }

    private void formUploadResponse(String fileName, long size, DataOutputStream out) throws IOException {
        int id;
        synchronized (writerLockIDToFile) {
            id = idToFile.size();
            idToFile.put(id, new RemoteFile(id, fileName, size));
            fileToSeedIPs.put(id, new ConcurrentSkipListSet<>(InetSocketAddressComparator::compare));
        }

        out.writeInt(id);
    }

    private void formSourcesResponse(int fileId, DataOutputStream out) throws IOException {
        Set<InetSocketAddress> ips;
        ips = new HashSet<>(fileToSeedIPs.getOrDefault(fileId, new HashSet<>()));

        out.writeInt(ips.size());
        for (InetSocketAddress ip : ips) {
            out.write(ip.getAddress().getAddress(), 0, 4);
            out.writeShort(ip.getPort());
        }
    }

    private int formUpdateResponse(DataInputStream in, DataOutputStream out, InetAddress ip) throws IOException {
        int port = in.readShort();
        int count = in.readInt();
        Set<Integer> fileIds = new HashSet<>();
        for (int i = 0; i < count; ++i) {
            fileIds.add(in.readInt());
        }
        synchronized (writerLockIPs) {
            for (int fileId : fileIds) {
                if (!fileToSeedIPs.containsKey(fileId)) {
                    fileToSeedIPs.put(fileId, new ConcurrentSkipListSet<>(
                            InetSocketAddressComparator::compare));
                }
                fileToSeedIPs.get(fileId).add(new InetSocketAddress(ip, port));
            }
        }
        out.writeBoolean(true);
        return port;
    }

    public ServerProtocolImpl(File saveDir) throws IOException {
        if (!saveDir.exists() || !saveDir.isDirectory())
            throw new ServerDirectoryException("Folder should exist");

        this.saveDir = saveDir;
        loadState();
    }

    private static final String idToFileSave = "idToFile.sav";

    public void saveState() throws IOException {
        File outFile = new File(saveDir, idToFileSave);
        outFile.createNewFile();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
        out.writeObject(new HashMap<>(idToFile));
    }

    private void loadState() throws IOException {
        File inFile = new File(saveDir, idToFileSave);
        if (!inFile.exists())
            return;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(inFile));
        try {
            idToFile = new ConcurrentHashMap<>((HashMap<Integer, RemoteFile>) in.readObject());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Shouldn't happen here, serialization error");
        }
    }
}
