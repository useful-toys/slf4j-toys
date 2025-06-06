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
 * Centralized configuration holder for controlling the behavior of the {@link Meter} and {@link MeterData}.
 * <p>
 * This class exposes a set of flags and properties that determine the behavior of meters, such as progress reporting,
 * logging categories, and encoded data handling. These properties can be configured at application startup or modified
 * dynamically at runtime.
 * <p>
 * These properties should ideally be defined <em>before</em> invoking any method from this library, to ensure
 * consistent behavior. Some properties can be modified dynamically at runtime, although care should be taken in
 * concurrent environments.
 * <p>
* <strong>Performance note:</strong> Some properties, such as progress reporting intervals, may impact performance
 * if configured with very low values.
 * <p>
 * This class is a utility holder and should not be instantiated.
* <p>
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class MeterConfig {
    
    public final String PROP_MESSAGE_SUFFIX = "slf4jtoys.meter.message.suffix";
    public final String PROP_MESSAGE_PREFIX = "slf4jtoys.meter.message.prefix";
    public final String PROP_DATA_SUFFIX = "slf4jtoys.meter.data.suffix";
    public final String PROP_DATA_PREFIX = "slf4jtoys.meter.data.prefix";
    public final String PROP_PRINT_MEMORY = "slf4jtoys.meter.print.memory";
    public final String PROP_PRINT_CATEGORY = "slf4jtoys.meter.print.category";
    public final String PROP_PRINT_LOAD = "slf4jtoys.meter.print.load";
    public final String PROP_PROGRESS_PERIOD = "slf4jtoys.meter.progress.period";
    public final String PROP_PRINT_POSITION = "slf4jtoys.meter.print.position";
    public final String PROP_PRINT_STATUS = "slf4jtoys.meter.print.status";

    static {
        init();
    }

    /**
     * Time to wait before reporting the next progress status, in milliseconds.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.progress.period}, defaulting to {@code 2000} (2 seconds).
     * The value can be suffixed with {@code ms}, {@code s}, {@code m}, or {@code h}.
     * <p>
     * You may assign a new value at runtime.
     */
    public long progressPeriodMilliseconds;

    /**
     * Whether the meter includes the category in readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.category}, defaulting to {@code false}.
     * <p>
     * You may assign a new value at runtime.
     */
    public boolean printCategory;

    /**
     * Whether the meter includes the status in readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.status}, defaulting to {@code true}.
     * <p>
     * You may assign a new value at runtime.
     */
    public boolean printStatus;

    /**
     * Whether the meter includes the position (event counter) in readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.position}, defaulting to {@code false}.
     * <p>
     * You may assign a new value at runtime.
     */
    public boolean printPosition;

    /**
     * Whether the meter includes the CPU load in readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.load}, defaulting to {@code false}.
     * <p>
     * You may assign a new value at runtime.
     */
    public boolean printLoad;

    /**
     * Whether the meter includes the memory load in readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.memory}, defaulting to {@code false}.
     * <p>
     * You may assign a new value at runtime.
     */
    public boolean printMemory;

    /**
     * Prefix added to the logger name used for encoded data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows directing
     * encoded data to a different logger.
     * <p>
     * Example: with prefix {@code data.}, a logger {@code a.b.c.MyClass} becomes {@code data.a.b.c.MyClass} for encoded
     * data.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.prefix}, defaulting to an empty string.
     */
    public String dataPrefix;

    /**
     * Suffix added to the logger name used for encoded data messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows directing
     * encoded data to a different logger.
     * <p>
     * Example: with suffix {@code .data}, a logger {@code a.b.c.MyClass} becomes {@code a.b.c.MyClass.data} for encoded
     * data.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.suffix}, defaulting to an empty string.
     */
    public String dataSuffix;

    /**
     * Prefix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a prefix allows directing
     * readable messages to a different logger.
     * <p>
     * Example: with prefix {@code message.}, a logger {@code a.b.c.MyClass} becomes {@code message.a.b.c.MyClass} for
     * readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.message.prefix}, defaulting to an empty string.
     */
    public String messagePrefix;

    /**
     * Suffix added to the logger name used for human-readable messages.
     * <p>
     * By default, encoded and human-readable messages are written to the same logger. Setting a suffix allows directing
     * readable messages to a different logger.
     * <p>
     * Example: with suffix {@code .message}, a logger {@code a.b.c.MyClass} becomes {@code a.b.c.MyClass.message} for
     * readable messages.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.message.suffix}, defaulting to an empty string.
     */
    public String messageSuffix;

    /**
     * Initializes the configuration attributes by reading the corresponding system properties.
     * This method should be called before accessing any configuration attributes to ensure
     * they are properly initialized.
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
     * Resets the configuration properties to their default values.
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
