/*
 * Copyright 2016 Daniel Felix Ferber.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j;

import org.usefultoys.slf4j.meter.*;
import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.watcher.Watcher;

/**
 * Collection of properties that drive {@link Meter} and {@link Watcher} common logging behavior. Initial values are
 * read from system properties, if available. Some properties allow reassigning their values at runtime.
 *
 * @author Daniel Felix Ferber
 */
public class LoggerConfig {

    /**
     * Use JUL instead of SLF4J. Uses JUL instead of SLF4J for start, progress, ok, reject and fail messages. This hack might be
     * reasonable when the SLF4J integration does not work well over the underlying logger framework.
     */
    public static boolean hackJulEnable = Config.getProperty("slf4jtoys.hack.jul.enable", false);
    /**
     * Replace class and method name with logger name. This hack might be reasonable does not
     * support logger name (eg. Google App Engine).
     */
    public static boolean hackJulReplaceSource = Config.getProperty("slf4jtoys.hack.jul.replaceSource", false);
}
