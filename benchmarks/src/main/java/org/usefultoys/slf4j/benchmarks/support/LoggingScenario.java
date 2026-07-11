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
import org.usefultoys.slf4j.watcher.WatcherConfig;

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
 * When a logger is enabled it is set at a level that covers <em>every</em> lifecycle point,
 * not only the terminal ones: the message logger uses DEBUG so {@code start()} (DEBUG) logs
 * alongside {@code ok()}/{@code reject()} (INFO) and {@code fail()} (ERROR); the data logger
 * uses TRACE, which the code checks at every point.
 * <p>
 * Note on reachability &mdash; and this differs between the two components:
 * <ul>
 *   <li><b>Meter</b>: the data statement is nested inside the message-level guard
 *       ({@code if (messageLogger.isXxxEnabled()) { ... if (dataLogger.isTraceEnabled()) ... }}),
 *       so {@link #DATA_ONLY} emits nothing and costs the same as {@link #OFF}. The three
 *       meaningful regimes are {@link #OFF}, {@link #MESSAGE} and {@link #MESSAGE_DATA}
 *       ({@code DATA_ONLY} is kept only to verify that equivalence empirically).</li>
 *   <li><b>Watcher</b>: {@code run()} collects when {@code isInfoEnabled() || isTraceEnabled()}
 *       and emits the message and data lines independently (not nested), so {@link #DATA_ONLY}
 *       <em>is</em> a distinct, reachable regime. All four apply.</li>
 * </ul>
 * <p>
 * Enabled loggers are routed to a {@link DiscardAppender} (additivity off) so the measured
 * cost is the string construction inside slf4j-toys, not logback's encoder or I/O.
 */
public enum LoggingScenario {
    /** Both loggers OFF: nothing logged, pure instrumentation floor. */
    OFF(Level.OFF, Level.OFF),
    /**
     * Human-readable messages only. The message logger is at DEBUG (not INFO) on purpose,
     * so the activation covers <em>every</em> Meter lifecycle point &mdash; {@code start()}
     * logs at DEBUG, {@code ok()} at INFO/WARN, {@code reject()} at INFO, {@code fail()} at
     * ERROR &mdash; not just the terminal ones. JSON5 data off.
     */
    MESSAGE(Level.DEBUG, Level.OFF),
    /**
     * Both human-readable messages and JSON5 data, at every lifecycle point. Message logger
     * at DEBUG (covers start + ok/reject/fail); data logger at TRACE.
     */
    MESSAGE_DATA(Level.DEBUG, Level.TRACE),
    /**
     * JSON5 data only (message logger off). Reachable for the <b>Watcher</b>, which emits its
     * message and data lines independently. For the <b>Meter</b> this is <em>unreachable</em>
     * and equals {@link #OFF}: every {@code dataLogger.trace(...)} is nested inside a
     * {@code messageLogger.isXxxEnabled()} guard, so with the message logger off no data is
     * ever emitted, at start or at any terminal.
     */
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
     * Splits the message and data loggers onto distinct names for <em>both</em> the
     * Meter and the Watcher configuration. Call once per trial, before any
     * Meter/Watcher is constructed, since the suffixes are read at construction time
     * (each resolves both loggers in its constructor). Prefixes are left at their
     * empty defaults, so a base category {@code "c"} yields {@code "c" + MESSAGE_SUFFIX}
     * and {@code "c" + DATA_SUFFIX} for both components.
     */
    public static void configureSuffixes() {
        MeterConfig.messageSuffix = MESSAGE_SUFFIX;
        MeterConfig.dataSuffix = DATA_SUFFIX;
        WatcherConfig.messageSuffix = MESSAGE_SUFFIX;
        WatcherConfig.dataSuffix = DATA_SUFFIX;
    }

    /**
     * Applies this regime to the two loggers derived from the given base category.
     * Assumes {@link #configureSuffixes()} has run and prefixes are empty, so the
     * derived names are {@code baseCategory + MESSAGE_SUFFIX} and
     * {@code baseCategory + DATA_SUFFIX} — shared by Meter and Watcher alike.
     *
     * @param baseCategory the base logger name passed to the Meter/Watcher
     */
    public void apply(final String baseCategory) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        configure(context, baseCategory + MESSAGE_SUFFIX, messageLevel);
        configure(context, baseCategory + DATA_SUFFIX, dataLevel);
    }

    private static void configure(final LoggerContext context, final String loggerName, final Level level) {
        final Logger logger = context.getLogger(loggerName);
        logger.detachAndStopAllAppenders();
        if (!Level.OFF.equals(level)) {
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
