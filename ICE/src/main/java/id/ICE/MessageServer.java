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

import id.ICE.impl.ObjectsFactory;
import id.ICE.impl.Utils;
import id.ICE.scanners.MessageScanner;
import id.xfunction.concurrent.NamedThreadFactory;
import id.xfunction.logging.XLogger;

/**
 * Runs ICE server on a given port and manages all interaction
 * between clients and {@link MessageService}.
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
    private ObjectsFactory factory = ObjectsFactory.getInstance();

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
                factory.createLooper(group, ch, service, scanner).start();
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
