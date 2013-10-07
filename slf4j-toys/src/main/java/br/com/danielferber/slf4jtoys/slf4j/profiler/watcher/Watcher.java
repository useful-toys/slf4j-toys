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

import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import org.slf4j.Logger;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherEvent implements Runnable {

    private Logger logger;
    private final MessageWriter writer = new MessageWriter();
    private static final LoggerMessageCodec loggerMessageCodec = new LoggerMessageCodec();
    
    @Override
    public void run() {
        time = System.nanoTime();
        collectSystemStatus();
        {
            final StringBuilder buffer = new StringBuilder();
            this.readableString(buffer);
            logger.info(buffer.toString());
        }
        if (logger.isTraceEnabled()) {
            final StringBuilder buffer = new StringBuilder();
            loggerMessageCodec.writeLogMessage(buffer, writer, this);
            logger.trace(Slf4JMarkers.WATCHER, buffer.toString());
        }
    }

    public Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.uuid = ProfilingSession.uuid;
    }

    protected void setLogger(Logger logger) {
        this.logger = logger;
    }
}
