package id.ICE.tests;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import id.ICE.MessageResponse;
import id.ICE.MessageServer;
import id.ICE.MessageService;
import id.ICE.scanners.FixedLengthMessageScanner;
import id.xfunction.concurrent.DelayedCompletableFuture;

public class MessageServerTests {

    private static final int PORT = 1234;

    /**
     * Test that server sends all data back correctly
     */
    @Test
    public void test_server_send() {
        String data = "g".repeat(1_000);
        MessageService handler = req -> {
            return completedFuture(new MessageResponse(ByteBuffer.wrap(data.getBytes())));
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

