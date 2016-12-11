package ru.spbau.mit.CreationAndConfigs;

import ru.spbau.mit.MeasureClients.MeasureClient;
import ru.spbau.mit.MeasureClients.TcpClientPerm;
import ru.spbau.mit.MeasureClients.TcpClientTemp;
import ru.spbau.mit.MeasureClients.UdpClient;
import ru.spbau.mit.MeasureServers.MeasureServer;
import ru.spbau.mit.MeasureServers.TCP.AsyncTcp.TcpAsyncServer;
import ru.spbau.mit.MeasureServers.TCP.NonBlockingTcp.TcpNonBlockServer;
import ru.spbau.mit.MeasureServers.TCP.TcpServer;
import ru.spbau.mit.MeasureServers.UDP.UdpServer;
import ru.spbau.mit.Tester.Timing.ServerLogger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ClientServerFactory {
    public static MeasureServer createServer(ServerType type) {
        switch (type) {
            case TCP_PERM_CACHED_POOL:
            case TCP_PERM_THREADS:
            case TCP_TEMP_SINGLE_THREAD:
                return new TcpServer(type);
            case UDP_FIXED_THREAD_POOL:
            case UDP_THREAD_PER_REQUEST:
                return new UdpServer(type);
            case TCP_PERM_ASYNC:
                return new TcpAsyncServer();
            case TCP_PERM_NON_BLOCK:
                return new TcpNonBlockServer();
            default:
                throw new NotImplementedException();
        }
    }
    public static MeasureClient createClient(ServerType type){
        switch (type){
            case TCP_PERM_CACHED_POOL:
            case TCP_PERM_NON_BLOCK:
            case TCP_PERM_ASYNC:
            case TCP_PERM_THREADS:
                return new TcpClientPerm();
            case TCP_TEMP_SINGLE_THREAD:
                return new TcpClientTemp();
            case UDP_FIXED_THREAD_POOL:
            case UDP_THREAD_PER_REQUEST:
                return new UdpClient();
            default:
                throw new NotImplementedException();
        }
    }
}
