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
 * - lambdaprime <intid@protonmail.com>
 */
package id.ICE.scanners;

import java.nio.ByteBuffer;

/**
 * Scanner for messages where each message is represented
 * as a single line ending with some delimiter char (ex. ';').
 * 
 * The delimiter is not considered as part of the message.
 */
public class DelimiterMessageScanner implements MessageScanner {

    private byte delim;
    
    /**
     * @param delim delimiter character (for UTF-8 characters use conversion
     * for example for ';' use (byte)';')
     */
    public DelimiterMessageScanner(byte delim) {
        this.delim = delim;
    }

    @Override
    public int scan(ByteBuffer buf) {
        while (buf.hasRemaining()) {
            if (buf.get() == delim) return buf.position();
        }
        return -1;
    }

}
