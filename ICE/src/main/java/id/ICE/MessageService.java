package id.ICE;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MessageService {

    /**
     * This method is being called by the server each time new
     * message is received.
     */
    CompletableFuture<ByteBuffer> process(ByteBuffer message);
}
