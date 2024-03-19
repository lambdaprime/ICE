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
package id.ICE.impl;

import id.ICE.MessageService;
import id.ICE.handlers.MessageReceiver;
import id.ICE.scanners.MessageScanner;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This factory is a single point for managing all dependencies in ICE. It is used for tests.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class ObjectsFactory {

    private static ObjectsFactory instance = new ObjectsFactory();

    protected ObjectsFactory() {}

    public MessageReceiver createMessageReceiver(
            AsynchronousSocketChannel channel, MessageScanner scanner) {
        return new MessageReceiver(channel, scanner);
    }

    public MessageReceiver createMessageReceiver(
            AsynchronousSocketChannel channel, MessageScanner scanner, int bufSize) {
        return new MessageReceiver(channel, scanner, bufSize);
    }

    public Looper createLooper(
            AsynchronousChannelGroup group,
            AsynchronousSocketChannel channel,
            MessageService service,
            MessageScanner scanner) {
        return new Looper(group, channel, service, scanner);
    }

    public static ObjectsFactory getInstance() {
        return instance;
    }

    public static void setInstance(ObjectsFactory objectsFactory) {
        instance = objectsFactory;
    }
}
