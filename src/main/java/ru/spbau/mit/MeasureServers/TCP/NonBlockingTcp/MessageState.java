package ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp;

public enum MessageState {
    EMPTY, READING_DATA, PROCESSING, WAITING_TO_WRITE
}
