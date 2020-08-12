package id.ICE.impl;

import java.nio.channels.AsynchronousCloseException;
import java.util.logging.Logger;

import id.xfunction.logging.XLogger;

public class Utils {

    private static final Logger LOGGER = XLogger.getLogger(Utils.class);

    public void handleException(Throwable exc) {
        // main thread forcefully closed all channels
        if (exc instanceof AsynchronousCloseException)
            return;
        LOGGER.severe(exc.getMessage());
        exc.printStackTrace();
    }

}
