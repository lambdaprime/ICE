package id.ICE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AsyncServer implements Runnable, AutoCloseable {

    private Function<Request, Response> handler;
    private int port;
    private int threads;
    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel channel;

    public AsyncServer(Function<Request, Response> handler, int port, int threads) {
        this.handler = handler;
        this.port = port;
        this.threads = threads;
    }

    private class ResponseHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        ByteBuffer response;
        
        public ResponseHandler(ByteBuffer response) {
            this.response = response;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel channel) {
            handleException(exc);
        }

        public void process(AsynchronousSocketChannel channel) {
            channel.write(response, null, this);            
        }
    }

    private class RequestHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        ByteBuffer buf = ByteBuffer.wrap(new byte[256]);

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            var res = handler.apply(new Request(new String(buf.array(), 0, buf.position())));
            buf.clear();
            if (res == null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (res.data != null && channel.isOpen())
                new ResponseHandler(ByteBuffer.wrap(res.data.getBytes())).process(channel);
            if (channel.isOpen())
                channel.read(buf, channel, this);
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel channel) {
            handleException(exc);
        }

        public void process(AsynchronousSocketChannel ch) {
            ch.read(buf, ch, this);
        }
    }

    @Override
    public void run() {
        try {
            runInternal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleException(Throwable exc) {
        System.out.println("got exception");
        if (exc instanceof AsynchronousCloseException)
            return;
        System.err.println(exc);

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
                    var handler = new RequestHandler();
                    handler.process(ch);
                    if (!group.isShutdown())
                        channel.accept(null, this);
                }
                public void failed(Throwable exc, Void att) {
                    handleException(exc);
                }
            });
    }

    @Override
    public void close() throws Exception {
//        Thread.sleep(5000);
        group.shutdown();
        System.out.println("waiting");
        group.awaitTermination(1000, TimeUnit.MILLISECONDS);
        System.out.println("awaken");
        group.shutdownNow();
    }
}
