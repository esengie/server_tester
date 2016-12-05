package ru.spbau.mit.MeasureServers;

import ru.spbau.mit.MeasureServers.TCP.TcpServer;
import ru.spbau.mit.MeasureServers.UDP.UdpAsyncServer;
import ru.spbau.mit.MeasureServers.UDP.UdpServer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ServerFactory {
    public MeasureServer createServer(ServerType type){
        switch (type){
            case TCP_PERM_CACHED_POOL:
            case TCP_PERM_THREADS:
            case TCP_TEMP_SINGLE_THREAD:
                return new TcpServer(type);
            case UDP_FIXED_THREAD_POOL:
            case UDP_THREAD_PER_REQUEST:
                return new UdpServer(type);
            case UDP_ASYNC:
                throw new NotImplementedException();
            case TCP_PERM_NON_BLOCK:
                throw new NotImplementedException();
        }
        return null;
    }
}
