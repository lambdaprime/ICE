package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * This scanner is useful when all incoming messages have
 * fixed length.
 */
public class FixedLengthMessageScanner implements MessageScanner {

    private int messageLength;
    
    public FixedLengthMessageScanner(int messageLength) {
        this.messageLength = messageLength;
    }

    @Override
    public int scan(ByteBuffer buf) {
        return buf.position() < messageLength? -1: messageLength + 1;
    }

}
