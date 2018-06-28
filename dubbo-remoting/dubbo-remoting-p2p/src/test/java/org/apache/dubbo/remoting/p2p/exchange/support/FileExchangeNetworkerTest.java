/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.apache.dubbo.remoting.p2p.exchange.support;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.ExchangeChannel;
import org.apache.dubbo.remoting.exchange.ExchangeHandler;
import org.apache.dubbo.remoting.exchange.support.ExchangeHandlerAdapter;
import org.apache.dubbo.remoting.p2p.Group;
import org.apache.dubbo.remoting.p2p.Networkers;
import org.apache.dubbo.remoting.p2p.Peer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class FileExchangeNetworkerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        folder.create();
    }

    @After
    public void tearDown() {
        folder.delete();
    }

    @Test
    public void testJoin() throws RemotingException, InterruptedException, IOException {
        File file = folder.newFile();
        java.net.URL url = file.toURL();
        final URL groupURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());

        FileExchangeNetworker fileExchangeNetworker = new FileExchangeNetworker();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Peer peer1 = fileExchangeNetworker.lookup(groupURL)
                .join(URL.valueOf("dubbo://0.0.0.0:" + NetUtils.getAvailablePort()), new ExchangeHandlerAdapter() {
                    @Override
                    public CompletableFuture<Object> reply(ExchangeChannel channel, Object msg) throws RemotingException {
                        countDownLatch.countDown();
                        return super.reply(channel, msg);
                    }
                });
        Peer peer2 = fileExchangeNetworker.lookup(groupURL)
                .join(URL.valueOf("dubbo://0.0.0.0:" + NetUtils.getAvailablePort()), mock(ExchangeHandler.class));

        for (Channel channel : peer2.getChannels()) {
            channel.send("hello multicast exchange network!");
        }

        Group lookup = Networkers.lookup(groupURL);
        assertThat(lookup, not(nullValue()));

        assertThat(peer1, instanceOf(ExchangeServerPeer.class));

        peer1.close();
        peer2.close();
    }
}