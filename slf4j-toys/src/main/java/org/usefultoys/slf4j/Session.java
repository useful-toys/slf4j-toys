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
import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.watcher.Watcher;
import org.usefultoys.slf4j.watcher.WatcherConfig;

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
     * It allows to distinguish messages from different JVM instances when log files are shared.
     * Value is assigned at application startup and cannot be changed at runtime.
     */
    public static final String uuid = UUID.randomUUID().toString().replace("-", "");

    /**
     * Watcher default instance.
     * This Watcher is created at application startup. Its name is read from system property {@code slf4jtoys.watcher.name}, defaults to
     * {@code watcher}.
     * You cannot assign a new default watcher at runtime.
     */
    public static final Watcher DEFAULT_WATCHER = new Watcher(LoggerFactory.getLogger(Config.getProperty("slf4jtoys.watcher.name", "watcher")));

    private static ScheduledExecutorService defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledDefaultWatcher;

    /**
     * Starts the executor that periodically invokes the default watcher to report system status.
     * Intended for simple architectures. May not be suitable for JavaEE environments that manage threads by itself.
     */
    public static synchronized void startDefaultWatcher() {
        if (defaultWatcherExecutor == null) {
            defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (scheduledDefaultWatcher == null) {
            scheduledDefaultWatcher = defaultWatcherExecutor.scheduleAtFixedRate(
                    DEFAULT_WATCHER,
                    WatcherConfig.delay,
                    WatcherConfig.period,
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
     * Intended for simple architectures. May not be suitable for JavaEE environments that do not allow blocking threads for extended amount of time.
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
}
