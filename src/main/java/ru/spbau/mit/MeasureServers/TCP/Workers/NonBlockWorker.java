package ru.spbau.mit.MeasureServers.TCP.Workers;


import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.ServerDataEvent;
import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.TcpNonBlockServer;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockWorker {
    public class EchoWorker implements Runnable {
        private Queue<ServerDataEvent> queue = new ConcurrentLinkedQueue<>();

        public void processData(TcpNonBlockServer server, SocketChannel socket, byte[] data, int count) {
            byte[] dataCopy = new byte[count];
            System.arraycopy(data, 0, dataCopy, 0, count);
            queue.add(new ServerDataEvent(server, socket, dataCopy));
        }

        public void run() {
            ServerDataEvent dataEvent;

            while(true) {
                // Wait for data to become available
                synchronized(queue) {
                    dataEvent = queue.poll();
                }

                // Return to sender
                dataEvent.server.send(dataEvent.socket, dataEvent.data);
            }
        }
    }
}
