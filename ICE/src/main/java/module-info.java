/**
 * This module provides server implementation based on Java Async Channels.
 * Processing of client connections is done through handlers.
 * 
 * Handler is notified each time server reads some data from the client.
 * All read requests are done asynchronously. It means that handlers may be called
 * with different amounts of data read each time.
 * 
 * Handler needs to be thread safe because it can be called by multiple threads.
 * 
 * Server decides what to do with the connection based on handler result:
 * - if response is null the connection is closed
 * - otherwise we will read new data again
 * When response payload is non null we send it back to the client.
 * 
 */
module id.ICE {
    exports id.ICE;
    exports id.ICE.scanners;
}