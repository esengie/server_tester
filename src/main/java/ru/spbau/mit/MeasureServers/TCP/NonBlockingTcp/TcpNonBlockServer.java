package ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

public class TcpNonBlockServer extends MeasureServer {
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Thread serverThread = new Thread(new ServerThread());
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    private List<ChangeRequest> pendingChanges = new ArrayList<>();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private Map<SocketChannel, ByteBuffer> pendingData = new HashMap<>();

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (!isStopped()) {
                try {
                    synchronized (changeRequests) {
                        Iterator changes = changeRequests.iterator();
                        while (changes.hasNext()) {
                            ChangeRequest change = (ChangeRequest) changes.next();
                            switch (change.type) {
                                case ChangeRequest.CHANGEOPS:
                                    SelectionKey key = change.socket.keyFor(this.selector);
                                    key.interestOps(change.ops);
                            }
                        }
                        changeRequests.clear();
                    }

                    // Wait for an event one of the registered channels
                    selector.select();

                    // Iterate over the set of keys for which events are available
                    Iterator selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = (SelectionKey) selectedKeys.next();
                        selectedKeys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }

                } catch (IOException e) {
                    if (isStopped()) {
                        break;
                    }
                }
            }
        }

    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
//        this.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }

    public void send(SocketChannel socket, byte[] data) {
        synchronized (pendingChanges) {
            // Indicate we want the interest ops set changed
            pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (pendingData) {
                List queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        selector.wakeup();
    }


    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    @Override
    protected void startHelper() throws IOException {
        selector = SelectorProvider.provider().openSelector();

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        InetSocketAddress isa = new InetSocketAddress("localhost",
                ProtocolConstants.SERVER_PORT);
        serverChannel.socket().bind(isa);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        serverThread.start();
    }

    @Override
    protected void stopHelper() throws IOException {
        serverChannel.close();
    }
}
