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
 * This scanner treats all data which was written into the buffer
 * so far as one single message. As the result such messages may
 * have variable length.
 * 
 * Effectively message will consist from whatever ICE managed to
 * receive from the client in one non blocking read operation.
 */
public class VarLengthMessageScanner implements MessageScanner {

    @Override
    public int scan(ByteBuffer buf) {
        return buf.position() + 1;
    }

}
