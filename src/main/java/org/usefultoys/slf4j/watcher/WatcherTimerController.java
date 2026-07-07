/*
 * Copyright 2026 Daniel Felix Ferber
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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Periodically executes a {@link Watcher} using a {@link Timer}.
 * <p>
 * Each controller owns its own {@link Watcher} instance, created from the configured {@code name}.
 * By default, the controller reads {@code name}, {@code delayMilliseconds} and {@code periodMilliseconds}
 * from {@link WatcherConfig} at the moment the builder or {@code create()} method is invoked. This allows
 * the application to set configuration before materializing the watcher.
 * <p>
 * The controller exposes {@link #create()} shortcuts for the most common use cases, and a fluent
 * {@link Builder} for full flexibility. The underlying {@link Timer} is created as a daemon and named
 * after the watcher, so it will not prevent the JVM from shutting down.
 * <p>
 * {@code start()} and {@code stop()} are idempotent and may be called multiple times.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 * @see WatcherConfig
 * @see WatcherExecutorController
 */
public final class WatcherTimerController implements AutoCloseable {

    private final String name;
    private final long delayMilliseconds;
    private final long periodMilliseconds;
    private final Watcher watcher;

    private Timer timer;
    private TimerTask timerTask;

    private WatcherTimerController(final Builder builder) {
        this.name = builder.name;
        this.delayMilliseconds = builder.delayMilliseconds;
        this.periodMilliseconds = builder.periodMilliseconds;
        this.watcher = new Watcher(name);
    }

    /**
     * Returns a builder pre-populated with defaults from {@link WatcherConfig}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a controller using all defaults from {@link WatcherConfig}.
     *
     * @return a new, not-yet-started controller
     */
    public static WatcherTimerController create() {
        return builder().build();
    }

    /**
     * Creates a controller with a custom watcher name and default schedule from {@link WatcherConfig}.
     *
     * @param name logical watcher name
     * @return a new, not-yet-started controller
     */
    public static WatcherTimerController create(final String name) {
        return builder().name(name).build();
    }

    /**
     * Creates a controller with a custom watcher name and schedule.
     *
     * @param name                logical watcher name
     * @param delayMilliseconds   initial delay before the first execution
     * @param periodMilliseconds  interval between executions
     * @return a new, not-yet-started controller
     */
    public static WatcherTimerController create(final String name, final long delayMilliseconds, final long periodMilliseconds) {
        return builder().name(name).delayMilliseconds(delayMilliseconds).periodMilliseconds(periodMilliseconds).build();
    }

    /**
     * Starts periodic execution using a daemon {@link Timer} named after the watcher.
     * <p>
     * Calling this method when the controller is already running has no effect.
     */
    public synchronized void start() {
        if (timer == null) {
            timer = new Timer(name, true);
        }
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    watcher.run();
                }
            };
            timer.schedule(timerTask, delayMilliseconds, periodMilliseconds);
        }
    }

    /**
     * Stops periodic execution and cancels the timer.
     * <p>
     * Calling this method when the controller is already stopped has no effect.
     */
    public synchronized void stop() {
        if (timerTask != null) {
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Returns whether the controller has an active timer task.
     *
     * @return {@code true} if the watcher is scheduled, {@code false} otherwise
     */
    public synchronized boolean isRunning() {
        return timerTask != null;
    }

    /**
     * Stops the controller. Equivalent to {@link #stop()}.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Fluent builder for {@link WatcherTimerController}. Defaults are read from {@link WatcherConfig}
     * when the builder is created, allowing the application to configure values before building.
     */
    public static final class Builder {
        private String name = WatcherConfig.name;
        private long delayMilliseconds = WatcherConfig.delayMilliseconds;
        private long periodMilliseconds = WatcherConfig.periodMilliseconds;

        private Builder() {
            // Defaults are set above from WatcherConfig.
        }

        /**
         * Sets the logical watcher name (and timer thread name).
         *
         * @param name logical watcher name
         * @return this builder
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the initial delay before the first execution.
         *
         * @param delayMilliseconds delay in milliseconds
         * @return this builder
         */
        public Builder delayMilliseconds(final long delayMilliseconds) {
            this.delayMilliseconds = delayMilliseconds;
            return this;
        }

        /**
         * Sets the interval between executions.
         *
         * @param periodMilliseconds period in milliseconds
         * @return this builder
         */
        public Builder periodMilliseconds(final long periodMilliseconds) {
            this.periodMilliseconds = periodMilliseconds;
            return this;
        }

        /**
         * Builds a new controller. The owned {@link Watcher} is created from the configured name.
         *
         * @return a new controller
         */
        public WatcherTimerController build() {
            return new WatcherTimerController(this);
        }
    }
}
