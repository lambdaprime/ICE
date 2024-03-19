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

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Response message sent to the client
 *
 * @author lambdaprime intid@protonmail.com
 */
public class MessageRequest {

    private int connectionId;
    private Optional<ByteBuffer> message;

    /**
     * @param message Message request received from the client.
     */
    public MessageRequest(int clientId, Optional<ByteBuffer> message) {
        this.connectionId = clientId;
        this.message = message;
    }

    /**
     * Id of the connection to which this request belongs. All requests sent by the client in same
     * connection will have same connection id.
     */
    public int getConnectionId() {
        return connectionId;
    }

    /**
     * Request message as sent by the client. If service requested to ignore next client request
     * (see {@link MessageResponse#withIgnoreNextRequest}) then this message will be empty.
     */
    public Optional<ByteBuffer> getMessage() {
        return message;
    }
}
