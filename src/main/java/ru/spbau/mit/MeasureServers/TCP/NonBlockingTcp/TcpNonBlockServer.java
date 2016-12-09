package ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp;

import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.BufferedMessage;
import ru.spbau.mit.MeasureServers.TCP.BufferedMessage.MessageState;
import ru.spbau.mit.MeasureServers.TCP.Workers.ByteBufferWorkers.NonBlockWorker;
import ru.spbau.mit.Protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpNonBlockServer extends MeasureServer {
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Thread serverThread = new Thread(new ServerThread());
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            while (serverChannel.isOpen() && !isStopped()) {
                try {
                    int ready = selector.select();
                    if(ready == 0) continue;

                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = selectedKeys.next();
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
        BufferedMessage msg = (BufferedMessage) key.attachment();

        int numRead = 0;
        try {
            switch(msg.state){
                case EMPTY:
                    numRead = socketChannel.read(msg.sizeBuf);
                    if (msg.sizeBuf.hasRemaining())
                        break;
                    // we've read sizeBuf
                    msg.state = MessageState.READING_DATA;
                    msg.sizeBuf.flip();
                    msg.sizeBuf.mark();
                    msg.data = ByteBuffer.allocate(msg.sizeBuf.getInt());
                    msg.sizeBuf.reset();
                case READING_DATA:
                    numRead += socketChannel.read(msg.data);
                    if (msg.data.hasRemaining())
                        break;
                    msg.state = MessageState.PROCESSING;
                    msg.data.flip();
                    pool.execute(new NonBlockWorker(selector, key, msg));
            }
        } catch (IOException e) {
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            socketChannel.close();
            key.cancel();
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        BufferedMessage msg = (BufferedMessage) key.attachment();

        switch (msg.state){
            case WAITING_TO_WRITE:
                socketChannel.write(msg.data);
                if (msg.data.hasRemaining())
                    break;
                msg.state = MessageState.EMPTY;
                msg.sizeBuf.clear();
                key.interestOps(SelectionKey.OP_READ);
                selector.wakeup();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_READ, new BufferedMessage());
    }

    @Override
    protected void startHelper() throws IOException {
        selector = SelectorProvider.provider().openSelector();

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.bind(new InetSocketAddress("localhost",
                ProtocolConstants.SERVER_PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        serverThread.start();
    }

    @Override
    protected void stopHelper() throws IOException {
        serverChannel.close();
        pool.shutdown();
    }
}
