package ru.spbau.mit.ServerSide;

public enum ServerType {
    TCP_PERM_THREADS, TCP_PERM_CACHED_POOL, TCP_PERM_NON_BLOCK, TCP_TEMP_SINGLE_THREAD,
    UDP_THREAD_PER_REQUEST, UDP_FIXED_THREAD_POOL, UDP_ASYNC
}
