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
package br.com.danielferber.slf4jtoys.slf4j.profiler;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import br.com.danielferber.slf4jtoys.slf4j.profiler.watcher.Watcher;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Profiling session for the current JVM. Stores the UUID logged on each message
 * on the current JVM. Keeps the timer calls the watcher periodically.
 *
 * @author Daniel Felix Ferber
 */
public final class ProfilingSession {

    private ProfilingSession() {
        // prevent instances
    }

    public static final String uuid = UUID.randomUUID().toString().replace("-", "");
    public static boolean useManagementFactory = getProperty("profiler.useManagementFactory", false);
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledWatcher;

    public static synchronized void startWatcher() {
        if (scheduledWatcher == null) {
            final Watcher watcher = new Watcher(LoggerFactory.getLogger(getProperty("watcher.name", "watcher")));
            scheduledWatcher = executor.scheduleAtFixedRate(watcher, readWatcherDelayMillisecondsProperty(), readWatcherPeriodMillisecondsProperty(), TimeUnit.MILLISECONDS);
        }
    }

    public static synchronized void stopWatcher() {
        if (scheduledWatcher != null) {
            scheduledWatcher.cancel(true);
        }
    }

    public static synchronized void startExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public static synchronized void stopExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public static String getProperty(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return value == null ? defaultValue : value;
    }

    public static boolean getProperty(final String name, final boolean defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static int getProperty(final String name, final int defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long getProperty(final String name, final long defaultValue) {
        final String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

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

        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long readMeterProgressPeriodProperty() {
        return ProfilingSession.getMillisecondsProperty("meter.progress.period", 2000L);
    }

    public static long readWatcherPeriodMillisecondsProperty() {
        return getMillisecondsProperty("watcher.period", 15000L);
    }

    public static long readWatcherDelayMillisecondsProperty() {
        return getMillisecondsProperty("watcher.delay", 15000L);
    }
}
