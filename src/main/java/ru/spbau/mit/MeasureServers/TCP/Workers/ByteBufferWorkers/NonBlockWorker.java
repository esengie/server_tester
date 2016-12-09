package ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers;

import ru.spbau.mit.MeasureServers.TCP.BufferedMessage;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NonBlockWorker extends CommonWorker implements Runnable {
    private final Selector selector;
    private final SelectionKey key;

    public NonBlockWorker(Selector selector, SelectionKey key, BufferedMessage msg) {
        super(msg);
        this.selector = selector;
        this.key = key;
    }

    @Override
    public void run() {
        key.interestOps(SelectionKey.OP_WRITE);
        super.run();
        selector.wakeup();
    }
}
