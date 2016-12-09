package ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers;

import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommonChannelHandler implements CompletionHandler<Integer, BufferedMessage> {
    private static final Logger logger = Logger.getLogger(CommonChannelHandler.class.getName());
    protected final AsynchronousSocketChannel channel;

    protected CommonChannelHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void failed(Throwable throwable, BufferedMessage bufferedMessage) {
        handleClose();
    }

    protected void handleClose(){
        try {
            channel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}
