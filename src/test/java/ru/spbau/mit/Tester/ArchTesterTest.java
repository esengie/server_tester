package ru.spbau.mit.Tester;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;

import java.io.IOException;

public class ArchTesterTest {

    private ArchTester tester;
    private UserConfig config;
    private final int timeout = 10000000;

    @BeforeClass
    public static void launchLauncherServer() throws InterruptedException {
        new Thread(new ArchTesterRemote()).start();
    }

    private void config(ServerType type) {
        config = UserConfig.builder()
                .arraySize(10)
                .clientsSize(250)
                .nextReqDelta(20)
                .requestsPerClient(10)
                .serverType(type)
                .varyingParameter(VaryingParameter.CLIENTS_PARALLEL)
                .build();
        tester = new ArchTester(config, "localhost");
    }

    private void runCommon() throws IOException {
        tester.testOnce();
        config.addToVarying(-200);
        tester.testOnce();
    }

    @Ignore
    @Test(timeout = timeout)
    public void testTempTCP() throws Exception {
        config(ServerType.TCP_TEMP_SINGLE_THREAD);
        runCommon();
    }

    @Ignore
    @Test(timeout = timeout)
    public void testThreadsPermTCP() throws Exception {
        config(ServerType.TCP_PERM_THREADS);
        runCommon();
    }

    @Ignore
    @Test(timeout = timeout)
    public void testCachedPoolPermTCP() throws Exception {
        config(ServerType.TCP_PERM_CACHED_POOL);
        runCommon();
    }

//    @Ignore
    @Test(timeout = timeout)
    public void testNonBlockPermTCP() throws Exception {
        config(ServerType.TCP_PERM_NON_BLOCK);
        runCommon();
    }

//    @Ignore
    @Test(timeout = timeout)
    public void testAsyncPermTCP() throws Exception {
        config(ServerType.TCP_PERM_ASYNC);
        runCommon();
    }

//    @Ignore
    @Test(timeout = timeout)
    public void testFixedPoolUDP() throws Exception {
        config(ServerType.UDP_FIXED_THREAD_POOL);
        runCommon();
    }

    @Ignore
    @Test(timeout = timeout)
    public void testThreadsUDP() throws Exception {
        config(ServerType.UDP_THREAD_PER_REQUEST);
        runCommon();
    }

}