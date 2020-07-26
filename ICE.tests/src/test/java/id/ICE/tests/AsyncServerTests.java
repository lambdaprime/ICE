package id.ICE.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import id.ICE.AsyncServer;
import id.ICE.Request;
import id.ICE.Response;

public class AsyncServerTests {

    private static final int PORT = 1234;

    @Test
    public void testSingleThread() {
        test(1);
    }

    @Test
    public void testMultiThread() {
        test(10);
    }

    /**
     * Test that server sends all data back correctly
     */
    @Test
    public void tesServerSend() {
        int length = 1_000_000;
        String data = "g".repeat(length);
        Function<Request, Response> handler = req -> {
            return new Response(data);
        };
        try (var server = new AsyncServer(handler , PORT, 1)) {
            server.run();
            var ch = SocketChannel.open();
            ch.connect(new InetSocketAddress(PORT));
            ch.write(ByteBuffer.wrap("f".getBytes()));
            ByteBuffer buf = ByteBuffer.wrap(new byte[length]);
            while (ch.read(buf) > 0);
            Assertions.assertArrayEquals(data.getBytes(), buf.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void test(int serverThreadPoolSize) {
        var sender = new Sender();
        var receiver = new Receiver();
        try (var server = new AsyncServer(receiver::receive, PORT, serverThreadPoolSize)) {
            server.run();
            Stream.generate(System::currentTimeMillis).limit(1000)
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

    static class Receiver {
        Collection<String> received = new ConcurrentLinkedQueue<>();

        Response receive(Request req) {
            received.add(req.data);
            //System.out.println(received.size());
            return null;
        }
    }

    static class Sender {
        Collection<String> sent = new ConcurrentLinkedQueue<>();

        void send(String msg) {
    //        System.out.println("start sending");
            try {
                var ch = SocketChannel.open();
                ch.connect(new InetSocketAddress(PORT));
                if (ch.write(ByteBuffer.wrap(msg.getBytes())) == msg.length())
                    sent.add(msg);
                
                
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    //        System.out.println("sent");
        }
    }
}

