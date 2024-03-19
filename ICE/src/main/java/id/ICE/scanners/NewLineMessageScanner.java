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
package id.ICE.scanners;

/**
 * Scanner for messages where each message is represented as a single line ending with new line (ex.
 * '\n').
 *
 * <p>The new line is not considered as part of the message.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class NewLineMessageScanner extends DelimiterMessageScanner {

    public NewLineMessageScanner() {
        super((byte) '\n');
    }
}
