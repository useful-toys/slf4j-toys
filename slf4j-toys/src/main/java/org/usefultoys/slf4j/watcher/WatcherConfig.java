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
package org.usefultoys.slf4j.watcher;

import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of properties that drive {@link Watcher} and {@link WatcherData} behavior. Initial values are read from system properties at application startup,
 * if available. They may be assigned at application startup, before calling any {@link Watcher} methods. Some properties allow reassigning their values later
 * at runtime.
 */
public final class WatcherConfig {
    private WatcherConfig() {
        // Utility class
    }

    /**
     * For {@link WatcherSingleton#DEFAULT_WATCHER}, name where the watcher writes messages to.
     */
    @org.jetbrains.annotations.NonNls
    public static String name = Config.getProperty("slf4jtoys.watcher.name", "watcher");
    /**
     * For {@link WatcherSingleton#DEFAULT_WATCHER}, time to wait before reporting the first watcher status, in milliseconds.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.delay} at application startup and defaults to {@code 1 minute}. The number represents a long
     * integer that represents milliseconds. The system property allows the number suffixed with 'ms', 's', 'm' and 'h' to represent milliseconds, seconds,
     * minutes and hours.You may assign a new value at runtime, but if the default watcher is already running, you need to restart it.
     */
    @org.jetbrains.annotations.NonNls
    public static long delayMilliseconds = Config.getMillisecondsProperty("slf4jtoys.watcher.delay", 60000L);
    /**
     * For {@link WatcherSingleton#DEFAULT_WATCHER}, time period to wait before reporting further watcher status, in milliseconds. Time to wait before reporting
     * the first watcher status, in milliseconds.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.period} at application startup and defaults to {@code 10 minutes}. The number represents a
     * long integer that represents milliseconds. The system property allows the number suffixed with 'ms', 's', 'm' and 'h' to represent milliseconds, seconds,
     * minutes and hours. You may assign a new value at runtime, but if the default watcher is already running, you need to restart it.
     */
    @org.jetbrains.annotations.NonNls
    public static long periodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.watcher.period", 600000L);
    /**
     * A prefix added to the logger that writes encoded data for {@link Watcher}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write encoded data to another logger which name has the given prefix or suffix, and configure these loggers separately.
     * <p>
     * For example, by setting the prefix to {@code 'data.'}, a {@link Watcher} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass} and encoded data to {@code data.a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String dataPrefix = Config.getProperty("slf4jtoys.watcher.data.prefix", "");
    /**
     * A suffix added to the logger that writes encoded data for {@link Watcher}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write encoded data to another logger which name has the given prefix or suffix, and configure these loggers separately.
     * <p>
     * For example, by setting the suffix to {@code '.data'}, a {@link Watcher} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass} and encoded event data to {@code data.a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String dataSuffix = Config.getProperty("slf4jtoys.watcher.data.suffix", "");
    /**
     * A prefix added to the logger that writes readable message for {@link Watcher}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write readable message to another logger which name has the given prefix or suffix, and configure these loggers
     * separately.
     * <p>
     * For example, by setting the prefix to {@code 'message.'}, a {@link Watcher} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * message.a.b.c.MyClass} and encoded data to {@code a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.message.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String messagePrefix = Config.getProperty("slf4jtoys.watcher.message.prefix", "");
    /**
     * A suffix added to the logger that writes readable message for {@link Watcher}.
     * <p>
     * By default, readable messages and encoded data are written to the same logger. In order to handle readable messages and encoded data separately without
     * creating filter rules, you may write readable message to another logger which name has the given prefix or suffix, and configure these loggers
     * separately.
     * <p>
     * For example, by setting the suffix to {@code '.message'}, a {@link Watcher} using logger {@code a.b.c.MyClass} will write readable messages to {@code
     * a.b.c.MyClass.message} and encoded event data to {@code a.b.c.MyClass}.
     * <p>
     * Value is read from system property {@code slf4jtoys.watcher.data.prefix} at application startup, defaults to empty. You may assign a new value at
     * runtime.
     */
    @org.jetbrains.annotations.NonNls
    public static String messageSuffix = Config.getProperty("slf4jtoys.watcher.message.suffix", "");
}
