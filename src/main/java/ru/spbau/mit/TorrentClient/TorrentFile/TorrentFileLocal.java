package ru.spbau.mit.TorrentClient.TorrentFile;


import ru.spbau.mit.Protocol.RemoteFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorrentFileLocal extends Observable {
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());

    private final String mode = "rwd";
    private final Set<Integer> parts = new ConcurrentSkipListSet<>();
    private RandomAccessFile descriptor;
    private final File localFile;

    /**
     * Creates a remote file locally
     *
     * @param file - remote file, contains id, name and size
     * @throws IOException -- if couldn't set the file length
     */
    TorrentFileLocal(File dir, RemoteFile file) throws IOException {
        localFile = new File(dir, file.name);
        if (!localFile.createNewFile()) {
            logger.log(Level.FINE, "Possibly file with the same name exists, couldn't create the file");
            throw new FileAlreadyExistsException("FileManager can't overwrite files");
        }
        descriptor = new RandomAccessFile(localFile, mode);
        descriptor.setLength(file.size);
    }

    /**
     * Loads a torrentfile from a file
     *
     * @param filepath -- path
     */
    TorrentFileLocal(File filepath, Set<Integer> parts) {
        try {
            descriptor = new RandomAccessFile(filepath, mode);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "TorrentFile was passed a nonexistent file");
        }
        this.parts.addAll(parts);
        localFile = filepath;
    }

    /**
     * Creates a file from a local one
     *
     * @param filepath path to file
     */
    TorrentFileLocal(File filepath) throws IOException {
        int totalParts;
        try {
            descriptor = new RandomAccessFile(filepath, mode);
            totalParts = totalParts();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "TorrentFile was passed a nonexistent file");
            throw e;
        }
        for (int i = 0; i < totalParts; ++i) {
            this.parts.add(i);
        }
        localFile = filepath;
    }

    public Set<Integer> getParts() {
        return new HashSet<>(parts);
    }

    /**
     * Could it be faster? Potentially, just curious.
     *
     * @param buf  buffer
     * @param part part number to read
     * @throws IOException if writes are throwing
     */
    public synchronized void write(byte[] buf, int part) throws IOException {
        descriptor.seek(part * RemoteFile.PART_SIZE);
        descriptor.write(buf, 0, partSize(part));
        parts.add(part);
        setChanged();
        notifyObservers(percent());
    }

    public int totalParts() throws IOException {
        return (int) (((descriptor.length() - 1) + (long) RemoteFile.PART_SIZE) / (long) RemoteFile.PART_SIZE);
    }

    public int partSize(int part) throws IOException {
        long fileLength = descriptor.length();
        if ((part + 1) * RemoteFile.PART_SIZE > fileLength) {
            return (int) (fileLength - part * RemoteFile.PART_SIZE);
        }
        return RemoteFile.PART_SIZE;
    }

    public synchronized int read(byte[] buf, int part) throws IOException {
        int bytesToRead = partSize(part);
        descriptor.seek(part * RemoteFile.PART_SIZE);
        descriptor.readFully(buf, 0, bytesToRead);
        return bytesToRead;
    }

    /**
     * Saves to disk
     */
    synchronized void close() {
        try {
            descriptor.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Descriptor error occured while closing");
        }
    }

    public File getFile() {
        return localFile;
    }

    public double percent() {
        try {
            return parts.size() * 1.0 / totalParts();
        } catch (IOException e) {
            throw new IllegalStateException("Shouldn't be here");
        }
    }
}
