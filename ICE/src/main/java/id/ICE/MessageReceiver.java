package id.ICE;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import id.ICE.scanners.MessageScanner;

public class MessageReceiver implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private Utils utils = new Utils();
    private ByteBuffer buf;
    private AsynchronousSocketChannel channel;
    private MessageScanner scanner;
    private CompletableFuture<ByteBuffer> future;

    public MessageReceiver(AsynchronousSocketChannel channel, MessageScanner scanner) {
        this(channel, scanner, 256);
    }

    public MessageReceiver(AsynchronousSocketChannel channel, MessageScanner scanner, int bufSize) {
        this.channel = channel;
        this.scanner = scanner;
        this.buf = ByteBuffer.wrap(new byte[bufSize]);
    }

    /**
     * Called once read operation succeeds with some data read
     * or when read is failed. 
     */
    @Override
    public void completed(Integer result, AsynchronousSocketChannel channel) {
        if (processMessage()) return;
        if (!buf.hasRemaining()) {
            ByteBuffer newBuf = ByteBuffer.wrap(new byte[(int) Math.pow(buf.capacity(), 2)]);
            buf.rewind();
            newBuf.put(buf);
            buf = newBuf;
        }
        if (channel.isOpen())
            channel.read(buf, channel, this);
    }

    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel channel) {
        utils.handleException(exc);
    }

    public CompletableFuture<ByteBuffer> receive() {
        // let Java do async read of data and call us back once done
        future = new CompletableFuture<>();
        if (buf.position() == 0 || !processMessage())
            channel.read(buf, channel, this);
        return future;
    }

    private boolean processMessage() {
        var pos = scanner.scan(buf.duplicate().rewind().limit(buf.position()));
        if (pos == -1) {
            return false;
        }
        ByteBuffer message = ByteBuffer.wrap(Arrays.copyOf(buf.array(), pos));
        buf = utils.shiftToHead(buf, pos, buf.position());
        future.complete(message);
        return true;
    }

}