package id.ICE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AsyncServer implements Runnable, AutoCloseable {

    private Utils utils = new Utils();
    private Function<ByteBuffer, CompletableFuture<ByteBuffer>> handler;
    private MessageScanner scanner;
    private int port;
    private int threads;
    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel channel;

    public AsyncServer(Function<ByteBuffer, CompletableFuture<ByteBuffer>> handler,
            MessageScanner scanner, int port, int threads) {
        this.handler = handler;
        this.scanner = scanner;
        this.port = port;
        this.threads = threads;
    }

    @Override
    public void run() {
        try {
            runInternal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runInternal() throws IOException {
        group = AsynchronousChannelGroup.withFixedThreadPool(threads, Executors.defaultThreadFactory());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                group.shutdown();
            }
        });
        channel = AsynchronousServerSocketChannel.open(group)
            .bind(new InetSocketAddress(port));
        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            public void completed(AsynchronousSocketChannel ch, Void att) {
                if (!group.isShutdown())
                    channel.accept(null, this);
                System.out.println("incoming connection");
                new Looper(group, ch, handler, scanner).loop();
                System.out.println("spawned looper");
            }
            public void failed(Throwable exc, Void att) {
                utils.handleException(exc);
            }
        });
    }

    @Override
    public void close() throws Exception {
        group.shutdown();
        System.out.println("waiting");
        group.awaitTermination(1000, TimeUnit.MILLISECONDS);
        System.out.println("awaken");
        group.shutdownNow();
    }
}
