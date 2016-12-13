package ru.spbau.mit.CreationAndConfigs;

import java.util.Arrays;
import java.util.List;

/**
 * We have a MUTE server type to mute the server on the server starter
 */
public enum ServerType {
    TCP_PERM_THREADS, TCP_PERM_CACHED_POOL,
    TCP_PERM_NON_BLOCK, TCP_TEMP_SINGLE_THREAD,
    TCP_PERM_ASYNC,
    UDP_THREAD_PER_REQUEST, UDP_FIXED_THREAD_POOL,
    MUTE;

    public static List<ServerType> validValues() {
        return Arrays.asList(ServerType.values()).subList(0, 7);
    }
}
