/*
 * Copyright 2025 Daniel Felix Ferber
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
package org.usefultoys.slf4j.watcher;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * Centralized configuration for the {@link Watcher} and {@link WatcherData}.
 * <p>
 * This class holds properties that control Watcher-related logging behavior.
 * It reads initial values from system properties at startup, allowing for externalized configuration.
 * <p>
 * For consistent behavior, these properties should be set before any methods from this library are called.
 * While some properties can be modified at runtime, caution is advised in concurrent environments.
 * <p>
 * This is a utility class and is not meant to be instantiated.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 */
@UtilityClass
public class WatcherConfig {
    static {
        init();
    }

    // System property keys
    public final String PROP_NAME = "slf4jtoys.watcher.name";
    public final String PROP_DELAY = "slf4jtoys.watcher.delay";
    public final String PROP_PERIOD = "slf4jtoys.watcher.period";
    public final String PROP_DATA_PREFIX = "slf4jtoys.watcher.data.prefix";
    public final String PROP_DATA_SUFFIX = "slf4jtoys.watcher.data.suffix";
    public final String PROP_DATA_ENABLED = "slf4jtoys.watcher.data.enabled";
    public final String PROP_MESSAGE_PREFIX = "slf4jtoys.watcher.message.prefix";
    public final String PROP_MESSAGE_SUFFIX = "slf4jtoys.watcher.message.suffix";

    /**
     * The logger name used by {@link WatcherSingleton#DEFAULT_WATCHER} to write messages.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.name}, defaulting to {@code "watcher"}.
     */
    public String name;

    /**
     * The initial delay before the first status report by {@link WatcherSingleton#DEFAULT_WATCHER}, in milliseconds.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.delay}, defaulting to {@code 60000} (1 minute).
     * The value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * <p>
     * A new value can be assigned at runtime, but restarting the default watcher is required for the change to take effect.
     */
    public long delayMilliseconds;

    /**
     * The interval between subsequent status reports by {@link WatcherSingleton#DEFAULT_WATCHER}, in milliseconds.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.period}, defaulting to {@code 600000} (10 minutes).
     * The value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * <p>
     * A new value can be assigned at runtime, but restarting the default watcher is required for the change to take effect.
     */
    public long periodMilliseconds;

    /**
     * A prefix added to the logger name for machine-parsable data messages.
     * <p>
     * This allows directing data messages to a different logger than human-readable ones.
     * <p>
     * Example: With prefix {@code "data."}, a logger named {@code "a.b.c.MyClass"} becomes {@code "data.a.b.c.MyClass"}.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.data.prefix}, defaulting to an empty string.
     */
    public String dataPrefix;

    /**
     * A suffix added to the logger name for machine-parsable data messages.
     * <p>
     * This allows directing data messages to a different logger than human-readable ones.
     * <p>
     * Example: With suffix {@code ".data"}, a logger named {@code "a.b.c.MyClass"} becomes {@code "a.b.c.MyClass.data"}.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.data.suffix}, defaulting to an empty string.
     */
    public String dataSuffix;

    /**
     * Determines if the watcher writes machine-parsable data messages to the log.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.data.enabled}, defaulting to {@code false}.
     * Can be changed at runtime.
     */
    public boolean dataEnabled;

    /**
     * A prefix added to the logger name for human-readable messages.
     * <p>
     * This allows directing readable messages to a different logger than machine-parsable ones.
     * <p>
     * Example: With prefix {@code "message."}, a logger named {@code "a.b.c.MyClass"} becomes {@code "message.a.b.c.MyClass"}.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.message.prefix}, defaulting to an empty string.
     */
    public String messagePrefix;

    /**
     * A suffix added to the logger name for human-readable messages.
     * <p>
     * This allows directing readable messages to a different logger than machine-parsable ones.
     * <p>
     * Example: With suffix {@code ".message"}, a logger named {@code "a.b.c.MyClass"} becomes {@code "a.b.c.MyClass.message"}.
     * <p>
     * Read from the system property {@code slf4jtoys.watcher.message.suffix}, defaulting to an empty string.
     */
    public String messageSuffix;

    /**
     * Initializes the configuration properties. This method should be called at application startup to ensure
     * consistent behavior.
     */
    public void init() {
        name = ConfigParser.getProperty(PROP_NAME, "watcher");
        delayMilliseconds = ConfigParser.getMillisecondsProperty(PROP_DELAY, 60000L);
        periodMilliseconds = ConfigParser.getMillisecondsProperty(PROP_PERIOD, 600000L);
        dataPrefix = ConfigParser.getProperty(PROP_DATA_PREFIX, "");
        dataSuffix = ConfigParser.getProperty(PROP_DATA_SUFFIX, "");
        dataEnabled = ConfigParser.getProperty(PROP_DATA_ENABLED, false);
        messagePrefix = ConfigParser.getProperty(PROP_MESSAGE_PREFIX, "");
        messageSuffix = ConfigParser.getProperty(PROP_MESSAGE_SUFFIX, "");
    }

    /**
     * Resets the configuration properties to their default values.
     * This method is useful for testing or re-initializing the configuration.
     */
    void reset() {
        System.clearProperty(WatcherConfig.PROP_NAME);
        System.clearProperty(WatcherConfig.PROP_DELAY);
        System.clearProperty(WatcherConfig.PROP_PERIOD);
        System.clearProperty(WatcherConfig.PROP_DATA_PREFIX);
        System.clearProperty(WatcherConfig.PROP_DATA_SUFFIX);
        System.clearProperty(WatcherConfig.PROP_DATA_ENABLED);
        System.clearProperty(WatcherConfig.PROP_MESSAGE_PREFIX);
        System.clearProperty(WatcherConfig.PROP_MESSAGE_SUFFIX);
        init();
    }
}
