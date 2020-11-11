package id.ICE;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Processing of client messages are done through this interface.
 * 
 * It needs to be thread safe because it can be called by multiple threads.
 */
@FunctionalInterface
public interface MessageService {

    /**
     * This method is being called by the server to process received
     * messages.
     * 
     * @param message received client message. If service requested to ignore
     * next client request (@see withIgnoreNextRequest) then the duplicate
     * of original previous ByteBuffer is passed instead. Any changes to internal
     * array() of such ByteBuffer will be reflected in the original ByteBuffer.
     * 
     * @return once the returned future is complete the response will
     * be processed and sent back to the client. Server decides what to do
     * with the connection based on returned result:
     * - if response is null the connection is closed
     * - otherwise we will wait for next message from the client
     */
    CompletableFuture<MessageResponse> process(ByteBuffer message);
}
