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

import org.slf4j.Logger;
import org.usefultoys.slf4j.NullLogger;
import org.usefultoys.slf4j.Session;

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
 *
 * @author Daniel Felix Ferber
 * @see WatcherConfig
 * @see WatcherData
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    /** Logger for human-readable messages. */
    private final Logger messageLogger;
    /** Logger for machine-parsable data. */
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
        this.messageLogger = org.slf4j.LoggerFactory.getLogger(messagePrefix + name + messageSuffix);
        if (dataEnabled) {
            this.dataLogger = org.slf4j.LoggerFactory.getLogger(dataPrefix + name + dataSuffix);
        } else {
            this.dataLogger = NullLogger.INSTANCE; // Use NullLogger instead of null
        }
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
     * </ol>
     */
    @Override
    public void run() {
        collectCurrentTime();
        position++;
        // Removed dataLogger != null check
        if (messageLogger.isInfoEnabled() || dataLogger.isTraceEnabled()) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
        }
        if (messageLogger.isInfoEnabled()) {
            messageLogger.info(Markers.MSG_WATCHER, readableMessage());
        }
        // Removed dataLogger != null check
        if (dataLogger.isTraceEnabled()) {
            dataLogger.trace(Markers.DATA_WATCHER, json5Message());
        }
    }
}
