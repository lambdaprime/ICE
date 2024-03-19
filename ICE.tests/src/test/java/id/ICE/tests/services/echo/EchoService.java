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
package id.ICE.tests.services.echo;

import id.ICE.MessageRequest;
import id.ICE.MessageResponse;
import id.ICE.MessageService;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Echo service receives a string from the client and sends it back.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class EchoService implements MessageService {
    @Override
    public CompletableFuture<MessageResponse> process(MessageRequest request) {
        // obtaining and printing data from the request
        var inputData = request.getMessage().get();
        System.out.println(new String(inputData.array()));

        // generating response with same data and sending back
        byte[] outputData = new byte[inputData.capacity()];
        inputData.get(outputData, 0, inputData.capacity());
        return CompletableFuture.completedFuture(new MessageResponse(ByteBuffer.wrap(outputData)));
    }
}
