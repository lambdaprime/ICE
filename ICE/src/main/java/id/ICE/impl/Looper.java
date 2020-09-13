package id.ICE.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import id.ICE.MessageResponse;
import id.ICE.MessageService;
import id.ICE.handlers.MessageReceiver;
import id.ICE.handlers.MessageSender;
import id.ICE.scanners.MessageScanner;

/*
 * Looper which serves single client connection.
 */
public class Looper {

    private Utils utils = new Utils();
    private AsynchronousChannelGroup group;
    private MessageReceiver receiver;
    private MessageSender sender;
    private AsynchronousSocketChannel channel;
    private MessageService service;
    private Optional<ByteBuffer> request = Optional.empty();
    private MessageResponse response = new MessageResponse(ByteBuffer.allocate(0));
    
    public Looper(AsynchronousChannelGroup group, AsynchronousSocketChannel channel,
            MessageService service, MessageScanner scanner) {
        this.group = group;
        this.service = service;
        this.channel = channel;
        sender = new MessageSender(channel);
        receiver = new MessageReceiver(channel, scanner);
    }
    
    public void loop() {
        if (!channel.isOpen())
            return;
        if (group.isShutdown())
            return;
        
        receive()
            .thenCompose(service::process)
            .thenCompose(this::send)
            .whenComplete(this::onComplete)
            .thenRun(() -> loop());
    }
    
    public CompletableFuture<ByteBuffer> receive() {
        if (request.isEmpty())
            return receiver.receive().whenComplete((msg, exc) -> {
                request = Optional.of(msg.duplicate());
            });
        return CompletableFuture.completedFuture(request.get().duplicate());
    }
    
    private CompletableFuture<Void> send(MessageResponse message) {
        response = message;
        if (message == null) {
            closeChannel();
            return CompletableFuture.completedFuture(null);
        }
        if (!message.shouldIgnoreNextRequest())
            request = Optional.empty();
        return sender.send(message.getMessage());
    }
    
    private void onComplete(Void result, Throwable exc) {
        if (response.shouldCloseOnResponse())
            closeChannel();
        if (exc != null) {
            utils.handleException(exc);
        }
    }
    
    private void closeChannel() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
