package ru.spbau.mit.ClientSide;

import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ru.spbau.mit.ProtoMessage.Messages.ArrayMessage;

public class PermTcpClient implements Clients {
    public static void main(String[] args) {
        List<MessageLite> list = new ArrayList<>();
        list.add(ArrayMessage.getDefaultInstance());
    }

    @Override
    public void connect(String hostName) throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public List<Integer> executeRequest() throws IOException {
        return null;
    }
}
