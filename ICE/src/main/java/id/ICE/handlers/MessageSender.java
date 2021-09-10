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
package id.ICE.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import id.xfunction.logging.XLogger;

/**
 * Async sends the message and notifies the caller thru the CompletableFuture.
 * This handler is not thread safe and designed to serve only one send request at a time.
 */
public class MessageSender implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private static final Logger LOGGER = XLogger.getLogger(MessageSender.class);
    private ByteBuffer message;
    private AsynchronousSocketChannel channel;
    private CompletableFuture<Void> future;
    private Consumer<Throwable> errorHandler;
    
    public MessageSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
        if (message.hasRemaining() && channel.isOpen()) {
            channel.write(message, null, this);
            return;
        }
        LOGGER.fine("Message sent");
        future.complete(null);
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
        errorHandler.accept(exc);
    }

    /**
     * Send data asynchronously and return the future which will be
     * completed once all data is sent.
     */
    public CompletableFuture<Void> send(ByteBuffer message, Consumer<Throwable> errorHandler) {
        LOGGER.fine("Sending message response");
        this.message = message;
        this.errorHandler = errorHandler;
        future = new CompletableFuture<>();
        channel.write(message, null, this);
        return future;
    }
}