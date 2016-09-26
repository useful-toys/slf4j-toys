/*
 * Copyright 2016 Daniel Felix Ferber.
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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.internal.Config;

/**
 * Keeps the default watcher singleton. Offers some methods to execute this watcher periodically on simple architectures.
 *
 * @author Daniel Felix Ferber
 */
public class WatcherSingleton {

    /**
     * Watcher default instance. It is created at application startup and named as system property {@code slf4jtoys.watcher.name}, which defaults to
     * {@code watcher}. You cannot assign a new default watcher at runtime.
     */
    public static final Watcher DEFAULT_WATCHER = new Watcher(LoggerFactory.getLogger(Config.getProperty("slf4jtoys.watcher.name", "watcher")));

    private static ScheduledExecutorService defaultWatcherExecutor;
    private static ScheduledFuture<?> scheduledDefaultWatcher;

    private static Timer defaultWatcherTimer;
    private static TimerTask defaultWatcherTask;

    /**
     * Starts the executor that periodically invokes the default watcher to report system status. Intended for simple architectures. May not be
     * suitable for JavaEE environments that manage threads by itself.
     */
    public static synchronized void startDefaultWatcherExecutor() {
        if (defaultWatcherExecutor == null) {
            defaultWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (scheduledDefaultWatcher == null) {
            scheduledDefaultWatcher = defaultWatcherExecutor.scheduleAtFixedRate(DEFAULT_WATCHER,
                    WatcherConfig.delayMilliseconds,
                    WatcherConfig.periodMilliseconds,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the executor that periodically invokes the default watcher periodically.
     */
    public static synchronized void stopDefaultWatcherExecutor() {
        if (scheduledDefaultWatcher != null) {
            scheduledDefaultWatcher.cancel(true);
        }
        if (defaultWatcherExecutor != null) {
            defaultWatcherExecutor.shutdownNow();
            defaultWatcherExecutor = null;
        }
    }

    /**
     * Starts the timer that periodically invokes the default watcher to report system status. Intended for simple architectures. May not be suitable
     * for JavaEE environments that manage threads by itself.
     */
    public static synchronized void startDefaultWatcherTimer() {
        if (defaultWatcherTimer == null) {
            defaultWatcherTimer = new Timer("Watcher");
        }
        if (defaultWatcherTask == null) {
            defaultWatcherTask = new TimerTask() {
                @Override
                public void run() {
                    WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
                }
            };
            defaultWatcherTimer.schedule(defaultWatcherTask,
                    WatcherConfig.delayMilliseconds,
                    WatcherConfig.periodMilliseconds);
        }
    }

    /**
     * Stops the timer that periodically invokes the default watcher periodically.
     */
    public static synchronized void stopDefaultWatcherTimer() {
        if (defaultWatcherTimer != null) {
            defaultWatcherTimer.cancel();
        }
        if (defaultWatcherTask != null) {
            defaultWatcherTask = null;
        }
    }

}
