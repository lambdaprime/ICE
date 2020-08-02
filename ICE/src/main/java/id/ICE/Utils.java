package id.ICE;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;

public class Utils {

    public void handleException(Throwable exc) {
        System.out.println("got exception");
        // main thread forcefully closed all channels
        if (exc instanceof AsynchronousCloseException)
            return;
        System.err.println(exc);
    }
    
    /**
     * Shifts portion of the buffer to the head of the ByteBuffer.
     * The existing elements will be overwritten.
     * @return same buf with position set to end of the portion shifted
     */
    public ByteBuffer shiftToHead(ByteBuffer buf, int start, int end) {
        ByteBuffer newBuf = buf.duplicate();
        newBuf.rewind();
        buf.position(start);
        for (int i = start; i < end; i++) {
            newBuf.put(buf.get());
        }
        buf.position(end - start);
        return buf;
    }
}
