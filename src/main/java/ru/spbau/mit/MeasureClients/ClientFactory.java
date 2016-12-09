package ru.spbau.mit.MeasureClients;

import ru.spbau.mit.MeasureServers.ServerType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ClientFactory {
    public static MeasureClient createClient(ServerType type){
        switch (type){
            case TCP_PERM_CACHED_POOL:
            case TCP_PERM_NON_BLOCK:
            case TCP_PERM_ASYNC:
            case TCP_PERM_THREADS:
            case TCP_TEMP_SINGLE_THREAD:
                return new TcpClient();
            case UDP_FIXED_THREAD_POOL:
            case UDP_THREAD_PER_REQUEST:
                return new UdpClient();
            default:
                throw new NotImplementedException();
        }
    }
}
