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
package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * Message scanner allows message server to find where one message ends
 * and another begins.
 * 
 * It is used by ICE server to perform fragmentation of messages in the
 * sequence of bytes.
 * 
 * The implementation of this iface depends on the format of the messages
 * being used. Most common implementations which cover variety of
 * message formats already provided so try to check them before
 * implementing your own.
 */
@FunctionalInterface
public interface MessageScanner {

    /**
     * @return message end position or -1 if message is not complete or not found
     * in the buffer yet. If last byte of the message found on position n then the
     * message end position will be considered as n + 1.
     */
    int scan(ByteBuffer buf);
}
