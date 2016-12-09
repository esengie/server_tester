package ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers;


import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers.CommonChannelHandler;
import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers.ReadHandler;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.MessageState;

import java.nio.channels.AsynchronousSocketChannel;

public class WriteHandler extends CommonChannelHandler {
    public WriteHandler(AsynchronousSocketChannel channel) {
        super(channel);
    }

    @Override
    public void completed(Integer res, BufferedMessage msg) {
        switch (msg.state){
            case WAITING_TO_WRITE:
                if (msg.data.hasRemaining()){
                    channel.write(msg.data, msg, this);
                    return;
                }
                msg.state = MessageState.EMPTY;
                msg.sizeBuf.clear();
                channel.read(msg.sizeBuf, msg, new ReadHandler(channel));
        }
    }
}