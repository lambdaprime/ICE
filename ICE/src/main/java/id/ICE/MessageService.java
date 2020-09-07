package id.ICE;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MessageService {

    /**
     * This method is being called by the server to process received
     * messages.
     * 
     * @param message received client message. If service requested to ignore
     * next client request (@see withIgnoreNextRequest) then the reference
     * to original previous message is used instead.
     * 
     * @return once the returned future is complete the response will
     * be processed and sent back to the client.
     */
    CompletableFuture<MessageResponse> process(ByteBuffer message);
}
