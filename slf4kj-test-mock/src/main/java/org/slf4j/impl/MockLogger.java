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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Creates a new MockLogger with the specified name.
     *
     * @param name the name of this logger
     */
    public MockLogger(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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

    /**
     * Logs a message with optional arguments at the specified level.
     *
     * @param level  the log level
     * @param format the message format string
     * @param args   the message arguments
     */
    private void log(final Level level, final String format, final Object... args) {
        addLoggingEvent(level, null, null, format, args);
    }

    /**
     * Logs a message with a throwable at the specified level.
     *
     * @param level     the log level
     * @param msg       the message
     * @param throwable the throwable
     */
    private void log(final Level level, final String msg, final Throwable throwable) {
        addLoggingEvent(level, null, throwable, msg, (Object[]) null);
    }

    /**
     * Logs a message with marker and optional arguments at the specified level.
     *
     * @param level  the log level
     * @param marker the marker
     * @param format the message format string
     * @param args   the message arguments
     */
    private void log(final Level level, final Marker marker, final String format, final Object... args) {
        addLoggingEvent(level, marker, null, format, args);
    }

    /**
     * Logs a message with marker and throwable at the specified level.
     *
     * @param level     the log level
     * @param marker    the marker
     * @param msg       the message
     * @param throwable the throwable
     */
    private void log(final Level level, final Marker marker, final String msg, final Throwable throwable) {
        addLoggingEvent(level, marker, throwable, msg, (Object[]) null);
    }

    /**
     * Adds a logging event to the internal list and prints it if printing is enabled.
     *
     * @param level     the log level
     * @param marker    the optional marker
     * @param throwable the optional throwable
     * @param format    the message format string
     * @param args      the message arguments
     */
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
    /**
     * Prints the log event to the appropriate output stream (stdout or stderr) based on its level and
     * if the logger had printing enabled.
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
    /**
     * Formats a log event for display output.
     *
     * @param event the log event to format
     * @return the formatted log statement string
     */
    private String formatLogStatement(final MockLoggerEvent event) {
        if (event.getThrowable() == null) {
            return event.getLevel() + " " + event.getLoggerName() + ": " + event.getFormattedMessage();
        }
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        event.getThrowable().printStackTrace(new PrintStream(s));
        final String st = s.toString();
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
     * Returns the list of all logged events.
     * The returned list is unmodifiable to prevent external modification.
     *
     * @return an unmodifiable list of all logged events
     */
    public List<MockLoggerEvent> getLoggerEvents() {
        return Collections.unmodifiableList(loggerEvents);
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected message.
     *
     * @param eventIndex       the index of the event to check
     * @param messagePart a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final String messagePart) {
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), String.format("Message does not contain expected string; expected: %s; message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     */
    public void assertEvent(final int eventIndex, final Marker expectedMarker) {
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedMarker, event.getMarker(), String.format("Logger expectedMarker does not match; expected: %s, actual: %s", expectedMarker, event.getMarker()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and message.
     *
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param messagePart a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final String messagePart) {
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), String.format("Logger expectedLevel does not match; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), String.format("Message does not contain expected string; expected: %s; message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker and message.
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     * @param messagePart a substring that should be present in the event's message
     */
    public void assertEvent(final int eventIndex, final Marker expectedMarker, final String messagePart) {
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedMarker, event.getMarker(), String.format("Logger expectedMarker does not match; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), String.format("Message does not contain expected string; expected: %s; message: %s", messagePart, event.getFormattedMessage()));
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
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), String.format("Logger expectedLevel does not match; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), String.format("Logger expectedMarker does not match; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), String.format("Message does not contain expected string; expected: %s; message: %s", messagePart, event.getFormattedMessage()));
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
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), String.format("Logger expectedLevel does not match; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), String.format("Logger expectedMarker does not match; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        for (final String messagePart : messageParts) {
//            Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), String.format("Message does not contain expected string; expected: %s; message: %s", messagePart, event.getFormattedMessage()));
        }
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and marker.
     *
     * @param eventIndex     the index of the event to check
     * @param expectedLevel  the expected log level of the event
     * @param expectedMarker the expected marker of the event
     */
    public void assertEvent(final int eventIndex, final Level expectedLevel, final Marker expectedMarker) {
        Assertions.assertTrue(eventIndex < loggerEvents.size(), String.format("Not enough logger event; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), String.format("Logger expectedLevel does not match; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), String.format("Logger expectedMarker does not match; expected: %s, actual: %s", expectedMarker, event.getMarker()));
    }

    /**
     * Returns all logged messages as a single text string, with each message on a separate line.
     * This is useful for debugging or when you need to verify the complete log output.
     *
     * @return a string containing all formatted log messages, separated by system line separators
     */
    public String toText() {
        return getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
