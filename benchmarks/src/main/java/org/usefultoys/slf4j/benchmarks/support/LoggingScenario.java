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
package org.usefultoys.slf4j.benchmarks.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.MeterConfig;

/**
 * A logging regime for the benchmarks, expressed as an independent level for the
 * human-readable <em>message</em> logger and for the JSON5 <em>data</em> logger.
 * <p>
 * To make the two loggers independently addressable, the benchmarks give them
 * distinct names via {@link MeterConfig#messageSuffix} and {@link MeterConfig#dataSuffix}
 * (see {@link #configureSuffixes()}). A Meter/Watcher created on base category
 * {@code "c"} then writes human-readable messages to {@code "c" + messageSuffix} and
 * JSON5 data to {@code "c" + dataSuffix}, so each can be switched on or off on its own.
 * <p>
 * Note on reachability: in the Meter/Watcher source the data statement is nested inside
 * the message-level guard ({@code if (messageLogger.isXxxEnabled()) { ...
 * if (dataLogger.isTraceEnabled()) ... }}). Hence a "data on / message off" combination
 * emits nothing and costs the same as {@link #OFF}; {@link #DATA_ONLY} is provided so that
 * fact can be verified empirically, but the three primary regimes are
 * {@link #OFF}, {@link #MESSAGE} and {@link #MESSAGE_DATA}.
 * <p>
 * Enabled loggers are routed to a {@link DiscardAppender} (additivity off) so the measured
 * cost is the string construction inside slf4j-toys, not logback's encoder or I/O.
 */
public enum LoggingScenario {
    /** Both loggers OFF: pure instrumentation floor. */
    OFF(Level.OFF, Level.OFF),
    /** Human-readable messages built and emitted; JSON5 data off. */
    MESSAGE(Level.INFO, Level.OFF),
    /** Both human-readable messages and JSON5 data built and emitted. */
    MESSAGE_DATA(Level.INFO, Level.TRACE),
    /** Message off, data TRACE: unreachable data path; should match {@link #OFF}. */
    DATA_ONLY(Level.OFF, Level.TRACE);

    public static final String MESSAGE_SUFFIX = ".msg";
    public static final String DATA_SUFFIX = ".data";

    private final Level messageLevel;
    private final Level dataLevel;

    LoggingScenario(final Level messageLevel, final Level dataLevel) {
        this.messageLevel = messageLevel;
        this.dataLevel = dataLevel;
    }

    /**
     * Splits the message and data loggers onto distinct names. Call once per trial,
     * before any Meter/Watcher is constructed, since the suffixes are read at
     * construction time ({@code Meter} resolves both loggers in its constructor).
     */
    public static void configureSuffixes() {
        MeterConfig.messageSuffix = MESSAGE_SUFFIX;
        MeterConfig.dataSuffix = DATA_SUFFIX;
    }

    /**
     * Applies this regime to the two loggers derived from the given base category,
     * using the same prefix/suffix rules the Meter/Watcher use.
     *
     * @param baseCategory the base logger name passed to the Meter/Watcher
     */
    public void apply(final String baseCategory) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        configure(context, MeterConfig.messagePrefix + baseCategory + MeterConfig.messageSuffix, messageLevel);
        configure(context, MeterConfig.dataPrefix + baseCategory + MeterConfig.dataSuffix, dataLevel);
    }

    private static void configure(final LoggerContext context, final String loggerName, final Level level) {
        final Logger logger = context.getLogger(loggerName);
        logger.detachAndStopAllAppenders();
        if (level != Level.OFF) {
            final DiscardAppender appender = new DiscardAppender();
            appender.setContext(context);
            appender.setName("discard");
            appender.start();
            logger.addAppender(appender);
        }
        logger.setAdditive(false);
        logger.setLevel(level);
    }
}
