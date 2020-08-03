package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * Scanner for messages where each message are represented
 * as a single line ending with some delimiter char (ex. '\n').
 * 
 * The delimiter is not considered as part of the message.
 */
public class DelimiterMessageScanner implements MessageScanner {

    private byte delim;
    
    public DelimiterMessageScanner(byte delim) {
        this.delim = delim;
    }

    @Override
    public int scan(ByteBuffer buf) {
        while (buf.hasRemaining()) {
            if (buf.get() == delim) return buf.position();
        }
        return -1;
    }

}
