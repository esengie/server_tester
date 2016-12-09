package ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers;

import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.Handlers.WriteHandler;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

import java.nio.channels.AsynchronousSocketChannel;

public class AsyncWorker extends CommonWorker implements Runnable {
    private final AsynchronousSocketChannel channel;

    public AsyncWorker(AsynchronousSocketChannel channel, BufferedMessage msg) {
        super(msg);
        this.channel = channel;
    }

    @Override
    public void run() {
        super.run();
        channel.write(msg.data, msg, new WriteHandler(channel));
    }
}
