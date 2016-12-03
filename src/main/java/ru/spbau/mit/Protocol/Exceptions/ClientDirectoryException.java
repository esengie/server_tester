package ru.spbau.mit.Protocol.Exceptions;

import java.io.IOException;

/**
 * Throws error if the client directory doesn't exist or is corrupted
 */
public class ClientDirectoryException extends IOException {
    public ClientDirectoryException(String s) {
        super(s);
    }
}
