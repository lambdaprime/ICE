package id.ICE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import id.ICE.impl.Looper;
import id.ICE.impl.Utils;
import id.ICE.scanners.MessageScanner;
import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;

/**
 * Runs ICE server on a given port and manages all interaction
 * between clients and MessageService.
 */
public class MessageServer implements Runnable, AutoCloseable {

    private static final int DEFAULT_PORT = 12345;
    private static final Logger LOGGER = XLogger.getLogger(MessageServer.class);
    private Utils utils = new Utils();
    private MessageService service;
    private MessageScanner scanner;
    private int port;
    private int threads;
    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel channel;

    /**
     * @param service message service implementation which will process
     * all incoming messages
     * @param scanner scanner for messages to use
     */
    public MessageServer(MessageService service, MessageScanner scanner) {
        this.service = service;
        this.scanner = scanner;
        this.port = DEFAULT_PORT;
        this.threads = ForkJoinPool.getCommonPoolParallelism();
    }

    /**
     * Port where to run the server.
     * @see DEFAULT_PORT for default port.
     */
    public MessageServer withPort(int port) {
        this.port = port;
        return this;
    }

    public MessageServer withNumberOfThreads(int threads) {
        this.threads = threads;
        return this;
    }

    /**
     * Starts ICE server.
     * 
     * It opens a given port and starts to accept connections.
     * For each new incoming connection it will run a looper.
     */
    @Override
    public void run() {
        try {
            runInternal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runInternal() throws IOException {
        group = AsynchronousChannelGroup.withFixedThreadPool(threads, new NamedThreadFactory("ICE-" + port));
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
                new Looper(group, ch, service, scanner).loop();
                LOGGER.fine("spawned looper");
            }
            public void failed(Throwable exc, Void att) {
                utils.handleException(exc);
            }
        });
    }

    /**
     * Calling close on server which is not started has no effect
     */
    @Override
    public void close() throws Exception {
        if (group == null) return;
        group.shutdown();
        LOGGER.fine("waiting");
        group.awaitTermination(1000, TimeUnit.MILLISECONDS);
        LOGGER.fine("awaken");
        group.shutdownNow();
        channel.close();
    }
}
