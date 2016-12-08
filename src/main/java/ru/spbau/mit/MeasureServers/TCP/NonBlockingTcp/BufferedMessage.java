package ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp;

import java.nio.ByteBuffer;

public class BufferedMessage {
    public final ByteBuffer sizeBuf = ByteBuffer.allocate(4);
    public volatile ByteBuffer data;
    public volatile MessageState state = MessageState.EMPTY;
}
