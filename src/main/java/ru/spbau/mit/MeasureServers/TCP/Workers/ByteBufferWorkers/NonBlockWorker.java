package ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NonBlockWorker extends CommonWorker implements Runnable {
    private final Selector selector;
    private final SelectionKey key;

    public NonBlockWorker(MeasureServer server, Selector selector, SelectionKey key, BufferedMessage msg) {
        super(server, msg);
        this.selector = selector;
        this.key = key;
    }

    @Override
    public void run() {
        super.run();
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }
}
