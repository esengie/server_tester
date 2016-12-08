package ru.spbau.mit.MeasureServers.TCP.Workers;

import ru.spbau.mit.MeasureServers.Job;
import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.MessageState;
import ru.spbau.mit.Protocol.ByteProtocol;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;

public class NonBlockWorker implements Runnable {
    private final Selector selector;
    private final SelectionKey key;
    private final BufferedMessage msg;

    public NonBlockWorker(Selector selector, SelectionKey key, BufferedMessage msg) {
        this.selector = selector;
        this.key = key;
        this.msg = msg;
    }

    @Override
    public void run() {
        ByteProtocol protocol = new ByteProtocol();

        ByteBuffer buf = ByteBuffer.allocate(msg.sizeBuf.limit() + msg.data.limit());
        buf.put(msg.sizeBuf); buf.put(msg.data);
        List<Integer> lst = protocol.decodeArray(buf.array());

        Job job = new Job(lst);
        msg.data = ByteBuffer.wrap(protocol.encodeArray(job.call()));

        key.interestOps(SelectionKey.OP_WRITE);
        msg.state = MessageState.WAITING_TO_WRITE;
        selector.wakeup();
    }
}
