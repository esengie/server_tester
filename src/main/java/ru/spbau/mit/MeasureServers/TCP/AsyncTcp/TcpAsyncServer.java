package ru.spbau.mit.MeasureServers.TCP.AsyncTcp;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers.ReadHandler;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple tcp async server
 * <p>
 * We're dumping the data for sorting in the fixed pool,
 * Didn't want to thread it through the whole execution so it lives in the readHandler class - bad?
 */
public class TcpAsyncServer extends MeasureServer {
    private static final Logger logger = Logger.getLogger(TcpAsyncServer.class.getName());

    private AsynchronousServerSocketChannel serverChannel;

    @Override
    protected void startHelper() throws IOException {
        ReadHandler.startupPool();
        serverChannel = AsynchronousServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(ProtocolConstants.SERVER_PORT));

        serverChannel.accept(new BufferedMessage(clientIdGen.getAndIncrement()), new CompletionHandler<AsynchronousSocketChannel, BufferedMessage>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, BufferedMessage msg) {
                serverChannel.accept(new BufferedMessage(clientIdGen.getAndIncrement()), this);
                channel.read(msg.sizeBuf, msg, new ReadHandler(TcpAsyncServer.this, channel));
            }

            @Override
            public void failed(Throwable throwable, BufferedMessage bufferedMessage) {
                logger.log(Level.FINE, throwable.toString());
            }
        });
    }

    @Override
    protected void stopHelper() throws IOException {
        serverChannel.close();
        ReadHandler.shutdownPool();
    }
}
