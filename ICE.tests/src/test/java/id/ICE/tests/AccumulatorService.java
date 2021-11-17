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
 * - lambdaprime <intid@protonmail.com>
 */
package id.ICE.tests;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import id.ICE.MessageRequest;
import id.ICE.MessageResponse;
import id.ICE.MessageService;
import id.xfunction.concurrent.DelayedCompletableFuture;

/**
 * Service which accumulates all received data into internal collection
 */
class AccumulatorService implements MessageService {
    Collection<String> received = new ConcurrentLinkedQueue<>();

    @Override
    public CompletableFuture<MessageResponse> process(MessageRequest req) {
        var message = new String(req.getMessage().get().array());
        received.add(message);
        System.out.format("%d => %s\n", received.size(), message);
        return new DelayedCompletableFuture<>(null, 10, 50);
    }
}