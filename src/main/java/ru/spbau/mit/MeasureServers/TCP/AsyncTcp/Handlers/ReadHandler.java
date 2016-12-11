package ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.MessageState;
import ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers.AsyncWorker;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadHandler extends CommonChannelHandler {
    static ExecutorService pool;
    private int logID;

    public ReadHandler(MeasureServer server, AsynchronousSocketChannel channel) {
        super(server, channel);
    }

    @Override
    public void completed(Integer res, BufferedMessage msg) {
        if (res == -1) {
            handleClose();
        }

        switch (msg.state) {
            case EMPTY:
                if (msg.sizeBuf.hasRemaining()) {
                    channel.read(msg.sizeBuf, msg, this);
                    return;
                }
                msg.state = MessageState.READING_DATA;
                msg.sizeBuf.flip();
                msg.sizeBuf.mark();
                msg.data = ByteBuffer.allocate(msg.sizeBuf.getInt());
                msg.sizeBuf.reset();

                server.clientLog.logStart(msg.logID);
            case READING_DATA:
                if (msg.data.hasRemaining()) {
                    channel.read(msg.data, msg, this);
                    return;
                }
                msg.state = MessageState.PROCESSING;
                msg.data.flip();
                pool.execute(new AsyncWorker(server, channel, msg));
        }
    }

    public static void startupPool() {
        pool = Executors.newFixedThreadPool(10);
    }

    public static void shutdownPool() {
        pool.shutdownNow();
    }
}
