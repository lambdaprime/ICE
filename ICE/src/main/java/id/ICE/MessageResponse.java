package id.ICE;

import java.nio.ByteBuffer;

/**
 * Response message sent to the client
 */
public class MessageResponse {

    private ByteBuffer message;
    private boolean closeOnResponse;
    private boolean ignoreNextRequest;
    
    /**
     * @param message message to be sent to the client
     */
    public MessageResponse(ByteBuffer message) {
        this.message = message;
    }

    /**
     * <p>Close the connection after response is sent.</p>
     */
    public MessageResponse withCloseOnResponse() {
        this.closeOnResponse = true;
        return this;
    }
    
    /**
     * <p>By default every time your service provides a *response* message, ICE
     * sends it back to the client and waits for the next *request* message from
     * the client (see ICE looper description).</p>
     * 
     * <p>With this flag set ICE will not wait for next request from the client
     * instead it will proceed to MessageService for another message which needs
     * to be send to the client.</p>
     * 
     * <p>You may want to use it when you need your service to send stream of messages.</p>
     */
    public MessageResponse withIgnoreNextRequest() {
        this.ignoreNextRequest = true;
        return this;
    }
    
    public ByteBuffer getMessage() {
        return message;
    }

    public boolean shouldCloseOnResponse() {
        return closeOnResponse;
    }
    
    public boolean shouldIgnoreNextRequest() {
        return ignoreNextRequest;
    }
}
