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
package org.usefultoys.jul.mock;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * A test-friendly implementation of the JUL {@link Handler} interface.
 * <p>
 * This handler captures log records in memory, allowing tests to inspect log output by accessing the recorded
 * {@link MockLogRecord} instances. It supports all log levels (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE)
 * and provides configuration methods to control logging behavior during test execution.
 * <p>
 * Each log record may be printed to {@code System.out} or {@code System.err} based on its severity, and is always
 * stored in an internal list for later inspection.
 * <p>
 * This handler is primarily intended to be used in unit tests where you need to verify that code produces expected
 * log output. Attach it to a logger using {@code logger.addHandler(mockHandler)}.
 * <p>
 * Example usage in test assertions:
 * <pre>{@code
 * Logger logger = Logger.getLogger("test.logger");
 * MockHandler handler = new MockHandler();
 * logger.addHandler(handler);
 * logger.setUseParentHandlers(false);
 * 
 * logger.info("Sample message");
 * 
 * assertEquals(1, handler.getRecordCount());
 * assertTrue(handler.getRecord(0).getFormattedMessage().contains("Sample"));
 * }</pre>
 * <p>
 * Note: This class is not thread-safe and should be used in single-threaded test contexts only.
 *
 * @author Daniel Felix Ferber
 */
public class MockHandler extends Handler {

    /**
     * Whether to print log records at FINEST, FINER, FINE, CONFIG, and INFO levels to stdout.
     */
    @Getter
    @Setter
    private boolean stdoutEnabled = false;

    /**
     * Whether to print log records at WARNING and SEVERE levels to stderr.
     */
    @Getter
    @Setter
    private boolean stderrEnabled = false;

    private final List<MockLogRecord> logRecords = new ArrayList<>();

    /**
     * Creates a new MockHandler with default settings.
     * <p>
     * By default, the handler captures all log levels (FINEST and above) but does not print to console.
     */
    public MockHandler() {
        setLevel(Level.ALL);
    }

    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        final MockLogRecord mockRecord = new MockLogRecord(record);
        logRecords.add(mockRecord);
        print(mockRecord);
    }

    @Override
    public void flush() {
        // No buffering, nothing to flush
    }

    @Override
    public void close() throws SecurityException {
        clearRecords();
    }

    /**
     * Clears all recorded log records.
     */
    public void clearRecords() {
        logRecords.clear();
    }

    /**
     * Returns the number of records captured by this handler.
     *
     * @return the number of recorded log records
     */
    public int getRecordCount() {
        return logRecords.size();
    }

    /**
     * Returns the record at the specified index.
     *
     * @param recordIndex the index of the record to retrieve
     * @return the record at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public MockLogRecord getRecord(final int recordIndex) {
        return logRecords.get(recordIndex);
    }

    /**
     * Returns the list of all logged records.
     * The returned list is unmodifiable to prevent external modification.
     *
     * @return an unmodifiable list of all logged records
     */
    public List<MockLogRecord> getLogRecords() {
        return Collections.unmodifiableList(logRecords);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected message.
     *
     * @param recordIndex the index of the record to check
     * @param messagePart a substring that should be present in the record's message
     */
    public void assertRecord(final int recordIndex, final String messagePart) {
        AssertHandler.assertRecord(this, recordIndex, messagePart);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected level and message.
     *
     * @param recordIndex   the index of the record to check
     * @param expectedLevel the expected log level of the record
     * @param messagePart   a substring that should be present in the record's message
     */
    public void assertRecord(final int recordIndex, final Level expectedLevel, final String messagePart) {
        AssertHandler.assertRecord(this, recordIndex, expectedLevel, messagePart);
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected level and message parts.
     *
     * @param recordIndex   the index of the record to check
     * @param expectedLevel the expected log level of the record
     * @param messageParts  an array of substrings that should be present in the record's message
     */
    public void assertRecord(final int recordIndex, final Level expectedLevel, final String... messageParts) {
        AssertHandler.assertRecord(this, recordIndex, expectedLevel, messageParts);
    }

    /**
     * Returns all logged messages as a single text string, with each message on a separate line.
     * This is useful for debugging or when you need to verify the complete log output.
     *
     * @return a string containing all formatted log messages, separated by system line separators
     */
    public String toText() {
        return getLogRecords().stream()
                .map(MockLogRecord::getFormattedMessage)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Prints the log record to the appropriate output stream (stdout or stderr) based on its level and
     * if the handler has printing enabled.
     *
     * @param record the log record to print
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void print(final MockLogRecord record) {
        final boolean isStderr = record.getLevel().intValue() >= Level.WARNING.intValue();
        if (isStderr && !stderrEnabled) {
            return;
        } else if (!isStderr && !stdoutEnabled) {
            return;
        }

        final PrintStream ps = isStderr ? System.err : System.out;
        ps.println(formatLogStatement(record));
        ps.flush();
    }

    /**
     * Formats a log record for display output.
     *
     * @param record the log record to format
     * @return the formatted log statement string
     */
    private String formatLogStatement(final MockLogRecord record) {
        if (record.getThrown() == null) {
            return record.getLevel() + " " + record.getLoggerName() + ": " + record.getFormattedMessage();
        }
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        record.getThrown().printStackTrace(new PrintStream(s));
        final String st = s.toString();
        return record.getLevel() + " " + record.getLoggerName() + ": " + record.getFormattedMessage() + "\n" + st;
    }
}
