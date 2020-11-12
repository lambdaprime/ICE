/*
 * Copyright 2020 ICE project
 * 
 * Website: https://github.com/lambdaprime/ICE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Authors:
 * - lambdaprime <id.blackmesa@gmail.com>
 */
package id.ICE.tests.services.echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.stream.IntStream.range;

import id.ICE.MessageServer;
import id.ICE.scanners.NewLineMessageScanner;

public class EchoServiceTests {

    private static final int PORT = 1234;

    @Test
    public void test_one_client() throws Exception {
        try (var server = new MessageServer(new EchoService(), new NewLineMessageScanner())) {
            server
                .withNumberOfThreads(1)
                .withPort(PORT);
            server.run();

            var ch1 = SocketChannel.open();
            ch1.connect(new InetSocketAddress(PORT));
            send(ch1, "hello");
            Assertions.assertEquals("hello", receive(5, ch1));
            send(ch1, "world");
            Assertions.assertEquals("world", receive(5, ch1));
            
            var ch2 = SocketChannel.open();
            ch2.connect(new InetSocketAddress(PORT));
            send(ch1, "test");
            Assertions.assertEquals("test", receive(4, ch1));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_concurrency() throws Exception {
        try (var server = new MessageServer(new EchoService(), new NewLineMessageScanner())) {
            server
                .withNumberOfThreads(7)
                .withPort(PORT);
            server.run();
            var c = new AtomicInteger();
            range(0, 100).forEach(i -> {
                ForkJoinPool.commonPool().submit(() -> testInteraction(c, 100));
            });
            while (c.get() < 10000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends n different messages in one connection and checks
     * the responses to each of them
     */
    private void testInteraction(AtomicInteger c, int n) {
        try (var channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(PORT));
            range(0, n).forEach((i) -> {
                String data = "a".repeat((int) (Math.random() * 1000));
                send(channel, data);
                Assertions.assertEquals(data, receive(data.length(), channel));
                c.incrementAndGet();
            });
            Assertions.assertEquals(100, c.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String receive(int len, SocketChannel ch) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[len + 1]);
        try {
            ch.read(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(buf.array()).replace("\n", "");
    }

    private void send(SocketChannel channel, String message) {
        message += "\n";
        try {
            channel.write(ByteBuffer.wrap(message.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try (var server = new MessageServer(new EchoService(), new NewLineMessageScanner())) {
            server
                .withNumberOfThreads(1)
                .withPort(10007);
            server.run();
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

