package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * This scanner treats all data which was written into the buffer
 * so far as one single message. As the result such messages may
 * have variable length.
 * 
 * Effectively message will consist from whatever ICE managed to
 * receive from the client in one non blocking read operation.
 */
public class VarLengthMessageScanner implements MessageScanner {

    @Override
    public int scan(ByteBuffer buf) {
        return buf.position() + 1;
    }

}
