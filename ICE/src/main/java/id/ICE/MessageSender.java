package id.ICE;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

public class MessageSender extends CompletableFuture<Void> implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private Utils utils = new Utils();
    private ByteBuffer message;
    private AsynchronousSocketChannel channel;
    
    public MessageSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
        if (message.hasRemaining() && channel.isOpen()) 
            channel.write(message, null, this);
        complete(null);
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
        utils.handleException(exc);
    }

    public CompletableFuture<Void> send(ByteBuffer message) {
        this.message = message;
        channel.write(message, null, this);
        return this;
    }
}