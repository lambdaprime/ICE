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
package id.ICE.impl;

import id.xfunction.logging.XLogger;
import java.nio.channels.AsynchronousCloseException;
import java.util.logging.Logger;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class Utils {

    private static final Logger LOGGER = XLogger.getLogger(Utils.class);

    public void handleException(Throwable exc) {
        // main thread forcefully closed all channels
        if (exc instanceof AsynchronousCloseException) return;
        LOGGER.severe(exc.getMessage());
        exc.printStackTrace();
    }
}
