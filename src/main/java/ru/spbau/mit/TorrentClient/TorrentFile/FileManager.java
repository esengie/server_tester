package ru.spbau.mit.TorrentClient.TorrentFile;

import ru.spbau.mit.Protocol.Exceptions.ClientDirectoryException;
import ru.spbau.mit.Protocol.RemoteFile;
import ru.spbau.mit.Serialization.Serializer;
import ru.spbau.mit.Serialization.SerializerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class that manages all the files and their parts stored on a client
 * <p>
 * Multithreaded access to parts of files and serialization of state
 */
public class FileManager {
    private final Map<Integer, TorrentFileLocal> files = new ConcurrentHashMap<>();
    private final File saveDir;

    public List<Integer> getFileIds() {
        return new ArrayList<>(files.keySet());
    }

    public FileManager(File location) throws IOException {
        saveDir = location;
        File appFolder = new File(saveDir, appFolderName);
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            throw new ClientDirectoryException("Directory doesn't exist or corrupted");
        }
        if (!appFolder.exists()) {
            return;
        }
        loadState(appFolder);
    }

    public TorrentFileLocal getTorrentFile(int fileId) {
        return files.getOrDefault(fileId, null);
    }

    public synchronized TorrentFileLocal createTorrentFile(File location, RemoteFile file) throws IOException {
        checkDir(location);
        TorrentFileLocal f = new TorrentFileLocal(location, file);
        if (files.containsKey(file.id)) {
            throw new FileAlreadyExistsException("Can't add another file wih the same ID");
        }
        files.put(file.id, f);
        return f;
    }

    public synchronized TorrentFileLocal addTorrentFile(File filePath, RemoteFile file) throws IOException {
        TorrentFileLocal f = new TorrentFileLocal(filePath);
        if (files.containsKey(file.id)) {
            throw new FileAlreadyExistsException("Can't add another file wih the same ID");
        }
        files.put(file.id, f);
        return f;
    }

    private static void checkDir(File location) throws NotDirectoryException {
        if (!location.exists() || !location.isDirectory())
            throw new NotDirectoryException(location.getAbsolutePath());
    }

    public synchronized void saveToDisk() throws IOException {
        Serializer<Map<Integer, Set<Integer>>> ser1 = new SerializerImpl<>();
        Map<Integer, Set<Integer>> parts = new HashMap<>();

        Serializer<Map<Integer, File>> ser2 = new SerializerImpl<>();
        Map<Integer, File> fileNames = new HashMap<>();

        for (int id : files.keySet()) {
            TorrentFileLocal f = files.get(id);
            parts.put(id, f.getParts());
            fileNames.put(id, f.getFile());
            f.close();
        }

        File folder = new File(saveDir, appFolderName);
        if (!folder.exists()) {
            folder.mkdir();
        }

        File file1 = new File(folder, idToParts);
        if (!file1.exists()) {
            file1.createNewFile();
        }
        ser1.serialize(parts, new FileOutputStream(file1));

        File file2 = new File(folder, idToNames);
        if (!file2.exists()) {
            file2.createNewFile();
        }
        ser2.serialize(fileNames, new FileOutputStream(file2));
    }

    private void loadState(File appFolder) throws IOException {
        Serializer<Map<Integer, Set<Integer>>> ser1 = new SerializerImpl<>();
        Map<Integer, Set<Integer>> parts = ser1.deserialize(
                new FileInputStream(new File(appFolder, idToParts)));

        Serializer<Map<Integer, File>> ser2 = new SerializerImpl<>();
        Map<Integer, File> fileNames = ser2.deserialize(
                new FileInputStream(new File(appFolder, idToNames)));

        for (int fileId : fileNames.keySet()) {
            TorrentFileLocal file = new TorrentFileLocal(
                    fileNames.get(fileId),
                    parts.get(fileId));
            files.put(fileId, file);
        }
    }


    private static final String appFolderName = "torrentSeedState";
    private static final String idToParts = "idToParts";
    private static final String idToNames = "idToNames";
}
