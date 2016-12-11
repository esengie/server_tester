package ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers;

import ru.spbau.mit.MeasureServers.Job;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.MessageState;
import ru.spbau.mit.Protocol.ByteProtocol;

import java.nio.ByteBuffer;
import java.util.List;

public class CommonWorker implements Runnable {
    protected final BufferedMessage msg;

    protected CommonWorker(BufferedMessage msg){
        this.msg = msg;
    }

    @Override
    public void run() {
        ByteProtocol protocol = new ByteProtocol();

        ByteBuffer buf = ByteBuffer.allocate(msg.sizeBuf.limit() + msg.data.limit());
        buf.put(msg.sizeBuf); buf.put(msg.data);
        List<Integer> lst = protocol.decodeArray(buf.array());

        Job job = MeasureServer.createJob(lst);
        msg.data = ByteBuffer.wrap(protocol.encodeArray(job.call()));
        msg.state = MessageState.WAITING_TO_WRITE;
    }
}
