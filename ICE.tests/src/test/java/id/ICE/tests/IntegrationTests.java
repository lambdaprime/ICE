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
package id.ICE.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import id.ICE.MessageResponse;
import id.ICE.MessageServer;
import id.ICE.MessageService;
import id.ICE.handlers.MessageReceiver;
import id.ICE.impl.ObjectsFactory;
import id.ICE.scanners.FixedLengthMessageScanner;
import id.ICE.scanners.MessageScanner;
import id.ICE.scanners.VarLengthMessageScanner;
import id.ICE.tests.services.echo.EchoService;
import id.xfunction.XUtils;
import id.xfunction.concurrent.DelayedCompletableFuture;

public class IntegrationTests {

    private static final int PORT = 1234;

    /**
     * Test that server sends all data back correctly.
     * It does not cover cases when data sent in portions (it may happen when amount
     * of data to be sent exceeds what system can send in one operation).
     */
    @Test
    public void test_server_send() {
        String data = "g".repeat(1_000);
        MessageService handler = req -> {
            return new DelayedCompletableFuture<>(new MessageResponse(ByteBuffer.wrap(data.getBytes())), 3000);
        };
        try (var server = new MessageServer(handler, buf -> buf.limit())) {
            server
                .withNumberOfThreads(1)
                .withPort(PORT);
            server.run();
            var ch = SocketChannel.open();
            ch.connect(new InetSocketAddress(PORT));
            ch.write(ByteBuffer.wrap(data.getBytes()));
            ByteBuffer buf = ByteBuffer.wrap(new byte[data.length()]);
            while (ch.read(buf) > 0);
            Assertions.assertArrayEquals(data.getBytes(), buf.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testSingleThread() {
        test(1);
    }

    @Test
    public void testMultiThread() {
        test(100);
    }

    @Test
    public void test_close() throws Exception {
        var server = new MessageServer(req -> null, new FixedLengthMessageScanner(0));
        server.run();
        server.close();
    }
    
    @Test
    public void test_withIgnoreNextRequest_withCloseOnResponse() {
        var data = List.of("hello", "dash", "berlin");
        MessageService handler = new StreamService(data) {
            ByteBuffer orig;
            @Override
            public CompletableFuture<MessageResponse> process(
                    ByteBuffer message) {
                Assertions.assertEquals(0, message.position());
                if (orig == null) orig = message;
                // change position
                message.get();
                System.out.println();
                Assertions.assertEquals(orig, message);
                return super.process(message);
            }
        };
        try (MessageServer server = new MessageServer(handler, buf -> buf.limit());
                SocketChannel ch = SocketChannel.open()) {
            server
                .withNumberOfThreads(1)
                .withPort(PORT);
            server.run();
            ch.connect(new InetSocketAddress(PORT));
            ch.write(ByteBuffer.wrap("hello".getBytes()));
            for (var expectedMessage: data) {
                var receiver = new Receiver(ch);
                Assertions.assertEquals(expectedMessage, receiver.nextLine(expectedMessage.length()));
            }                
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * There was a bug that client closing the connection could cause
     * CPU increase due to MessageReceiver::completed being called indefinitely
     */
    @Test
    public void test_client_closed_connection() {
        var counter = new AtomicInteger();
        ObjectsFactory.setInstance(new TestObjectsFactory() {
            @Override
            public MessageReceiver createMessageReceiver(
                    AsynchronousSocketChannel channel, MessageScanner scanner) {
                return new MessageReceiver(channel, scanner) {
                    @Override
                    public void completed(Integer result,
                            AsynchronousSocketChannel channel) {
                        counter.incrementAndGet();
                        super.completed(result, channel);
                    }
                };
            }
        });
        try (var server = new MessageServer(new EchoService(), new FixedLengthMessageScanner(1000))) {
            server
                .withNumberOfThreads(1)
                .withPort(PORT);
            server.run();
            System.out.println("start sending");
            try (var ch = SocketChannel.open()) {
                ch.connect(new InetSocketAddress(PORT));
                ch.write(ByteBuffer.wrap("dd".getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("sent");
            XUtils.sleep(1_000);
            Assertions.assertEquals(2, counter.get());
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    /*
     * Test that service receives messages exactly as they are sent by the sender
     */
    private void test(int serverThreadPoolSize) {
        var sender = new Sender();
        var service = new AccumulatorService();
        try (var server = new MessageServer(service, buf -> buf.limit())) {
            server
                .withNumberOfThreads(serverThreadPoolSize)
                .withPort(PORT);
            server.run();
            Stream.generate(System::currentTimeMillis).limit(300)
                .map(l -> l.toString())
                .parallel()
                .forEach(sender::send);
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var sent = sender.sent.stream()
                .sorted()
                .toArray();
        var received = service.received.stream()
                .sorted()
                .toArray();
        Assertions.assertArrayEquals(sent, received);
    }

    /**
     * Test that ICE pass the error back to the service:
     * - if service set withErrorHandler on the message
     * - if client closed the connection abruptly and ICE failed to deliver
     * the service response to the client
     */
    @Test
    public void test_withErrorHandler() throws Exception {
        Throwable[] exception = new Throwable[1];
        Consumer<Throwable> errorHandler = exc -> exception[0] = exc;
        var service = new StreamService("hello") {
            @Override
            public CompletableFuture<MessageResponse> process(
                    ByteBuffer message) {
                return super.process(message).thenApply(response -> response.withErrorHandler(errorHandler));
            }
        };
        try (var server = new MessageServer(service, new VarLengthMessageScanner())) {
            server
                .withPort(PORT)
                .run();
            System.out.println("start sending");
            try (var ch = SocketChannel.open()) {
                ch.connect(new InetSocketAddress(PORT));
                ch.write(ByteBuffer.wrap("dd".getBytes()));
            }
            System.out.println("sent");
            XUtils.sleep(1_000);
            Assertions.assertNotNull(exception[0]);
            Assertions.assertTrue(exception[0] instanceof IOException);
        }        
    }

    /*
     * Sender which accumulates all sent data in internal collection.
     * Every time new data needs to be sent it will open a new connection.
     */
    private static class Sender {
        Collection<String> sent = new ConcurrentLinkedQueue<>();

        void send(String msg) {
            System.out.println("start sending");
            try (var ch = SocketChannel.open()) {
                ch.connect(new InetSocketAddress(PORT));
                if (ch.write(ByteBuffer.wrap(msg.getBytes())) == msg.length())
                    sent.add(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("sent");
        }
    }
}

