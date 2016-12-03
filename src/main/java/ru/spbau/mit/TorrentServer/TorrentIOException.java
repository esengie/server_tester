package ru.spbau.mit.TorrentServer;

import java.io.IOException;

public class TorrentIOException extends IOException {
    public TorrentIOException(String s, IOException e) {
    }

    public TorrentIOException(String s) {
        super(s);
    }
}
