package id.ICE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import id.ICE.scanners.MessageScanner;

public class Looper {

    private AsynchronousChannelGroup group;
    private MessageReceiver receiver;
    private MessageSender sender;
    private AsynchronousSocketChannel channel;
    private Function<ByteBuffer, CompletableFuture<ByteBuffer>> handler;
    
    public Looper(AsynchronousChannelGroup group, AsynchronousSocketChannel channel,
            Function<ByteBuffer, CompletableFuture<ByteBuffer>> handler, MessageScanner scanner) {
        this.group = group;
        this.handler = handler;
        this.channel = channel;
        sender = new MessageSender(channel);
        receiver = new MessageReceiver(channel, scanner);
    }
    
    public void loop() {
        if (!channel.isOpen())
            return;
        if (group.isShutdown())
            return;
        receiver.receive()
            .thenCompose(handler)
            .thenCompose(this::send)
            .thenRun(() -> loop());
    }
    
    private CompletableFuture<Void> send(ByteBuffer message) {
        if (message == null) {
            try {
                channel.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return CompletableFuture.completedFuture(null);
        }
        return sender.send(message);
    }
}
