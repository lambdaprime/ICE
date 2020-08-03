package id.ICE.tests;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import id.ICE.MessageReceiver;

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
        var receiver = new MessageReceiver(channel, b -> b.limit() < 100? -1: 100, 10);
        Assertions.assertArrayEquals(expectedMessage, receiver.receive().get().array());
    }
}

