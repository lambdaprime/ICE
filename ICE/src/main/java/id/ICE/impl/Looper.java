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

/**
 * Looper which serves single client connection.
 */
public class Looper {

    private Utils utils = new Utils();
    private AsynchronousChannelGroup group;
    private MessageReceiver receiver;
    private MessageSender sender;
    private AsynchronousSocketChannel channel;
    private MessageService service;
    private MessageScanner scanner;
    private Optional<ByteBuffer> request = Optional.empty();
    private MessageResponse response = new MessageResponse(ByteBuffer.allocate(0));
    private ObjectsFactory factory = ObjectsFactory.getInstance();
    
    public Looper(AsynchronousChannelGroup group, AsynchronousSocketChannel channel,
            MessageService service, MessageScanner scanner) {
        this.group = group;
        this.service = service;
        this.channel = channel;
        this.scanner = scanner;
    }

    public void start() {
        sender = new MessageSender(channel);
        receiver = factory.createMessageReceiver(channel, scanner);
        loop();
    }

    private void loop() {
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
    
    private CompletableFuture<ByteBuffer> receive() {
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
        return sender.send(message.getMessage(),
            message.getErrorHandler().orElse(this::failed));
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

    private void failed(Throwable exc) {
        utils.handleException(exc);
    }
}
