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
package org.usefultoys.slf4j.meter;

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * Centralized configuration for controlling the behavior of the {@link Meter} and {@link MeterData}.
 * <p>
 * This class exposes a set of flags and properties that determine the behavior of meters, such as
 * progress reporting, logging categories, and encoded data handling. These properties can be
 * configured at application startup or modified dynamically at runtime.
 * <p>
 * These properties should ideally be defined *before* invoking any method from this library to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken
 * in concurrent environments.
 * <p>
 * **Performance Note:** Some properties, such as progress reporting intervals, may impact performance
 * if configured with very low values.
 * <p>
 * This is a utility class and should not be instantiated.
 *
 * @author Daniel Felix Ferber
 * @see Meter
 * @see MeterData
 */
@UtilityClass
public class MeterConfig {

    /** System property key for the message logger name suffix. */
    public final String PROP_MESSAGE_SUFFIX = "slf4jtoys.meter.message.suffix";
    /** System property key for the message logger name prefix. */
    public final String PROP_MESSAGE_PREFIX = "slf4jtoys.meter.message.prefix";
    /** System property key for the data logger name suffix. */
    public final String PROP_DATA_SUFFIX = "slf4jtoys.meter.data.suffix";
    /** System property key for the data logger name prefix. */
    public final String PROP_DATA_PREFIX = "slf4jtoys.meter.data.prefix";
    /** System property key for enabling/disabling memory printing in readable messages. */
    public final String PROP_PRINT_MEMORY = "slf4jtoys.meter.print.memory";
    /** System property key for enabling/disabling category printing in readable messages. */
    public final String PROP_PRINT_CATEGORY = "slf4jtoys.meter.print.category";
    /** System property key for enabling/disabling CPU load printing in readable messages. */
    public final String PROP_PRINT_LOAD = "slf4jtoys.meter.print.load";
    /** System property key for the progress reporting period. */
    public final String PROP_PROGRESS_PERIOD = "slf4jtoys.meter.progress.period";
    /** System property key for enabling/disabling position printing in readable messages. */
    public final String PROP_PRINT_POSITION = "slf4jtoys.meter.print.position";
    /** System property key for enabling/disabling status printing in readable messages. */
    public final String PROP_PRINT_STATUS = "slf4jtoys.meter.print.status";

    static {
        init();
    }

    /**
     * The minimum time interval (in milliseconds) between consecutive progress status reports.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.progress.period}, defaulting to {@code 2000} (2 seconds).
     * The value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * Can be assigned a new value at runtime.
     */
    public long progressPeriodMilliseconds;

    /**
     * Whether the {@link Meter} includes the category in its human-readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.category}, defaulting to {@code false}.
     * Can be assigned a new value at runtime.
     */
    public boolean printCategory;

    /**
     * Whether the {@link Meter} includes the operation's status (e.g., OK, FAIL) in its human-readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.status}, defaulting to {@code true}.
     * Can be assigned a new value at runtime.
     */
    public boolean printStatus;

    /**
     * Whether the {@link Meter} includes the operation's position (event counter) in its human-readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.position}, defaulting to {@code false}.
     * Can be assigned a new value at runtime.
     */
    public boolean printPosition;

    /**
     * Whether the {@link Meter} includes the system CPU load in its human-readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.load}, defaulting to {@code false}.
     * Can be assigned a new value at runtime.
     */
    public boolean printLoad;

    /**
     * Whether the {@link Meter} includes memory usage information in its human-readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.memory}, defaulting to {@code false}.
     * Can be assigned a new value at runtime.
     */
    public boolean printMemory;

    /**
     * A prefix added to the logger name used for machine-parsable data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows
     * directing encoded data to a different logger.
     * <p>
     * Example: With prefix {@code "data."}, a logger {@code "a.b.c.MyClass"} becomes {@code "data.a.b.c.MyClass"}
     * for encoded data.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.prefix}, defaulting to an empty string.
     */
    public String dataPrefix;

    /**
     * A suffix added to the logger name used for machine-parsable data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows
     * directing encoded data to a different logger.
     * <p>
     * Example: With suffix {@code ".data"}, a logger {@code "a.b.c.MyClass"} becomes {@code "a.b.c.MyClass.data"}
     * for encoded data.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.suffix}, defaulting to an empty string.
     */
    public String dataSuffix;

    /**
     * A prefix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows
     * directing readable messages to a different logger.
     * <p>
     * Example: With prefix {@code "message."}, a logger {@code "a.b.c.MyClass"} becomes {@code "message.a.b.c.MyClass"}
     * for readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.message.prefix}, defaulting to an empty string.
     */
    public String messagePrefix;

    /**
     * A suffix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows
     * directing readable messages to a different logger.
     * <p>
     * Example: With suffix {@code ".message"}, a logger {@code "a.b.c.MyClass"} becomes {@code "a.b.c.MyClass.message"}
     * for readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.message.suffix}, defaulting to an empty string.
     */
    public String messageSuffix;

    /**
     * Initializes the configuration attributes by reading the corresponding system properties.
     * This method should be called at application startup to ensure they are properly initialized.
     */
    public void init() {
        progressPeriodMilliseconds = ConfigParser.getMillisecondsProperty(PROP_PROGRESS_PERIOD, 2000L);
        printCategory = ConfigParser.getProperty(PROP_PRINT_CATEGORY, false);
        printStatus = ConfigParser.getProperty(PROP_PRINT_STATUS, true);
        printPosition = ConfigParser.getProperty(PROP_PRINT_POSITION, false);
        printLoad = ConfigParser.getProperty(PROP_PRINT_LOAD, false);
        printMemory = ConfigParser.getProperty(PROP_PRINT_MEMORY, false);
        dataPrefix = ConfigParser.getProperty(PROP_DATA_PREFIX, "");
        dataSuffix = ConfigParser.getProperty(PROP_DATA_SUFFIX, "");
        messagePrefix = ConfigParser.getProperty(PROP_MESSAGE_PREFIX, "");
        messageSuffix = ConfigParser.getProperty(PROP_MESSAGE_SUFFIX, "");
    }

    /**
     * Resets all configuration properties to their default values.
     * This method is useful for testing purposes or when reinitializing the configuration.
     */
    void reset() {
        System.clearProperty(PROP_PROGRESS_PERIOD);
        System.clearProperty(PROP_PRINT_CATEGORY);
        System.clearProperty(PROP_PRINT_STATUS);
        System.clearProperty(PROP_PRINT_POSITION);
        System.clearProperty(PROP_PRINT_LOAD);
        System.clearProperty(PROP_PRINT_MEMORY);
        System.clearProperty(PROP_DATA_PREFIX);
        System.clearProperty(PROP_DATA_SUFFIX);
        System.clearProperty(PROP_MESSAGE_PREFIX);
        System.clearProperty(PROP_MESSAGE_SUFFIX);
        init();
    }
}
