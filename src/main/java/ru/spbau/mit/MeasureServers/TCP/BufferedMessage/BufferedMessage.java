package ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

import java.nio.ByteBuffer;

/**
 * Used in NIO servers for storing the state if the message and the stage it is in
 */
public class BufferedMessage {
    public int logID;
    public final ByteBuffer sizeBuf = ByteBuffer.allocate(4);
    public volatile ByteBuffer data;
    public volatile MessageState state = MessageState.EMPTY;

    public BufferedMessage(int logID) {
        this.logID = logID;
    }
}
