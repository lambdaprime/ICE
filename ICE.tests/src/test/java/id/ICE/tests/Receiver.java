package id.ICE.tests;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Receiver {

    private SocketChannel channel;
    private ByteBuffer buf = ByteBuffer.wrap(new byte[256]);

    public Receiver(SocketChannel channel) {
        this.channel = channel;
    }
    
    public String nextLine(int len) {
        try {
            buf.rewind();
            buf.limit(len);
            channel.read(buf);
            String line = new String(buf.array(), 0, len);
            return line;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
