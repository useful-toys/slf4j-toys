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
package org.usefultoys.slf4j.watcher;

import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.usefultoys.slf4j.Session;

/**
 * Periodically collect system status and reports to logger. It conveniently
 * implements {@link Runnable} for compliance with
 * {@link ScheduledExecutorService}. Call {@link #logCurrentStatus()} to log a
 * single 1-line summary of the current system status. 
 * 
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    transient private final Logger logger;

    public Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.sessionUuid = Session.uuid;
        this.eventPosition = 0;
        this.eventCategory = logger.getName();
    }

    @Override
    public void run() {
        logCurrentStatus();
    }

    /**
     * A single 1-line summary of the current system status
     */
    public void logCurrentStatus() {
        time = System.nanoTime();
        eventPosition++;

        if (logger.isInfoEnabled()) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
            logger.info(readableString(new StringBuilder()).toString());
        }
        if (logger.isTraceEnabled()) {
            logger.trace(Slf4JMarkers.WATCHER, write(new StringBuilder(), 'W').toString());
        }
    }

   
}
