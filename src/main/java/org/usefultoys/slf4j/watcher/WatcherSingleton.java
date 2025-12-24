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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages the default {@link Watcher} singleton and provides methods to execute it periodically.
 * This class is suitable for simple architectures but may not be appropriate for JavaEE environments
 * that manage their own threads.
 * <p>
 * The default watcher instance is created at application startup and is named according to the system property
 * {@code slf4jtoys.watcher.name} (defaulting to "watcher"). It cannot be reassigned at runtime.
 * <p>
 * This utility class offers two mechanisms for periodic execution:
 * <ul>
 *   <li>A {@link ScheduledExecutorService}-based executor.</li>
 *   <li>A {@link Timer}-based timer.</li>
 * </ul>
 * <p>
 * Note: Ensure proper lifecycle management when using this class to avoid resource leaks.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 * @see WatcherConfig
 */
@UtilityClass
public final class WatcherSingleton {

    /**
     * The default watcher instance. It is created lazily upon first access and named using the system property
     * {@code slf4jtoys.watcher.name}, which defaults to "watcher".
     */
    private static Watcher DEFAULT_WATCHER_INSTANCE;

    /**
     * Returns the default {@link Watcher} instance, creating it if it hasn't been initialized yet.
     * This method ensures that {@link WatcherConfig} is initialized before the Watcher instance is created.
     *
     * @return The default Watcher instance.
     */
    public static synchronized Watcher getDefaultWatcher() {
        if (DEFAULT_WATCHER_INSTANCE == null) {
            DEFAULT_WATCHER_INSTANCE = new Watcher(WatcherConfig.name);
        }
        return DEFAULT_WATCHER_INSTANCE;
    }

    /** Executor service for running the default watcher periodically. */
    ScheduledExecutorService defaultWatcherExecutor = null;
    ScheduledFuture<?> scheduledDefaultWatcher = null;

    /** Timer for running the default watcher periodically. */
    Timer defaultWatcherTimer = null;
    TimerTask defaultWatcherTask = null;

    /**
     * Starts the executor that periodically invokes the default watcher to report the runtime state.
     * This is intended for simple architectures and may not be suitable for JavaEE environments
     * that manage their own threads.
     */
    public synchronized void startDefaultWatcherExecutor() {
        if (defaultWatcherExecutor == null) {
            defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (scheduledDefaultWatcher == null) {
            scheduledDefaultWatcher = defaultWatcherExecutor.scheduleAtFixedRate(
                    getDefaultWatcher(), // Use the getter
                    WatcherConfig.delayMilliseconds,
                    WatcherConfig.periodMilliseconds,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Stops the executor that periodically invokes the default watcher.
     */
    public synchronized void stopDefaultWatcherExecutor() {
        if (scheduledDefaultWatcher != null) {
            scheduledDefaultWatcher.cancel(true);
            scheduledDefaultWatcher = null;
        }
        if (defaultWatcherExecutor != null) {
            defaultWatcherExecutor.shutdownNow();
            defaultWatcherExecutor = null;
        }
    }

    /**
     * Starts the timer that periodically invokes the default watcher to report the runtime state.
     * This is intended for simple architectures and may not be suitable for JavaEE environments
     * that manage their own threads.
     */
    public synchronized void startDefaultWatcherTimer() {
        if (defaultWatcherTimer == null) {
            defaultWatcherTimer = new Timer("Watcher");
        }
        if (defaultWatcherTask == null) {
            defaultWatcherTask = new TimerTask() {
                @Override
                public void run() {
                    getDefaultWatcher().run(); // Use the getter
                }
            };
            defaultWatcherTimer.schedule(
                    defaultWatcherTask,
                    WatcherConfig.delayMilliseconds,
                    WatcherConfig.periodMilliseconds
            );
        }
    }

    /**
     * Stops the timer that periodically invokes the default watcher.
     */
    public synchronized void stopDefaultWatcherTimer() {
        if (defaultWatcherTimer != null) {
            defaultWatcherTimer.cancel();
            defaultWatcherTimer = null;
        }
        if (defaultWatcherTask != null) {
            defaultWatcherTask = null;
        }
    }
}
