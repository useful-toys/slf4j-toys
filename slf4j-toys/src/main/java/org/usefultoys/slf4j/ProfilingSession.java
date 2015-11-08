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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

import org.usefultoys.slf4j.watcher.Watcher;

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
    public static boolean useMemoryManagedBean = getProperty("profiler.useMemoryManagedBean", false);
    public static boolean useClassLoadingManagedBean = getProperty("profiler.useClassLoadingManagedBean", false);
    public static boolean useCompilationManagedBean = getProperty("profiler.useCompilationManagedBean", false);
    public static boolean useGarbageCollectionManagedBean = getProperty("profiler.useGarbageCollectionManagedBean", false);
    public static boolean usePlatformManagedBean = getProperty("profiler.usePlatformManagedBean", false);
    public static boolean reportVM = getProperty("report.VM", true);
    public static boolean reportFileSystem = getProperty("report.FileSystem", false);
    public static boolean reportMemory = getProperty("report.Memory", true);
    public static boolean reportUser = getProperty("report.User", true);
    public static boolean reportPhysicalSystem = getProperty("report.PhysicalSystem", true);
    public static boolean reportOperatingSystem = getProperty("report.OperatingSystem", true);
    public static boolean reportCalendar = getProperty("report.Calendar", true);
    public static boolean reportLocale = getProperty("report.Locale", true);
    public static boolean reportNetworkInterface = getProperty("report.NetworkInterface", false);

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

    public static void logReport() {
        final Logger logger = LoggerFactory.getLogger(getProperty("report.name", "report"));
        final Report report = new Report(logger);
        if (reportPhysicalSystem) {
            executor.execute(report.new ReportPhysicalSystem());
        }
        if (reportOperatingSystem) {
            executor.execute(report.new ReportOperatingSystem());
        }
        if (reportUser) {
            executor.execute(report.new ReportUser());
        }
        if (reportVM) {
            executor.execute(report.new ReportVM());
        }
        if (reportMemory) {
            executor.execute(report.new ReportMemory());
        }
        if (reportFileSystem) {
            executor.execute(report.new ReportFileSystem());
        }
        if (reportCalendar) {
            executor.execute(report.new ReportCalendar());
        }
        if (reportLocale) {
            executor.execute(report.new ReportLocale());
        }
        if (reportNetworkInterface) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface nif = interfaces.nextElement();
                    executor.execute(report.new ReportNetworkInterface(nif));
                }
            } catch (SocketException e) {
                logger.warn("Cannot report interfaces", e);
            }
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

    public static boolean readMeterPrintCategoryProperty() {
        return ProfilingSession.getProperty("meter.print.category", true);
    }

    public static long readMeterProgressPeriodProperty() {
        return ProfilingSession.getMillisecondsProperty("meter.progress.period", 2000L);
    }

    public static long readWatcherPeriodMillisecondsProperty() {
        return getMillisecondsProperty("watcher.period", 120000L);
    }

    public static long readWatcherDelayMillisecondsProperty() {
        return getMillisecondsProperty("watcher.delay", 60000L);
    }
}
