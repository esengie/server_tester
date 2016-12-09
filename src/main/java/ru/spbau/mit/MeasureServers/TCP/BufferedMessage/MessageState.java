package ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

public enum MessageState {
    EMPTY, READING_DATA, PROCESSING, WAITING_TO_WRITE
}
