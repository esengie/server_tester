package ru.spbau.mit.TorrentClient;

import com.google.protobuf.MessageLite;

import java.util.ArrayList;
import java.util.List;
import ru.spbau.mit.protocol.Messages.*;

public class TorrentClient1 {
    public static void main(String[] args) {
        List<MessageLite> list = new ArrayList<>();
        list.add(ArrayMessage.getDefaultInstance());
    }
}
