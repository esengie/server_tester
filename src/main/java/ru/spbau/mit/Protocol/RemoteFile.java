package ru.spbau.mit.Protocol;

import java.io.Serializable;

public class RemoteFile implements Serializable {
    public final int id;
    public final String name;
    public final long size;

    public static final int PART_SIZE = 1 << 22; // 4 Mb

    public RemoteFile(int fileId, String fileName, long size) {
        this.id = fileId;
        this.name = fileName;
        this.size = size;
    }

    public int parts() {
        return (int) (size + RemoteFile.PART_SIZE - 1) / RemoteFile.PART_SIZE;
    }
}
