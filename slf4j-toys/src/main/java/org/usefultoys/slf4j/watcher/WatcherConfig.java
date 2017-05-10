/*
 * Copyright 2017 Daniel Felix Ferber
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
package org.usefultoys.slf4j.watcher;

import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of properties that drive {@link Watcher} and {@link WatcherData} behavior.
 * Initial values are read from system properties at application startup, if available.
 * They may be assigned at application startup, before calling any {@link Watcher} methods.
 * Some properties allow reassigning their values later at runtime.
 */
@SuppressWarnings("CanBeFinal")
public class WatcherConfig {

    /**
     * Logger name where watcher log messages are written to.
     */
    public static String name = Config.getProperty("slf4jtoys.watcher.name", "watcher");
    public static String dataPrefix = Config.getProperty("slf4jtoys.watcher.data.prefix", "");
    public static String dataSuffix = Config.getProperty("slf4jtoys.watcher.data.suffix", "");
    public static String messagePrefix = Config.getProperty("slf4jtoys.watcher.message.prefix", "");
    public static String messageSuffix = Config.getProperty("slf4jtoys.watcher.message.suffix", "");
    public static int dataUuidSize = Config.getProperty("slf4jtoys.watcher.data.uuid.size", 10);

    /**
     * Time to wait before reporting the first watcher status, in milliseconds. Value is read from system property {@code slf4jtoys.watcher.delay} at
     * application startup and defaults to {@code 1 minute}. The number represents a long integer that represents milliseconds. The system property
     * allows the number suffixed with 'ms', 's', 'm' and 'h' to represent milliseconds, seconds, minutes and hours.You may assign a new value at
     * runtime, but if the default watcher is already running, you need to restart it.
     */
    public static long delayMilliseconds = Config.getMillisecondsProperty("slf4jtoys.watcher.delay", 60000L);

    /**
     * Time period to wait before reporting further watcher status, in milliseconds. Time to wait before reporting the first watcher status, in
     * milliseconds. Value is read from system property {@code slf4jtoys.watcher.period} at application startup and defaults to {@code 10 minutes}.
     * The number represents a long integer that represents milliseconds. The system property allows the number suffixed with 'ms', 's', 'm' and 'h'
     * to represent milliseconds, seconds, minutes and hours. You may assign a new value at runtime, but if the default watcher is already running,
     * you need to restart it.
     */
    public static long periodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.watcher.period", 600000L);
}
