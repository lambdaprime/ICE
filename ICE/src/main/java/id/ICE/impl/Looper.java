package id.ICE.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

import id.ICE.MessageService;
import id.ICE.handlers.MessageReceiver;
import id.ICE.handlers.MessageSender;
import id.ICE.scanners.MessageScanner;

public class Looper {

    private AsynchronousChannelGroup group;
    private MessageReceiver receiver;
    private MessageSender sender;
    private AsynchronousSocketChannel channel;
    private MessageService service;
    
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
        receiver.receive()
            .thenCompose(service::process)
            .thenCompose(this::send)
            .thenRun(() -> loop());
    }
    
    private CompletableFuture<Void> send(ByteBuffer message) {
        if (message == null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CompletableFuture.completedFuture(null);
        }
        return sender.send(message);
    }
}
