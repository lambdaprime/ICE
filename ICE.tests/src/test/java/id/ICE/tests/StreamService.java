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
package id.ICE.tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import id.ICE.MessageResponse;
import id.ICE.MessageService;

/*
 * Service which streams given messages to the client without
 * waiting any reply from the client.
 */
class StreamService implements MessageService {
    private final List<String> msgs;
    private boolean infinite;

    public StreamService(List<String> msgs) {
        this.msgs = new ArrayList<>(msgs);
    }

    /**
     * Stream data infinite number of times
     */
    public StreamService(String msg) {
        msgs = new ArrayList<>();
        msgs.add(msg);
        infinite = true;
    }

    @Override
    public CompletableFuture<MessageResponse> process(
            ByteBuffer message) {
        String msg = msgs.get(0);
        if (!infinite)
            msgs.remove(0);
        var response = new MessageResponse(ByteBuffer.wrap(msg.getBytes()))
                .withIgnoreNextRequest();
        if (msgs.isEmpty())
            response.withCloseOnResponse();
        return CompletableFuture.completedFuture(response);
    }
}