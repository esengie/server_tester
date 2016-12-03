package ru.spbau.mit.TorrentClient;

import ru.spbau.mit.TorrentServer.TorrentIOException;

public class TorrentExistsException extends TorrentIOException {
    public TorrentExistsException(String s) {
        super(s);
    }
}
