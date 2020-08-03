package id.ICE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import id.ICE.impl.Looper;
import id.ICE.impl.Utils;
import id.ICE.scanners.MessageScanner;
import id.xfunction.logging.XLogger;

public class MessageServer implements Runnable, AutoCloseable {

    private static final Logger LOGGER = XLogger.getLogger(MessageServer.class);
    private Utils utils = new Utils();
    private MessageService handler;
    private MessageScanner scanner;
    private int port;
    private int threads;
    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel channel;

    public MessageServer(MessageService handler, MessageScanner scanner) {
        this.handler = handler;
        this.scanner = scanner;
        this.port = 12345;
        this.threads = ForkJoinPool.getCommonPoolParallelism();
    }

    public MessageServer withPort(int port) {
        this.port = port;
        return this;
    }

    public MessageServer withNumberOfThreads(int threads) {
        this.threads = threads;
        return this;
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
                LOGGER.fine("incoming connection");
                new Looper(group, ch, handler, scanner).loop();
                LOGGER.fine("spawned looper");
            }
            public void failed(Throwable exc, Void att) {
                utils.handleException(exc);
            }
        });
    }

    @Override
    public void close() throws Exception {
        group.shutdown();
        LOGGER.fine("waiting");
        group.awaitTermination(1000, TimeUnit.MILLISECONDS);
        LOGGER.fine("awaken");
        group.shutdownNow();
        channel.close();
    }
}
