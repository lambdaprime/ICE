package id.ICE.tests;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import id.ICE.MessageServer;
import id.ICE.MessageService;
import id.ICE.impl.DelayedCompletableFuture;

public class MessageServerTests {

    private static final int PORT = 1234;

    /**
     * Test that server sends all data back correctly
     */
    @Test
    public void test_server_send() {
        String data = "g".repeat(1_000);
        MessageService handler = req -> {
            return completedFuture(ByteBuffer.wrap(data.getBytes()));
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
    public void test_handler_delayed_completion() {
        String data = "g".repeat(1_000);
        MessageService handler = req -> {
            return new DelayedCompletableFuture<>(ByteBuffer.wrap(data.getBytes()), 3000);
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

    private void test(int serverThreadPoolSize) {
        var sender = new Sender();
        var receiver = new Receiver();
        try (var server = new MessageServer(receiver::receive, buf -> buf.limit())) {
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
//        received.add("ddd");
        var received = receiver.received.stream()
                .sorted()
                .toArray();
        Assertions.assertArrayEquals(sent, received);
    }

    /**
     * Receiver which accumulates all received data in internal collection
     */
    private static class Receiver {
        Collection<String> received = new ConcurrentLinkedQueue<>();

        CompletableFuture<ByteBuffer> receive(ByteBuffer req) {
            var message = new String(req.array());
            received.add(message);
            System.out.format("%d => %s\n", received.size(), message);
            return new DelayedCompletableFuture<>(null, 10, 50);
        }
    }

    /**
     * Sender which accumulates all sent data in internal collection
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

