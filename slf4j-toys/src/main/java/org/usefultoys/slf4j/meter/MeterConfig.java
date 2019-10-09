/*
 * Copyright 2019 Daniel Felix Ferber
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
package org.usefultoys.slf4j.meter;

import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of properties that drive {@link Meter} and {@link MeterData} behavior. Initial values are read from system properties at application startup, if
 * available. They may be assigned at application startup, before calling any {@link Meter} methods. Some properties allow reassigning their values at runtime.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("CanBeFinal")
public final class MeterConfig {

    /**
     * Time to wait before reporting next progress status, in milliseconds.
     * <p>
     * Meter allows reporting progress status of incremental operations by calling the {@link Meter#inc()}, {@link Meter#incBy(long)} and {@link
     * Meter#incTo(long)} method on each step. To prevent crowding the log file and to prevent performance degradation, Meter waits a minimal amount of time
     * before printing the next status message. Value is read from system property {@code slf4jtoys.meter.progress.period} at application startup and defaults
     * to {@code 2 seconds}. The number represents a long integer that represents milliseconds.
     * <p>
     * The system property allows the number suffixed with 'ms', 's', 'm' and 'h' to represent milliseconds, seconds, minutes and hours. You may assign a new
     * value at runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static long progressPeriodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.meter.progress.period", 2000L);
    /**
     * If {@link Meter} and {@link MeterData} print the category on the readable message.
     * <p>
     * The Meter status is closely related to the logger name. The category is the logger's last name. The usual logger configuration already includes the
     * logger name on each message. If using a logging frameworks that does not support logger name, one may set this property to true to include the category.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.category} at application startup, defaults to {@code false}. You may assign a new value
     * at runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static boolean printCategory = Config.getProperty("slf4jtoys.meter.print.category", false);
    /**
     * If {@link Meter} and {@link MeterData} print the status on the readable message.
     * <p>
     * The Meter status is closely related to the logger level. If using Logback as logging framework, it is possible to display the Meter status instead of the
     * logger level and one may set this property to false to omit the status from the message.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.print.status} at application startup, defaults to {@code true}. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static boolean printStatus = Config.getProperty("slf4jtoys.meter.print.status", true);
    /**
     * If {@link Meter} and {@link MeterData} print the position (event counter) load on the readable message.
     *
     * <p>Value is read from system property {@code slf4jtoys.meter.print.position} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static boolean printPosition = Config.getProperty("slf4jtoys.meter.print.position", false);
    /**
     * If {@link Meter} and {@link MeterData} print the cpu load on the readable message.
     *
     * <p>Value is read from system property {@code slf4jtoys.meter.print.load} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static boolean printLoad = Config.getProperty("slf4jtoys.meter.print.load", false);
    /**
     * If {@link Meter} and {@link MeterData} print the memory load on the readable message.
     * <p>Value is read from system property {@code slf4jtoys.meter.print.memory} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static boolean printMemory = Config.getProperty("slf4jtoys.meter.print.memory", false);
    /**
     * A prefix added to the logger that writes encoded data for {@link Meter}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write encoded data to another logger which name has the given prefix or suffix, and configure these loggers separately.
     * <p>
     * For example, by setting the prefix to {@code 'data.'}, a {@link Meter} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass} and encoded data to {@code data.a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String dataPrefix = Config.getProperty("slf4jtoys.meter.data.prefix", "");
    /**
     * A suffix added to the logger that writes encoded data for {@link Meter}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write encoded data to another logger which name has the given prefix or suffix, and configure these loggers separately.
     * <p>
     * For example, by setting the suffix to {@code '.data'}, a {@link Meter} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass} and encoded event data to {@code data.a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String dataSuffix = Config.getProperty("slf4jtoys.meter.data.suffix", "");
    /**
     * A prefix added to the logger that writes readable message for {@link Meter}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write readable message to another logger which name has the given prefix or suffix, and configure these loggers
     * separately.
     * <p>
     * For example, by setting the prefix to {@code 'message.'}, a {@link Meter} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * message.a.b.c.MyClass} and encoded data to {@code a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.message.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String messagePrefix = Config.getProperty("slf4jtoys.meter.message.prefix", "");
    /**
     * A suffix added to the logger that writes readable message for {@link Meter}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write readable message to another logger which name has the given prefix or suffix, and configure these loggers
     * separately.
     * <p>
     * For example, by setting the suffix to {@code '.message'}, a {@link Meter} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass.message} and encoded event data to {@code a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.meter.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String messageSuffix = Config.getProperty("slf4jtoys.meter.message.suffix", "");

    private MeterConfig() {
    }
}
