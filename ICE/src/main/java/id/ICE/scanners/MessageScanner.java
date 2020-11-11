package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * Message scanner allows message server to find where one message ends
 * and another begins.
 * 
 * It is used by ICE server to perform fragmentation of messages in the
 * sequence of bytes.
 * 
 * The implementation of this iface depends on the format of the messages
 * being used. Most common implementations which cover variety of
 * message formats already provided so try to check them before
 * implementing your own.
 */
@FunctionalInterface
public interface MessageScanner {

    /**
     * @return message end position or -1 if message is not complete or not found
     * in the buffer yet. If last byte of the message found on position n then the
     * message end position will be considered as n + 1.
     */
    int scan(ByteBuffer buf);
}
