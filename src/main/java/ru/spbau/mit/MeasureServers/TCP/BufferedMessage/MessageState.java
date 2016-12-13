package ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

/**
 * A simple pipeline: we are reading data after we've read the size of data,
 * processing when we didn't yet sort the data, and waiting to write whenever
 * we haven't already
 */
public enum MessageState {
    EMPTY, READING_DATA, PROCESSING, WAITING_TO_WRITE
}
