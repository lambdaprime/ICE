package id.ICE;

import java.nio.ByteBuffer;

/**
 * Message scanner allows message server to find where one message ends and another begins.
 * 
 * Receiver relies on message scanner to extract a message from a stream of bytes and
 * then it pass it to the user service.
 * 
 * Each time receiver reads new portion of data it appends it to internal buffer and
 * then it uses scanner to tell where message ends in this buffer and another begins.
 */
@FunctionalInterface
public interface MessageScanner {

    /**
     * @return message end position or -1 if message is not complete
     */
    int scan(ByteBuffer buf);
}
