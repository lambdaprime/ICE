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
package id.ICE;

import java.nio.ByteBuffer;

/**
 * Response message sent to the client
 */
public class MessageResponse {

    private ByteBuffer message;
    private boolean closeOnResponse;
    private boolean ignoreNextRequest;
    
    /**
     * @param message message to be sent to the client
     */
    public MessageResponse(ByteBuffer message) {
        this.message = message;
    }

    /**
     * <p>Close the connection after response is sent.</p>
     */
    public MessageResponse withCloseOnResponse() {
        this.closeOnResponse = true;
        return this;
    }
    
    /**
     * <p>By default every time your service provides a *response* message, ICE
     * sends it back to the client and waits for the next *request* message from
     * it (see ICE looper description).</p>
     * 
     * <p>With this flag set ICE will not wait for next request from the client
     * instead it will proceed to {@link MessageService} for another message which needs
     * to be send to the client.</p>
     * 
     * <p>You may want to use it when you need your service to send stream of messages.</p>
     */
    public MessageResponse withIgnoreNextRequest() {
        this.ignoreNextRequest = true;
        return this;
    }
    
    /**
     * Message response
     */
    public ByteBuffer getMessage() {
        return message;
    }

    /**
     * Return whether {@link withCloseOnResponse} is set or not
     */
    public boolean shouldCloseOnResponse() {
        return closeOnResponse;
    }

    /**
     * Return whether {@link withIgnoreNextRequest} is set or not
     */
    public boolean shouldIgnoreNextRequest() {
        return ignoreNextRequest;
    }
}
