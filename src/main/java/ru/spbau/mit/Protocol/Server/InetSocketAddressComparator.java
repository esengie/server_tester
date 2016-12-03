package ru.spbau.mit.Protocol.Server;

import java.net.InetSocketAddress;

public class InetSocketAddressComparator {
    public static int compare(InetSocketAddress o1, InetSocketAddress o2) {
        if (o1 == o2) {
            return 0;
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }
}