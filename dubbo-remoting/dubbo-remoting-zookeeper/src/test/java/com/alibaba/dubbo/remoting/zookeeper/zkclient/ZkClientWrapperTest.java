package com.alibaba.dubbo.remoting.zookeeper.zkclient;

import com.alibaba.dubbo.common.utils.NetUtils;
import org.I0Itec.zkclient.IZkChildListener;
import org.apache.curator.test.TestingServer;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ZkClientWrapperTest {
    private TestingServer zkServer;
    private ZkClientWrapper zkClientWrapper;

    @Before
    public void setUp() throws Exception {
        int zkServerPort = NetUtils.getAvailablePort();
        zkServer = new TestingServer(zkServerPort, true);
        zkClientWrapper = new ZkClientWrapper("127.0.0.1:" + zkServerPort, 10000);
    }

    @Test
    public void testConnectedStatus() {
        boolean connected = zkClientWrapper.isConnected();
        assertThat(connected, is(false));
        zkClientWrapper.start();

        IZkChildListener listener = mock(IZkChildListener.class);
        zkClientWrapper.subscribeChildChanges("/path", listener);
        zkClientWrapper.unsubscribeChildChanges("/path", listener);
    }
}