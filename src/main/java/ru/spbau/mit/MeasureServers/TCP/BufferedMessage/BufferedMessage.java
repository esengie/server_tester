package ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

import java.nio.ByteBuffer;

public class BufferedMessage {
    public int logID;
    public final ByteBuffer sizeBuf = ByteBuffer.allocate(4);
    public volatile ByteBuffer data;
    public volatile MessageState state = MessageState.EMPTY;

    public BufferedMessage(int logID) {
        this.logID = logID;
    }
}
