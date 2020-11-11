/**
 * ICE is a Java module which helps you to build non-blocking I/O message based servers.
 * 
 * With ICE you can implement servers for existing protocols (like HTTP) or your own.
 * 
 * Every message in ICE represented as a ByteBuffer object. That allows you to
 * get control of its actual format.
 * 
 * ICE supports following types of interaction:
 * - request/response -- this is when client sends a request message
 * and server process it and returns a message back with response
 * - request/multiple responses -- same as before except in this case
 * server may returns multiple messages back to the client with multiple responses
 * 
 * By default after ICE sends the message back to client it keeps the connection open
 * waiting for another request. But you can easily change this and ask ICE to close it
 * right after response is sent.
 * 
 * ICE non-blocking I/O is based on Java Async Channels rather than separate native
 * libraries. This makes ICE really crossplatform and small.
 * 
 * ICE has no dependencies on obsolete sun.misc.Unsafe and others so
 * it makes it easy to include into custom Java runtime images.
 * 
 */
module id.ICE {
    exports id.ICE;
    exports id.ICE.scanners;
    requires id.xfunction;
    requires java.logging;

    exports id.ICE.impl to id.ICE.tests;
    exports id.ICE.handlers to id.ICE.tests;
}