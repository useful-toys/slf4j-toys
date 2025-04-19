/*
 * Copyright 2024 Daniel Felix Ferber
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

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of configuration properties that control {@link Watcher} and {@link WatcherData} behavior.
 *
 * <p>
 * This class exposes configurable properties that influence how the session-related logging behaves at runtime. It
 * supports reading initial values from system properties during application startup, allowing applications to
 * externalize configuration.
 * <p>
 * These properties should ideally be defined <em>before</em> invoking any method from this library, to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken in
 * concurrent environments.
 * <p>
 * This class is intended as a utility container and is not meant to be instantiated.
 */
@UtilityClass
public class WatcherConfig {
    /**
     * Logger name used by {@link WatcherSingleton#DEFAULT_WATCHER} to write messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.name}, defaulting to {@code "watcher"}.
     */
    public String name = Config.getProperty("slf4jtoys.watcher.name", "watcher");

    public final String PROP_DELAY = "slf4jtoys.watcher.delay";
    public final String PROP_PERIOD = "slf4jtoys.watcher.period";
    public final String PROP_DATA_PREFIX = "slf4jtoys.watcher.data.prefix";
    public final String PROP_DATA_SUFFIX = "slf4jtoys.watcher.data.suffix";
    public final String PROP_MESSAGE_PREFIX = "slf4jtoys.watcher.message.prefix";
    public final String PROP_MESSAGE_SUFFIX = "slf4jtoys.watcher.message.suffix";

    /**
     * Initial delay before the first status report by {@link WatcherSingleton#DEFAULT_WATCHER}, in milliseconds.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.delay}, defaulting to {@code 60000} (1 minute). The
     * value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * <p>
     * You may assign a new value at runtime, but restarting the default watcher is required for the change to take
     * effect.
     */
    public long delayMilliseconds = Config.getMillisecondsProperty(PROP_DELAY, 60000L);

    /**
     * Interval between subsequent status reports by {@link WatcherSingleton#DEFAULT_WATCHER}, in milliseconds.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.period}, defaulting to {@code 600000} (10 minutes).
     * The value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * <p>
     * You may assign a new value at runtime, but restarting the default watcher is required for the change to take
     * effect.
     */
    public long periodMilliseconds = Config.getMillisecondsProperty(PROP_PERIOD, 600000L);

    /**
     * Prefix added to the logger name used for encoded data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows directing
     * encoded data to a different logger.
     * <p>
     * Example: with prefix {@code data.}, a logger {@code a.b.c.MyClass} becomes {@code data.a.b.c.MyClass} for encoded
     * data.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.data.prefix}, defaulting to an empty string.
     */
    public String dataPrefix = Config.getProperty(PROP_DATA_PREFIX, "");

    /**
     * Suffix added to the logger name used for encoded data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows directing
     * encoded data to a different logger.
     * <p>
     * Example: with suffix {@code .data}, a logger {@code a.b.c.MyClass} becomes {@code a.b.c.MyClass.data} for encoded
     * data.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.data.suffix}, defaulting to an empty string.
     */
    public String dataSuffix = Config.getProperty(PROP_DATA_SUFFIX, "");

    /**
     * Prefix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows directing
     * readable messages to a different logger.
     * <p>
     * Example: with prefix {@code message.}, a logger {@code a.b.c.MyClass} becomes {@code message.a.b.c.MyClass} for
     * readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.message.prefix}, defaulting to an empty string.
     */
    public String messagePrefix = Config.getProperty(PROP_MESSAGE_PREFIX, "");

    /**
     * Suffix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows directing
     * readable messages to a different logger.
     * <p>
     * Example: with suffix {@code .message}, a logger {@code a.b.c.MyClass} becomes {@code a.b.c.MyClass.message} for
     * readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.message.suffix}, defaulting to an empty string.
     */
    public String messageSuffix = Config.getProperty(PROP_MESSAGE_SUFFIX, "");
}
