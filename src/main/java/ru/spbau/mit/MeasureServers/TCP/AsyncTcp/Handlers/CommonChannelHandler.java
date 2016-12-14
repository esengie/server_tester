package ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.TcpAsyncServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The common parts of a read/write handler, there were no server before measurements
 * But, alas, it contains all the Uid generators!
 */
abstract class CommonChannelHandler implements CompletionHandler<Integer, BufferedMessage> {
    private static final Logger logger = Logger.getLogger(TcpAsyncServer.class.getName());
    final AsynchronousSocketChannel channel;
    final MeasureServer server;

    CommonChannelHandler(MeasureServer server, AsynchronousSocketChannel channel) {
        this.server = server;
        this.channel = channel;
    }

    @Override
    public void failed(Throwable throwable, BufferedMessage bufferedMessage) {
        handleClose();
    }

    void handleClose() {
        try {
            channel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}
