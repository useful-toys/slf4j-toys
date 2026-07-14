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

import org.slf4j.Logger;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.internal.SystemMetrics;
import org.usefultoys.slf4j.meter.Meter;

import java.util.concurrent.ScheduledExecutorService;

import static org.usefultoys.slf4j.watcher.WatcherConfig.*;

/**
 * Collects and reports information about the state of the Java runtime.
 * <p>
 * On each execution, this class gathers metrics about the JVM's runtime, the
 * underlying platform, and registered JMX MBeans.
 * <p>
 * A call to {@link #run()} generates two log messages:
 * <ul>
 * <li>A **human-readable summary** at the {@code INFO} level.</li>
 * <li>A **machine-parsable data message** at the {@code TRACE} level for automated analysis.</li>
 * </ul>
 * As a {@link Runnable}, this class can be easily integrated with scheduling
 * services like {@link ScheduledExecutorService}.
 * <p>
 * <b>Thread safety:</b> {@code Watcher} instances are <em>not</em> thread-safe. A single instance must be executed by
 * at most one thread at a time; {@link #run()} mutates the internal state inherited from {@code EventData} and
 * {@code SystemData}. When integrating with an external scheduler, either dedicate the instance to a single-threaded
 * schedule or serialize calls to {@link #run()} externally (as {@link WatcherServlet} does with a private lock). Do not
 * register the same instance with multiple schedulers or submit it to a thread pool that allows concurrent execution.
 * <p>
 * A {@link Watcher} instance maintains its own internal event {@link org.usefultoys.slf4j.internal.EventData#position position}
 * sequence. Two distinct {@code Watcher} instances that use the same {@code name} will write to the same logger but will
 * produce interleaved position sequences, because each instance counts independently. If a single ordered sequence per
 * name is required, the application must ensure that only one {@code Watcher} instance uses that name.
 *
 * @author Daniel Felix Ferber
 * @see WatcherConfig
 * @see WatcherData
 */

public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    /** Logger for human-readable messages. */
    @SuppressWarnings("NonConstantLogger")
    private final Logger messageLogger;
    /** Logger for machine-parsable data. */
    @SuppressWarnings("NonConstantLogger")
    private final Logger dataLogger;

    /**
     * Creates a new Watcher.
     * <p>
     * The loggers for reporting the runtime state are derived from the {@code name}
     * parameter, using prefixes and suffixes defined in {@link WatcherConfig}.
     *
     * @param name A logical identifier for this Watcher, used to create the logger names.
     */
    public Watcher(final String name) {
        super(Session.shortSessionUuid());
        final Logger base = org.slf4j.LoggerFactory.getLogger(name);
        messageLogger = resolveDecoratedLogger(base, messagePrefix, messageSuffix);
        dataLogger = resolveDecoratedLogger(base, dataPrefix, dataSuffix);
    }

    /**
     * Resolves the logger to use for decorated (prefixed/suffixed) output. Reuses {@code base} directly when both
     * {@code prefix} and {@code suffix} are empty (the default), avoiding a redundant name concatenation and
     * backend logger-registry lookup for the common case.
     *
     * @param base   the undecorated logger, as looked up by name.
     * @param prefix the prefix to prepend to {@code base}'s name, or empty for none.
     * @param suffix the suffix to append to {@code base}'s name, or empty for none.
     * @return {@code base} unchanged if both {@code prefix} and {@code suffix} are empty; otherwise a logger
     *         looked up under the decorated name.
     */
    private static Logger resolveDecoratedLogger(final Logger base, final String prefix, final String suffix) {
        return (prefix.isEmpty() && suffix.isEmpty())
                ? base
                : org.slf4j.LoggerFactory.getLogger(prefix + base.getName() + suffix);
    }

    /**
     * Collects the current runtime state and reports it to the configured loggers.
     * This method serves as the entry point for execution, typically called by a
     * {@link ScheduledExecutorService}.
     * <p>
     * The process is as follows:
     * <ol>
     * <li>Collects runtime, platform, and MBean metrics.</li>
     * <li>Logs a human-readable summary at the {@code INFO} level.</li>
     * <li>Logs a machine-parsable data message at the {@code TRACE} level.</li>
     * <li>Flushes any pending forgotten-meter leaks via {@link Meter#drainLeaks()}.</li>
     * </ol>
     */
    @Override
    public void run() {
        collectCurrentTime();
        nextPosition();
        // Removed dataLogger != null check
        if (messageLogger.isInfoEnabled() || dataLogger.isTraceEnabled()) {
            SystemMetrics.getInstance().collectRuntimeStatus(this);
            SystemMetrics.getInstance().collectPlatformStatus(this);
            SystemMetrics.getInstance().collectManagedBeanStatus(this);
        }
        if (messageLogger.isInfoEnabled()) {
            messageLogger.info(Markers.MSG_WATCHER, readableMessage());
        }
        // Removed dataLogger != null check
        if (dataLogger.isTraceEnabled()) {
            dataLogger.trace(Markers.DATA_WATCHER, json5Message());
        }
        /* Drive the leak detector's opportunistic drain from the watcher's periodic tick, so forgotten-meter
           leaks are still reported when the application has otherwise gone quiet on meter activity. Cheap
           when no leak is pending (a single volatile poll). Note that disabling MeterConfig.detectLeaks at
           runtime only stops new registrations -- it does not suppress reports for meters registered while
           it was still enabled, so this call can still emit reports even right after the flag is toggled off. */
        Meter.drainLeaks();
    }
}
