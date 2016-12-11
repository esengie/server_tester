package ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers;


import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.MessageState;

import java.nio.channels.AsynchronousSocketChannel;

public class WriteHandler extends CommonChannelHandler {
    public WriteHandler(MeasureServer server, AsynchronousSocketChannel channel) {
        super(server, channel);
    }

    @Override
    public void completed(Integer res, BufferedMessage msg) {
        switch (msg.state) {
            case WAITING_TO_WRITE:
                if (msg.data.hasRemaining()) {
                    channel.write(msg.data, msg, this);
                    return;
                }
                server.clientLogger.logEnd(msg.logID);

                msg.logID = server.clientID.getAndIncrement();
                msg.state = MessageState.EMPTY;
                msg.sizeBuf.clear();
                channel.read(msg.sizeBuf, msg, new ReadHandler(server, channel));
        }
    }
}