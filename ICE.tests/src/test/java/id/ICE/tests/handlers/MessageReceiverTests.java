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
package id.ICE.tests.handlers;

import id.ICE.handlers.MessageReceiver;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class MessageReceiverTests {

    @Test
    public void test_one_read_many_messages() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("a".repeat(100).getBytes());
        var channel = new AsynchronousSocketChannelMock(buf, List.of(100));
        var receiver = new MessageReceiver(channel, b -> 10);
        byte[] message = "a".repeat(10).getBytes();
        for (int i = 0; i < 10; i++) {
            Assertions.assertArrayEquals(message, receiver.receive().get().array());
            System.out.println("received ok");
        }
    }

    @Test
    public void test_many_reads_one_message() throws Exception {
        var expectedMessage = "a".repeat(100).getBytes();
        ByteBuffer buf = ByteBuffer.wrap(expectedMessage);
        var channel = new AsynchronousSocketChannelMock(buf, List.of(20, 20, 20, 20, 20));
        var receiver = new MessageReceiver(channel, b -> b.limit() < 100 ? -1 : 100, 10);
        Assertions.assertArrayEquals(expectedMessage, receiver.receive().get().array());
    }
}
