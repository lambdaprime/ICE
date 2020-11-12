/*
 * Copyright 2020 ICE project
 * 
 * Website: https://github.com/lambdaprime/ICE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Authors:
 * - lambdaprime <id.blackmesa@gmail.com>
 */
/**
 * <p>ICE is a Java module which helps you to build non-blocking I/O message based servers.</p>
 * 
 * <p>With ICE you can implement servers for existing protocols (like HTTP) or your own.</p>
 * 
 * <p>ICE supports following types of interaction:</p>
 * <ul>
 * <li>request/response -- this is when client sends a request message
 * and server process it and returns a message back with response</li>
 * <li>request/multiple responses -- same as before except in this case
 * server may returns multiple messages back to the client with multiple responses</li>
 * </ul>
 * 
 * <p>By default after ICE sends the message back to client it keeps the connection open
 * waiting for another request. But you can easily change this and ask ICE to close it
 * right after response is sent.<p>
 * 
 * <p>ICE non-blocking I/O is based on Java Async Channels rather than separate native
 * libraries. This makes ICE really crossplatform and small.</p>
 * 
 * <p>ICE has no dependencies on obsolete sun.misc.Unsafe and others so
 * it makes it easy to include into custom Java runtime images.</p>
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