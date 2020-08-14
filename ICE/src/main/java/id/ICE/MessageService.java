package id.ICE;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface MessageService {

    /**
     * This method is being called by the server each time new
     * message is received.
     * 
     * @return once the returned future is complete its value will
     * be sent back to the client. If the value of future is null the connection
     * is closed 
     */
    CompletableFuture<MessageResponse> process(ByteBuffer message);
}
