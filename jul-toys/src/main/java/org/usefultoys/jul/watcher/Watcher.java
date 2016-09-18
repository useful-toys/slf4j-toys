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
package org.usefultoys.jul.watcher;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.usefultoys.jul.Session;

/**
 * Periodically collects system status and reports it to logger. It conveniently
 * implements {@link Runnable} for compliance with {@link ScheduledExecutorService}.
 * Call {@link #logCurrentStatus()} to produce a 1-line summary of
 * the current system status as information message and an encoded event as trace message.
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger that reports messages.
     */
    transient private final Logger logger;

    /**
     * Constructor.
     * Events produced by this watcher will use the logger name as event category.
     *
     * @param logger Logger that reports messages.
     */
    public Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.sessionUuid = Session.uuid;
        this.eventPosition = 0;
        this.eventCategory = logger.getName();
    }

    /**
     * Constructor.
     *
     * @param logger logger that reports messages.
     * @param eventCategory event category.
     */
    public Watcher(final Logger logger, String eventCategory) {
        super();
        this.logger = logger;
        this.sessionUuid = Session.uuid;
        this.eventPosition = 0;
        this.eventCategory = eventCategory;
    }

    /**
     * @return Logger that reports current system status as information messages.
     */
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void run() {
        logCurrentStatus();
    }

    /**
     * Produces a 1-line summary of the current system status as information message
     * and an encoded event as trace message.
     */
    public void logCurrentStatus() {
        time = System.nanoTime();
        eventPosition++;

        if (logger.isLoggable(Level.INFO)) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
            logger.info(readableString(new StringBuilder()).toString());
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "[{0}] {1}", new Object[] { Markers.WATCHER, write(new StringBuilder(), 'W').toString()} );
        }
    }
}