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

import lombok.NonNull;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Periodically executes a {@link Watcher} using a {@link ScheduledExecutorService}.
 * <p>
 * Each controller owns its own {@link Watcher} instance, created from the configured {@code name}.
 * By default, the factory methods read {@code name}, {@code delayMilliseconds} and {@code periodMilliseconds}
 * from {@link WatcherConfig} at the moment they are called. This allows the application to set
 * configuration before materializing the controller.
 * <p>
 * The controller schedules the watcher on a single daemon thread, so the owned {@link Watcher#run()} is never
 * invoked concurrently by this controller. Executions are additionally serialized by a private lock, so that even
 * across executor generations — for example a {@link #stop()} followed by {@link #start()} while a collection is still
 * in flight — {@link Watcher#run()} is never entered by two threads at once. Do not share the controller's watcher with
 * another scheduler or thread pool; {@link Watcher} instances are not thread-safe and must be executed by at most one
 * thread at a time.
 * <p>
 * Because each controller owns its own watcher, it also maintains its own internal event
 * {@code position} sequence. If another
 * {@code Watcher} instance (for example, another controller or a {@code WatcherServlet}) uses the
 * same name, the same logger will receive interleaved position sequences. Use a unique name for
 * each watcher instance when consumers depend on a single ordered sequence per name.
 * <p>
 * The controller exposes {@link #create()} static factory methods for common use cases. The underlying
 * executor uses a daemon thread named after the watcher, so it will not prevent the JVM from shutting down.
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

    /**
     * Serializes {@link Watcher#run()} so it is never entered concurrently, including across executor generations
     * when {@link #stop()} does not wait for an in-flight execution and {@link #start()} schedules a new one.
     */
    private final ReentrantLock runLock = new ReentrantLock();

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> task;

    /**
     * Creates a new controller with the supplied configuration and builds the owned {@link Watcher}.
     *
     * @param name                logical watcher name
     * @param delayMilliseconds   initial delay before the first execution
     * @param periodMilliseconds  interval between executions
     */
    private WatcherExecutorController(@NonNull final String name, final long delayMilliseconds, final long periodMilliseconds) {
        this(name, delayMilliseconds, periodMilliseconds, new Watcher(name));
    }

    /**
     * Creates a new controller with the supplied configuration and a pre-built {@link Watcher}.
     * <p>
     * Package-private: exists so tests in this package can inject a {@link Watcher} stub without
     * resorting to reflection.
     *
     * @param name                logical watcher name
     * @param delayMilliseconds   initial delay before the first execution
     * @param periodMilliseconds  interval between executions
     * @param watcher             the watcher instance to run on schedule
     */
    WatcherExecutorController(@NonNull final String name, final long delayMilliseconds, final long periodMilliseconds,
                               final Watcher watcher) {
        if (delayMilliseconds < 0) {
            throw new IllegalArgumentException("delayMilliseconds must be >= 0");
        }
        if (periodMilliseconds <= 0) {
            throw new IllegalArgumentException("periodMilliseconds must be > 0");
        }
        this.name = name;
        this.delayMilliseconds = delayMilliseconds;
        this.periodMilliseconds = periodMilliseconds;
        this.watcher = watcher;
    }

    /**
     * Creates a controller using all defaults from {@link WatcherConfig}.
     *
     * @return a new, not-yet-started controller
     */
    public static WatcherExecutorController create() {
        return new WatcherExecutorController(WatcherConfig.name, WatcherConfig.delayMilliseconds, WatcherConfig.periodMilliseconds);
    }

    /**
     * Creates a controller with a custom watcher name and default schedule from {@link WatcherConfig}.
     *
     * @param name logical watcher name
     * @return a new, not-yet-started controller
     */
    public static WatcherExecutorController create(final String name) {
        return new WatcherExecutorController(name, WatcherConfig.delayMilliseconds, WatcherConfig.periodMilliseconds);
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
        return new WatcherExecutorController(name, delayMilliseconds, periodMilliseconds);
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
                    new Runnable() {
                        @Override
                        public void run() {
                            runSafely();
                        }
                    }, delayMilliseconds, periodMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Executes the watcher, logging and swallowing any runtime exception so the
     * scheduled execution stays alive.
     * <p>
     * Acquires {@link #runLock} for the duration of {@link Watcher#run()} so that an execution scheduled by a new
     * executor generation (after a {@link #stop()} that did not wait for the previous one to finish) blocks until the
     * in-flight execution completes, instead of running concurrently on the shared {@link Watcher} instance.
     */
    private void runSafely() {
        runLock.lock();
        try {
            watcher.run();
        } catch (final RuntimeException e) {
            LoggerFactory.getLogger(WatcherExecutorController.class)
                    .error("Watcher execution failed; next executions remain scheduled.", e);
        } finally {
            runLock.unlock();
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
}
