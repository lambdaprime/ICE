package id.ICE.tests.services.echo;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import id.ICE.MessageResponse;
import id.ICE.MessageService;

/**
 * Echo service receives a string from the client and sends it back.
 */
public class EchoService implements MessageService {

    @Override
    public CompletableFuture<MessageResponse> process(ByteBuffer message) {
        System.out.println(new String(message.array()));
        byte[] b = new byte[message.capacity()];
        message.get(b, 0, message.capacity());
        return CompletableFuture.completedFuture(new MessageResponse(ByteBuffer.wrap(b)));
    }
}
