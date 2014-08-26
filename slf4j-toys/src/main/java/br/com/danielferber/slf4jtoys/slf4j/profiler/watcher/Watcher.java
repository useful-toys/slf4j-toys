/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import org.slf4j.Logger;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {
	private static final long serialVersionUID = 1L;

	transient private final Logger logger;

    public Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.sessionUuid = ProfilingSession.uuid;
        this.eventPosition = 0;
        this.eventCategory = logger.getName();
    }

    @Override
    public void run() {
        time = System.nanoTime();
        eventPosition++;

        if (logger.isInfoEnabled()) {
            collectSystemStatus();
            logger.info(readableString(new StringBuilder()).toString());
        }
        if (logger.isTraceEnabled()) {
            logger.trace(Slf4JMarkers.WATCHER, write(new StringBuilder(), 'W').toString());
        }
    }
}
