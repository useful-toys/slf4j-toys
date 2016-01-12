/*
 * Copyright 2015 Daniel Felix Ferber.
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

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.watcher.Watcher;

/**
 * Profiling session for the current JVM.
 * <ul>
 * <li>Stores the UUID of the current SLF4J-Toys instance.
 * <li>Retrieves global configuration.
 * <li>Keeps the default watcher instance.
 * <li>Keeps the default executor that periodically invokes the default watcher.
 * </ul>
 *
 * @author Daniel Felix Ferber
 */
public final class Session {

    private Session() {
        // prevent instances
    }

    /**
     * UUID of the current SLF4J-Toys instance. This UUID is added to all trace messages.
     * It allows to distinguish messages from different JVM instances when logfiles are shared.
     * Value is assigned at application startup and cannot be changed at runtome.
     */
    public static final String uuid = UUID.randomUUID().toString().replace("-", "");
    /**
     * If memory usage status is retrieved from MemoryMXBean.
     * Not all JVM may support or allow MemoryMXBean usage.
     * Value is read from system property {@code slf4jtoys.useMemoryManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useMemoryManagedBean = getProperty("slf4jtoys.useMemoryManagedBean", false);
    /**
     * If class loading status is retrieved from ClassLoadingMXBean.
     * Not all JVM may support or allow ClassLoadingMXBean usage.
     * Value is read from system property {@code slf4jtoys.useClassLoadingManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useClassLoadingManagedBean = getProperty("slf4jtoys.useClassLoadingManagedBean", false);
    /**
     * If JIT compiler status is retrieved from CompilationMXBean.
     * Not all JVM may support or allow CompilationMXBean usage.
     * Value is read from system property {@code slf4jtoys.useCompilationManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useCompilationManagedBean = getProperty("slf4jtoys.useCompilationManagedBean", false);
    /**
     * If garbage collector status is retrieved from GarbageCollectorMXBean.
     * Not all JVM may support or allow GarbageCollectorMXBean usage.
     * Value is read from system property {@code slf4jtoys.useGarbageCollectionManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useGarbageCollectionManagedBean = getProperty("slf4jtoys.useGarbageCollectionManagedBean", false);
    /**
     * If operating system status is retrieved from OperatingSystemMXBean.
     * Not all JVM may support or allow OperatingSystemMXBean usage.
     * Value is read from system property {@code slf4jtoys.usePlatformManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean usePlatformManagedBean = getProperty("slf4jtoys.usePlatformManagedBean", false);

    /**
     * Watcher default instance.
     * This Watcher is created at application startup. Its name is read from system property {@code slf4jtoys.watcher.name}, defaults to
     * {@code watcher}.
     * You cannot assign a new default watcher at runtime.
     */
    public static final Watcher DEFAULT_WATCHER = new Watcher(LoggerFactory.getLogger(getProperty("slf4jtoys.watcher.name", "watcher")));

    private static ScheduledExecutorService defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledDefaultWatcher;

    /**
     * Starts the executor that periodically invokes the default watcher to report system status.
     * Intended for simple arquitectures. May not be suitable for JavaEE environments that manage threads by iteself.
     */
    public static synchronized void startDefaultWatcher() {
        if (defaultWatcherExecutor == null) {
            defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (scheduledDefaultWatcher == null) {
            scheduledDefaultWatcher = defaultWatcherExecutor.scheduleAtFixedRate(
                    DEFAULT_WATCHER,
                    getMillisecondsProperty("slf4jtoys.watcher.delay", 60000L),
                    getMillisecondsProperty("slf4jtoys.watcher.period", 600000L),
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the executor that periodically invokes the default watcher to report system status.
     */
    public static synchronized void stopDefaultWatcher() {
        if (scheduledDefaultWatcher != null) {
            scheduledDefaultWatcher.cancel(true);
        }
        if (defaultWatcherExecutor != null) {
            defaultWatcherExecutor.shutdownNow();
            defaultWatcherExecutor = null;
        }
    }

    /**
     * Runs the default report on the current thread.
     * Intended for simple arquitectures. May not be suitable for JavaEE environments that do not allow blocking threads for extended amount of time.
     */
    public static void runDefaultReport() {
        final Executor noThreadExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        new Reporter().logDefaultReports(noThreadExecutor);
    }

    /**
     * Retrieve the value of a system propert as a string value.
     * If the system property is not set, the default value is returned.

     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as string
     */
    public static String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return value == null ? defaultValue : value;
    }

    /**
     * Retrieve the value of a system propert as a boolean value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     * See {@link Boolean#parseBoolean(java.lang.String)}.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as boolean
     */
    public static boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Retrieve the value of a system propert as an integer value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as integer
     */
    public static int getProperty(final String name, final int defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * Retrieve the value of a system propert as a long integer value.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     *
     * @param name the system property name
     * @param defaultValue the default value, returned if system property is not set
     * @return the value as long integer
     */
    public static long getProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * Retrieve the value of a system property as an integer representing milliseconds.
     * If the system property is not set, or its value is a valid number, the default value is returned.
     * The value may be suffixed with 'ms', 's', 'm' or 'h', that will be intepreted as
     * a value in milliseconds, seconds, minutes or hours, respectively, and converted
     * to an integer in milliseconds.
     *
     * @param name the system property name
     * @param defaultValue the default value, in milliseconds, returned if system property is not set
     * @return the value in milliseconds
     */
    public static long getMillisecondsProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            int multiplicador = 1;
            int suffixLength = 1;
            if (value.endsWith("ms")) {
                suffixLength = 2;
            } else if (value.endsWith("s")) {
                multiplicador = 1000;
            } else if (value.endsWith("m")) {
                multiplicador = 60 * 1000;
            } else if (value.endsWith("h")) {
                multiplicador = 60 * 60 * 1000;
            } else {
                return defaultValue;
            }
            return Long.parseLong(value.substring(0, value.length() - suffixLength)) * multiplicador;

        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }

    public static boolean readMeterPrintCategoryProperty() {
        return Session.getProperty("slf4jtoys.meter.print.category", true);
    }

    public static long readMeterProgressPeriodProperty() {
        return Session.getMillisecondsProperty("slf4jtoys.meter.progress.period", 2000L);
    }

}