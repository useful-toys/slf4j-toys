/*
 * Copyright 2024 Daniel Felix Ferber
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

import org.slf4j.Logger;
import org.usefultoys.slf4j.Session;

import java.util.concurrent.ScheduledExecutorService;

import static org.usefultoys.slf4j.watcher.WatcherConfig.*;

/**
 * Collects system status and reports it to logger. Call {@link #logCurrentStatus()} to produce a readable message as INFO and an encoded data as TRACE. It
 * conveniently implements {@link Runnable} for compliance with {@link ScheduledExecutorService}.
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    /** Logger that prints readable messages. */
    private final Logger messageLogger;
    /** Logger that prints enconded data. */
    private final Logger dataLogger;

    /**
     * Constructor. Events produced by this watcher will use the given logger.
     *
     * @param name Logger that reports messages.
     */
    public Watcher(final String name) {
        super(Session.shortSessionUudi());
        this.messageLogger = org.slf4j.LoggerFactory.getLogger(messagePrefix + name + messageSuffix);
        if (dataEnabled) {
            this.dataLogger = org.slf4j.LoggerFactory.getLogger(dataPrefix + name + dataSuffix);
        } else {
            this.dataLogger = null;
        }
    }

    @Override
    public void run() {
        logCurrentStatus();
    }

    /**
     * Logs about the current system status.
     */
    public void logCurrentStatus() {
        time = System.nanoTime();
        position++;
        if (messageLogger.isInfoEnabled() || (dataLogger != null && dataLogger.isTraceEnabled())) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
        }
        if (messageLogger.isInfoEnabled()) {
            messageLogger.info(Markers.MSG_WATCHER, readableMessage());
        }
        if (dataLogger != null && dataLogger.isTraceEnabled()) {
            dataLogger.trace(Markers.DATA_WATCHER, encodedMessage());
        }
    }
}
