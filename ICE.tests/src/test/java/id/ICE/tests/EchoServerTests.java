package id.ICE.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.stream.IntStream.range;

import id.ICE.AsyncServer;
import id.ICE.scanners.DelimiterMessageScanner;

public class EchoServerTests {

    private static final int PORT = 1234;

    @Test
    public void test_one_read_many_messages() throws Exception {
        try (var server = new AsyncServer(this::handle, new DelimiterMessageScanner((byte)'\n'), PORT, 1)) {
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
        try (var server = new AsyncServer(this::handle, new DelimiterMessageScanner((byte)'\n'), PORT, 7)) {
            server.run();
            range(0, 100).forEach(i -> {
                ForkJoinPool.commonPool().submit(this::testInteraction);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends 100 different messages in one connection and checks
     * the responses to each of them
     */
    private void testInteraction() {
        var c = new AtomicInteger();
        try (var channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(PORT));
            range(0, 100).forEach((i) -> {
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
    
    private CompletableFuture<ByteBuffer> handle(ByteBuffer message) {
        byte[] b = new byte[message.capacity() + 1];
        message.get(b, 0, message.capacity());
        b[b.length - 1] = '\n';
        System.out.println(new String(message.array()));
        return CompletableFuture.completedFuture(ByteBuffer.wrap(b));
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

}

