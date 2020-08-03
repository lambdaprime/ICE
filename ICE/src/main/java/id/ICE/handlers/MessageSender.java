package id.ICE.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import id.ICE.impl.Utils;

/**
 * Async sends the message and notifies the caller thru the CompletableFuture
 */
public class MessageSender implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private Utils utils = new Utils();
    private ByteBuffer message;
    private AsynchronousSocketChannel channel;
    private CompletableFuture<Void> future;
    
    public MessageSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
        if (message.hasRemaining() && channel.isOpen()) 
            channel.write(message, null, this);
        future.complete(null);
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
        utils.handleException(exc);
    }

    /**
     * Send data
     */
    public CompletableFuture<Void> send(ByteBuffer message) {
        this.message = message;
        future = new CompletableFuture<>();
        channel.write(message, null, this);
        return future;
    }
}