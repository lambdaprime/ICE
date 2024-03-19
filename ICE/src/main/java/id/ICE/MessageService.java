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
package id.ICE;

import java.util.concurrent.CompletableFuture;

/**
 * Processing of client messages are done through this interface.
 *
 * <p>It needs to be thread safe because it can be called by multiple threads.
 *
 * <p>Every message in ICE represented as a ByteBuffer object. That allows you to get control of its
 * actual format.
 *
 * @author lambdaprime intid@protonmail.com
 */
@FunctionalInterface
public interface MessageService {

    /**
     * This method is being called by the server to process received messages.
     *
     * @param message Received client message.
     * @return once the returned future is complete the response will be processed and sent back to
     *     the client. Server decides what to do with the connection based on returned result:
     *     <ul>
     *       <li>if response is null the connection is closed
     *       <li>otherwise we will wait for next message from the client
     *     </ul>
     */
    CompletableFuture<MessageResponse> process(MessageRequest message);
}
