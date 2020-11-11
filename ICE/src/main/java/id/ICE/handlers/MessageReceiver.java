package id.ICE.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import id.ICE.impl.Utils;
import id.ICE.scanners.MessageScanner;
import id.xfunction.io.ByteBufferUtils;

/**
 * This handler acts as a message receiver and is notified each time server
 * reads some data from the client.
 * 
 * All read requests are done asynchronously. It means that this receiver may be
 * called with different amounts of data read each time.
 * 
 * Receiver relies on message scanner to extract a message from a stream of bytes and
 * then it passes them back to the looper.
 * 
 * Each time receiver reads new portion of data it appends it to internal buffer and
 * then it uses scanner to tell where message ends in this buffer and another begins.
 */
public class MessageReceiver implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private Utils utils = new Utils();
    private ByteBufferUtils bbUtils = new ByteBufferUtils();
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
        buf = bbUtils.shiftToHead(buf, pos, buf.position());
        future.complete(message);
        return true;
    }

}