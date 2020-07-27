package id.ICE;

import java.nio.channels.AsynchronousCloseException;

public class Utils {

    public void handleException(Throwable exc) {
        System.out.println("got exception");
        // main thread forcefully closed all channels
        if (exc instanceof AsynchronousCloseException)
            return;
        System.err.println(exc);

    }
}
