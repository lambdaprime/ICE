package id.ICE;

import java.nio.ByteBuffer;

/**
 * Response message sent to the client
 */
public class MessageResponse {

    private ByteBuffer message;
    private boolean closeOnResponse;
    
    public MessageResponse(ByteBuffer message) {
        this(message, false);
    }
    
    /**
     * @param message message to be sent to the client
     * @param closeOnResponse close the connection after response is sent
     * or not.
     */
    public MessageResponse(ByteBuffer message,
            boolean closeOnResponse) {
        this.message = message;
        this.closeOnResponse = closeOnResponse;
    }
    
    public ByteBuffer getMessage() {
        return message;
    }

    /**
     * Returns whether the connection should be closed after response is sent
     * or not.
     * Intended for request/response protocols.
     */
    public boolean shouldCloseOnResponse() {
        return closeOnResponse;
    }
}
