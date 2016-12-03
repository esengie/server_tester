package ru.spbau.mit.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Plain and simple, nothing fancy, code from StackOverflow
 */
public interface Serializer<T> {
    void serialize(T obj, OutputStream out) throws IOException;

    T deserialize(InputStream in) throws IOException;
}