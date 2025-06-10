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
package org.slf4j.impl;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A test-friendly implementation of the SLF4J {@link Logger} interface.
 * <p>
 * This logger captures log events in memory, allowing tests to inspect log output by accessing the recorded
 * {@link MockLoggerEvent} instances. It supports all log levels (TRACE, DEBUG, INFO, WARN, ERROR) and provides
 * configuration methods to enable or disable specific levels during test execution.
 * <p>
 * Each log event may be printed to {@code System.out} or {@code System.err} based on its severity, and is always stored in an
 * internal list for later inspection.
 * <p>
 * This logger is primarily intended to be used together with {@link MockLoggerFactory} during test runs, where SLF4J
 * dynamically loads this implementation via its SPI mechanism.
 * <p>
 * Example usage in test assertions:
 * <pre>{@code
 * Logger logger = LoggerFactory.getLogger("test.logger");
 * logger.info("Sample message");
 *
 * MockLogger testLogger = (MockLogger) logger;
 * assertEquals(1, testLogger.getEventCount());
 * assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Sample"));
 * }</pre>
 * <p>
 * Note: This class is not thread-safe and should be used in single-threaded test contexts only.
 *
 * @author Daniel Felix Ferber
 */
public class MockLogger implements Logger {

    private final String name;
    /* No concept of "LEVEL" here, because it is not a real logger implementation. */
    @Getter
    @Setter
    private boolean traceEnabled = true;
    @Getter
    @Setter
    private boolean debugEnabled = true;
    @Getter
    @Setter
    private boolean infoEnabled = true;
    @Getter
    @Setter
    private boolean warnEnabled = true;
    @Getter
    @Setter
    private boolean errorEnabled = true;
    /**
     * Whether to print log trace, debug and info events to stdout.
     */
    @Getter
    @Setter
    private boolean stdoutEnabled = false;
    /**
     * Whether to print log warn and error events to stderr.
     */
    @Getter
    @Setter
    private boolean stderrEnabled = false;

    private final List<MockLoggerEvent> loggerEvents = new ArrayList<MockLoggerEvent>();

    public MockLogger(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    private void addEvent(final MockLoggerEvent event) {
        loggerEvents.add(event);
    }

    /**
     * Clears all recorded log events.
     */
    public void clearEvents() {
        loggerEvents.clear();
    }

    /**
     * Enables or disables all logging levels.
     *
     * @param enabled true to enable all levels, false to disable them
     */
    public void setEnabled(final boolean enabled) {
        errorEnabled = enabled;
        warnEnabled = enabled;
        infoEnabled = enabled;
        debugEnabled = enabled;
        traceEnabled = enabled;
    }

    @Override
    public void trace(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void trace(final String format, final Object arg) {
        log(Level.TRACE, format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        log(Level.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        log(Level.TRACE, format, args);
    }

    @Override
    public void trace(final String msg, final Throwable throwable) {
        log(Level.TRACE, msg, throwable);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return traceEnabled;
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        log(Level.TRACE, marker, msg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        log(Level.TRACE, marker, format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.TRACE, marker, format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... args) {
        log(Level.TRACE, marker, format, args);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.TRACE, marker, msg, throwable);
    }

    @Override
    public void debug(final String message) {
        log(Level.DEBUG, message);
    }

    @Override
    public void debug(final String format, final Object arg) {
        log(Level.DEBUG, format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        log(Level.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... args) {
        log(Level.DEBUG, format, args);
    }

    @Override
    public void debug(final String msg, final Throwable throwable) {
        log(Level.DEBUG, msg, throwable);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return debugEnabled;
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        log(Level.DEBUG, marker, msg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        log(Level.DEBUG, marker, format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.DEBUG, marker, format, arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... args) {
        log(Level.DEBUG, marker, format, args);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.DEBUG, marker, msg, throwable);
    }

    @Override
    public void info(final String message) {
        log(Level.INFO, message);
    }

    @Override
    public void info(final String format, final Object arg) {
        log(Level.INFO, format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        log(Level.INFO, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        log(Level.INFO, format, args);
    }

    @Override
    public void info(final String msg, final Throwable throwable) {
        log(Level.INFO, msg, throwable);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return infoEnabled;
    }

    @Override
    public void info(final Marker marker, final String msg) {
        log(Level.INFO, marker, msg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        log(Level.INFO, marker, format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.INFO, marker, format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... args) {
        log(Level.INFO, marker, format, args);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.INFO, marker, msg, throwable);
    }

    @Override
    public void warn(final String message) {
        log(Level.WARN, message);
    }

    @Override
    public void warn(final String format, final Object arg) {
        log(Level.WARN, format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        log(Level.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        log(Level.WARN, format, args);
    }

    @Override
    public void warn(final String msg, final Throwable throwable) {
        log(Level.WARN, msg, throwable);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return warnEnabled;
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        log(Level.WARN, marker, msg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        log(Level.WARN, marker, format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.WARN, marker, format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... args) {
        log(Level.WARN, marker, format, args);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.WARN, marker, msg, throwable);
    }

    @Override
    public void error(final String message) {
        log(Level.ERROR, message);
    }

    @Override
    public void error(final String format, final Object arg) {
        log(Level.ERROR, format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        log(Level.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        log(Level.ERROR, format, args);
    }

    @Override
    public void error(final String msg, final Throwable throwable) {
        log(Level.ERROR, msg, throwable);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return errorEnabled;
    }

    @Override
    public void error(final Marker marker, final String msg) {
        log(Level.ERROR, marker, msg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        log(Level.ERROR, marker, format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.ERROR, marker, format, arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... args) {
        log(Level.ERROR, marker, format, args);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.ERROR, marker, msg, throwable);
    }

    private void log(final Level level, final String format, final Object... args) {
        addLoggingEvent(level, null, null, format, args);
    }

    private void log(final Level level, final String msg, final Throwable throwable) {
        addLoggingEvent(level, null, throwable, msg, (Object[]) null);
    }

    private void log(final Level level, final Marker marker, final String format, final Object... args) {
        addLoggingEvent(level, marker, null, format, args);
    }

    private void log(final Level level, final Marker marker, final String msg, final Throwable throwable) {
        addLoggingEvent(level, marker, throwable, msg, (Object[]) null);
    }

    private void addLoggingEvent(
            final Level level,
            final Marker marker,
            final Throwable throwable,
            final String format,
            final Object... args) {
        final MockLoggerEvent event = new MockLoggerEvent(name, level, null, marker, throwable, format, args);
        loggerEvents.add(event);
        print(event);
    }

    /**
     * Prints the log event to the appropriate output stream (stdout or stderr) based on its level and if the
     * logger had printing enabled.
     *
     * @param event the log event to print
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void print(final MockLoggerEvent event) {
        final boolean isStderr = event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN;
        if (isStderr && !stderrEnabled) {
            return;
        } else if (!stdoutEnabled) {
            return;
        }
        final PrintStream ps = isStderr ? System.err : System.out;
        ps.println(formatLogStatement(event));
        ps.flush();
    }

    /**
     * Formats the log statement for printing.
     *
     * @param event the log event to format
     * @return the formatted log statement
     */
    private String formatLogStatement(final MockLoggerEvent event) {
        if (event.getThrowable() == null) {
            return event.getLevel() + " " + event.getLoggerName() + ": " + event.getFormattedMessage();
        }
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        event.getThrowable().printStackTrace(new PrintStream(s));
        final String st = s.toString(StandardCharsets.UTF_8);
        return event.getLevel() + " " + event.getLoggerName() + ": " + event.getFormattedMessage() + "\n" + st;
    }

    /**
     * Returns the number of events recorded by this logger.
     *
     * @return the number of recorded events
     */
    public int getEventCount() {
        return loggerEvents.size();
    }

    /**
     * Returns the event at the specified index.
     *
     * @param eventIndex the index of the event to retrieve
     * @return the event at the specified index
     */
    public MockLoggerEvent getEvent(final int eventIndex) {
        return loggerEvents.get(eventIndex);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected message.
     *
     * @param eventIndex       the index of the event to check
     * @param exepectedMessage a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final String exepectedMessage) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertTrue(event.getFormattedMessage().contains(exepectedMessage), "Message does not contain expected string");
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final int eventIndex, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertSame(expectedMarker, event.getMarker(), "Logger expectedMarker does not match");
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and message.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param exepectedMessage a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final String exepectedMessage) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertSame(expectedLevel, event.getLevel(), "Logger expectedLevel does not match");
        Assertions.assertTrue(event.getFormattedMessage().contains(exepectedMessage), "Message does not contain expected string");
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker and message.
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     * @param exepectedMessage a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Marker expectedMarker, final String exepectedMessage) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertTrue(event.getFormattedMessage().contains(exepectedMessage), "Message does not contain expected string");
        Assertions.assertSame(expectedMarker, event.getMarker(), "Logger expectedMarker does not match");
    }



    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level, marker, and message.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     * @param messagePart a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String messagePart) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertSame(expectedLevel, event.getLevel(), "Logger expectedLevel does not match");
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), "Message does not contain expected string");
        Assertions.assertSame(expectedMarker, event.getMarker(), "Logger expectedMarker does not match");
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level, marker, and message.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertSame(expectedLevel, event.getLevel(), "Logger expectedLevel does not match");
        for (String messagePart : messageParts) {
            Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), "Message does not contain expected string: " + messagePart);
        }
        Assertions.assertSame(expectedMarker, event.getMarker(), "Logger expectedMarker does not match");
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and marker.
     *
     * @param eventIndex     the index of the event to check
     * @param expectedLevel  the expected log level of the event
     * @param expectedMarker the expected marker of the event
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final Marker expectedMarker) {
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(eventIndex < loggerEvents.size(), "Not enough logger messages");
        Assertions.assertSame(expectedLevel, event.getLevel(), "Logger expectedLevel does not match");
        Assertions.assertSame(expectedMarker, event.getMarker(), "Logger expectedMarker does not match");
    }
}
