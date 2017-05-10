/*
 * Copyright 2017 Daniel Felix Ferber
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
import org.usefultoys.slf4j.LoggerConfig;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.meter.Meter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.usefultoys.slf4j.watcher.WatcherConfig.*;

/**
 * Collects system status and reports it to logger. It conveniently implements {@link Runnable} for compliance with {@link ScheduledExecutorService}.
 * Call {@link #logCurrentStatus()} to produce a 1-line summary of the current system status as information message and an encoded event as trace
 * message.
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    /**
     * Logger that reports messages.
     */
    private transient final Logger logger;
    private transient final Logger dataLogger;
    private transient final java.util.logging.Logger julLogger;
    private transient final java.util.logging.Logger julDataLogger;

    /**
     * Constructor. Events produced by this watcher will use the logger name as event category.
     *
     * @param logger Logger that reports messages.
     */
    public Watcher(final Logger logger) {
        this.logger = org.slf4j.LoggerFactory.getLogger(messagePrefix + logger.getName() + messageSuffix);
        this.dataLogger = org.slf4j.LoggerFactory.getLogger(dataPrefix + logger.getName() + dataSuffix);
        if (LoggerConfig.hackJulEnable) {
            this.julLogger = java.util.logging.Logger.getLogger(logger.getName());
            this.julDataLogger = java.util.logging.Logger.getLogger(dataLogger.getName());
        } else {
            this.julLogger = null;
            this.julDataLogger = null;
        }
        this.eventPosition = 0;
        if (dataUuidSize == 0) {
            /* Watcher está configurado para não informar o UUID. */
            this.sessionUuid = null;
        } else {
            this.sessionUuid = Session.uuid.substring(Session.uuid.length()- dataUuidSize, Session.uuid.length());
        }
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
     * Produces a 1-line summary of the current system status as information message and an encoded event as trace message.
     */
    public void logCurrentStatus() {
        time = System.nanoTime();
        eventPosition++;

        if (logger.isInfoEnabled()) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
            final String message = readableWrite();
            if (julLogger != null) {
                watcherMessageLogRecord(message);
                julLogger.info(message);
            } else {
                logger.info(Markers.MSG_WATCHER, message);
            }
        }
        if (logger.isTraceEnabled()) {
            final String message = write();
            if (julDataLogger != null) {
                julDataLogger.log(watcherDataLogRecord(message));
            } else {
                dataLogger.trace(Markers.DATA_WATCHER, message);
            }
        }
    }

    private void watcherMessageLogRecord(final String message) {
        final LogRecord logRecord = new LogRecord(Level.INFO, "[{0}] {1}");
        logRecord.setParameters(new Object[]{Markers.MSG_WATCHER.getName(), message});
        if (LoggerConfig.hackJulReplaceSource) {
            logRecord.setSourceClassName(Meter.class.getName());
            logRecord.setSourceMethodName("watcher");
        }
    }

    private LogRecord watcherDataLogRecord(final String message) {
        final LogRecord logRecord = new LogRecord(Level.FINEST, "[{0}] {1}");
        logRecord.setParameters(new Object[]{Markers.DATA_WATCHER.getName(), message});
        if (LoggerConfig.hackJulReplaceSource) {
            logRecord.setSourceClassName(Meter.class.getName());
            logRecord.setSourceMethodName("watcher");
        }
        return logRecord;
    }
}
