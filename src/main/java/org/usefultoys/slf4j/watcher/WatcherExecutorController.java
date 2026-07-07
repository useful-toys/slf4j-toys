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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Periodically executes a {@link Watcher} using a {@link ScheduledExecutorService}.
 * <p>
 * Each controller owns its own {@link Watcher} instance, created from the configured {@code name}.
 * By default, the controller reads {@code name}, {@code delayMilliseconds} and {@code periodMilliseconds}
 * from {@link WatcherConfig} at the moment the builder or {@code create()} method is invoked. This allows
 * the application to set configuration before materializing the watcher.
 * <p>
 * The controller exposes {@link #create()} shortcuts for the most common use cases, and a fluent
 * {@link Builder} for full flexibility. The underlying executor uses a daemon thread named after the
 * watcher, so it will not prevent the JVM from shutting down.
 * <p>
 * {@code start()} and {@code stop()} are idempotent and may be called multiple times.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 * @see WatcherConfig
 * @see WatcherTimerController
 */
public final class WatcherExecutorController implements AutoCloseable {

    private final String name;
    private final long delayMilliseconds;
    private final long periodMilliseconds;
    private final Watcher watcher;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> task;

    private WatcherExecutorController(final Builder builder) {
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
    public static WatcherExecutorController create() {
        return builder().build();
    }

    /**
     * Creates a controller with a custom watcher name and default schedule from {@link WatcherConfig}.
     *
     * @param name logical watcher name
     * @return a new, not-yet-started controller
     */
    public static WatcherExecutorController create(final String name) {
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
    public static WatcherExecutorController create(final String name, final long delayMilliseconds, final long periodMilliseconds) {
        return builder().name(name).delayMilliseconds(delayMilliseconds).periodMilliseconds(periodMilliseconds).build();
    }

    /**
     * Starts periodic execution using a daemon single-thread executor.
     * <p>
     * Calling this method when the controller is already running has no effect.
     */
    public synchronized void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    final Thread t = new Thread(r, name);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        if (task == null) {
            task = executor.scheduleAtFixedRate(
                    watcher, delayMilliseconds, periodMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops periodic execution and shuts down the executor.
     * <p>
     * Calling this method when the controller is already stopped has no effect.
     */
    public synchronized void stop() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Returns whether the controller has an active scheduled task.
     *
     * @return {@code true} if the watcher is scheduled, {@code false} otherwise
     */
    public synchronized boolean isRunning() {
        return task != null;
    }

    /**
     * Stops the controller. Equivalent to {@link #stop()}.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Fluent builder for {@link WatcherExecutorController}. Defaults are read from {@link WatcherConfig}
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
         * Sets the logical watcher name (and thread name).
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
        public WatcherExecutorController build() {
            return new WatcherExecutorController(this);
        }
    }
}
