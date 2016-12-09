package ru.spbau.mit.MeasureServers.TCP.AsyncTcp;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers.ReadHandler;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpAsyncServer extends MeasureServer {
    private AsynchronousServerSocketChannel serverChannel;

    @Override
    protected void startHelper() throws IOException {
        serverChannel = AsynchronousServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost",
                ProtocolConstants.SERVER_PORT));
        serverChannel.accept(new BufferedMessage(), new CompletionHandler<AsynchronousSocketChannel, BufferedMessage>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, BufferedMessage msg) {
                serverChannel.accept(new BufferedMessage(), this);
                channel.read(msg.sizeBuf, msg, new ReadHandler(channel));
            }

            @Override
            public void failed(Throwable throwable, BufferedMessage bufferedMessage) {
                //err
            }
        });
    }

    @Override
    protected void stopHelper() throws IOException {
        serverChannel.close();
        ReadHandler.shutdownPool();
    }
}
