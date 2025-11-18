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

import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.logging.Level;

/**
 * Utility class providing static assertion methods for testing {@link MockHandler} instances.
 * <p>
 * This class contains methods to verify that logged records match expected criteria such as log level
 * and message content. All assertion methods are static and take a {@link MockHandler} instance as their
 * first parameter.
 * <p>
 * Example usage:
 * <pre>{@code
 * Logger logger = Logger.getLogger("test");
 * MockHandler handler = new MockHandler();
 * logger.addHandler(handler);
 * logger.info("Test message");
 * 
 * AssertHandler.assertRecord(handler, 0, Level.INFO, "Test message");
 * }</pre>
 *
 * @author Daniel Felix Ferber
 */
public final class AssertHandler {

    private AssertHandler() {
        // Utility class - prevent instantiation
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected message.
     *
     * @param handler     the MockHandler instance to check
     * @param recordIndex the index of the record to check
     * @param messagePart a substring that should be present in the record's message
     */
    public static void assertRecord(final MockHandler handler, final int recordIndex, final String messagePart) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        Assertions.assertTrue(record.getFormattedMessage().contains(messagePart),
                String.format("should contain expected message part; expected: %s; actual message: %s",
                        messagePart, record.getFormattedMessage()));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected level and message.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param expectedLevel the expected log level of the record
     * @param messagePart   a substring that should be present in the record's message
     */
    public static void assertRecord(final MockHandler handler, final int recordIndex, final Level expectedLevel,
                                     final String messagePart) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        Assertions.assertSame(expectedLevel, record.getLevel(),
                String.format("should have expected log level; expected: %s, actual: %s",
                        expectedLevel, record.getLevel()));
        Assertions.assertTrue(record.getFormattedMessage().contains(messagePart),
                String.format("should contain expected message part; expected: %s; actual message: %s",
                        messagePart, record.getFormattedMessage()));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with the expected level and message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param recordIndex   the index of the record to check
     * @param expectedLevel the expected log level of the record
     * @param messageParts  an array of substrings that should be present in the record's message
     */
    public static void assertRecord(final MockHandler handler, final int recordIndex, final Level expectedLevel,
                                     final String... messageParts) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        Assertions.assertSame(expectedLevel, record.getLevel(),
                String.format("should have expected log level; expected: %s, actual: %s",
                        expectedLevel, record.getLevel()));
        for (final String messagePart : messageParts) {
            Assertions.assertTrue(record.getFormattedMessage().contains(messagePart),
                    String.format("should contain expected message part; expected: %s; actual message: %s",
                            messagePart, record.getFormattedMessage()));
        }
    }

    /**
     * Asserts that the handler has recorded at least one record containing the expected message part.
     *
     * @param handler     the MockHandler instance to check
     * @param messagePart a substring that should be present in at least one record's message
     */
    public static void assertHasRecord(final MockHandler handler, final String messagePart) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> record.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record containing expected message part; expected: %s",
                        messagePart));
    }

    /**
     * Asserts that the handler has recorded at least one record with the expected level and message part.
     *
     * @param handler       the MockHandler instance to check
     * @param expectedLevel the expected log level
     * @param messagePart   a substring that should be present in the record's message
     */
    public static void assertHasRecord(final MockHandler handler, final Level expectedLevel, final String messagePart) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> expectedLevel == record.getLevel() &&
                        record.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected level and message part; expected level: %s, expected message: %s",
                        expectedLevel, messagePart));
    }

    /**
     * Asserts that the handler has recorded at least one record with the expected level and all message parts.
     *
     * @param handler       the MockHandler instance to check
     * @param expectedLevel the expected log level
     * @param messageParts  an array of substrings that should all be present in the record's message
     */
    public static void assertHasRecord(final MockHandler handler, final Level expectedLevel,
                                        final String... messageParts) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    if (expectedLevel != record.getLevel()) {
                        return false;
                    }
                    for (final String messagePart : messageParts) {
                        if (!record.getFormattedMessage().contains(messagePart)) {
                            return false;
                        }
                    }
                    return true;
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected level and all message parts; expected level: %s, expected messages: %s",
                        expectedLevel, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with a throwable of the expected type.
     *
     * @param handler        the MockHandler instance to check
     * @param recordIndex    the index of the record to check
     * @param throwableClass the expected throwable class
     */
    public static void assertRecordWithThrowable(final MockHandler handler, final int recordIndex,
                                                  final Class<? extends Throwable> throwableClass) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        final Throwable throwable = record.getThrown();
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(throwableClass.isInstance(throwable),
                String.format("should have expected throwable type; expected: %s, actual: %s",
                        throwableClass.getName(), throwable.getClass().getName()));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with a throwable of the expected type and message.
     *
     * @param handler          the MockHandler instance to check
     * @param recordIndex      the index of the record to check
     * @param throwableClass   the expected throwable class
     * @param throwableMessage a substring that should be present in the throwable's message
     */
    public static void assertRecordWithThrowable(final MockHandler handler, final int recordIndex,
                                                  final Class<? extends Throwable> throwableClass,
                                                  final String throwableMessage) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        final Throwable throwable = record.getThrown();
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(throwableClass.isInstance(throwable),
                String.format("should have expected throwable type; expected: %s, actual: %s",
                        throwableClass.getName(), throwable.getClass().getName()));
        final String actualMessage = throwable.getMessage();
        Assertions.assertNotNull(actualMessage, "should have throwable message");
        Assertions.assertTrue(actualMessage.contains(throwableMessage),
                String.format("should contain expected throwable message part; expected: %s; actual message: %s",
                        throwableMessage, actualMessage));
    }

    /**
     * Asserts that the handler has recorded a record at the specified index with any throwable.
     *
     * @param handler     the MockHandler instance to check
     * @param recordIndex the index of the record to check
     */
    public static void assertRecordHasThrowable(final MockHandler handler, final int recordIndex) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        Assertions.assertTrue(recordIndex < logRecords.size(),
                String.format("should have enough log records; requested record: %d, available records: %d",
                        recordIndex, logRecords.size()));
        final MockLogRecord record = logRecords.get(recordIndex);
        Assertions.assertNotNull(record.getThrown(), "should have a throwable");
    }

    /**
     * Asserts that the handler has recorded at least one record with a throwable of the expected type.
     *
     * @param handler        the MockHandler instance to check
     * @param throwableClass the expected throwable class
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler,
                                                     final Class<? extends Throwable> throwableClass) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    final Throwable throwable = record.getThrown();
                    return throwable != null && throwableClass.isInstance(throwable);
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected throwable type; expected: %s",
                        throwableClass.getName()));
    }

    /**
     * Asserts that the handler has recorded at least one record with a throwable of the expected type and message.
     *
     * @param handler          the MockHandler instance to check
     * @param throwableClass   the expected throwable class
     * @param throwableMessage a substring that should be present in the throwable's message
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler,
                                                     final Class<? extends Throwable> throwableClass,
                                                     final String throwableMessage) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> {
                    final Throwable throwable = record.getThrown();
                    if (throwable == null || !throwableClass.isInstance(throwable)) {
                        return false;
                    }
                    final String actualMessage = throwable.getMessage();
                    return actualMessage != null && actualMessage.contains(throwableMessage);
                });
        Assertions.assertTrue(hasRecord,
                String.format("should have at least one record with expected throwable type and message; expected type: %s, expected message: %s",
                        throwableClass.getName(), throwableMessage));
    }

    /**
     * Asserts that the handler has recorded at least one record with any throwable.
     *
     * @param handler the MockHandler instance to check
     */
    public static void assertHasRecordWithThrowable(final MockHandler handler) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final boolean hasRecord = logRecords.stream()
                .anyMatch(record -> record.getThrown() != null);
        Assertions.assertTrue(hasRecord, "should have at least one record with a throwable");
    }

    /**
     * Asserts that the handler has recorded the expected number of records.
     *
     * @param handler       the MockHandler instance to check
     * @param expectedCount the expected number of records
     */
    public static void assertRecordCount(final MockHandler handler, final int expectedCount) {
        final int actualCount = handler.getRecordCount();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records; expected: %d, actual: %d",
                        expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded no records.
     *
     * @param handler the MockHandler instance to check
     */
    public static void assertNoRecords(final MockHandler handler) {
        assertRecordCount(handler, 0);
    }

    /**
     * Asserts that the handler has recorded the expected number of records with the specified level.
     *
     * @param handler       the MockHandler instance to check
     * @param level         the log level to count
     * @param expectedCount the expected number of records with the specified level
     */
    public static void assertRecordCountByLevel(final MockHandler handler, final Level level,
                                                 final int expectedCount) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final long actualCount = logRecords.stream()
                .filter(record -> level == record.getLevel())
                .count();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records with level %s; expected: %d, actual: %d",
                        level, expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded the expected number of records containing the specified message part.
     *
     * @param handler       the MockHandler instance to check
     * @param messagePart   a substring that should be present in the record's message
     * @param expectedCount the expected number of records containing the message part
     */
    public static void assertRecordCountByMessage(final MockHandler handler, final String messagePart,
                                                   final int expectedCount) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();
        final long actualCount = logRecords.stream()
                .filter(record -> record.getFormattedMessage().contains(messagePart))
                .count();
        Assertions.assertEquals(expectedCount, actualCount,
                String.format("should have expected number of records containing message part '%s'; expected: %d, actual: %d",
                        messagePart, expectedCount, actualCount));
    }

    /**
     * Asserts that the handler has recorded records in the exact sequence of log levels specified.
     *
     * @param handler        the MockHandler instance to check
     * @param expectedLevels the expected sequence of log levels
     */
    public static void assertRecordSequence(final MockHandler handler, final Level... expectedLevels) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();

        Assertions.assertEquals(expectedLevels.length, logRecords.size(),
                String.format("should have expected number of records for sequence; expected: %d, actual: %d",
                        expectedLevels.length, logRecords.size()));

        for (int i = 0; i < expectedLevels.length; i++) {
            final Level actualLevel = logRecords.get(i).getLevel();
            Assertions.assertSame(expectedLevels[i], actualLevel,
                    String.format("should have expected level at position %d; expected: %s, actual: %s",
                            i, expectedLevels[i], actualLevel));
        }
    }

    /**
     * Asserts that the handler has recorded records containing message parts in the exact sequence specified.
     *
     * @param handler              the MockHandler instance to check
     * @param expectedMessageParts the expected sequence of message parts
     */
    public static void assertRecordSequence(final MockHandler handler, final String... expectedMessageParts) {
        final List<MockLogRecord> logRecords = handler.getLogRecords();

        Assertions.assertEquals(expectedMessageParts.length, logRecords.size(),
                String.format("should have expected number of records for sequence; expected: %d, actual: %d",
                        expectedMessageParts.length, logRecords.size()));

        for (int i = 0; i < expectedMessageParts.length; i++) {
            final String actualMessage = logRecords.get(i).getFormattedMessage();
            Assertions.assertTrue(actualMessage.contains(expectedMessageParts[i]),
                    String.format("should contain expected message part at position %d; expected: %s, actual message: %s",
                            i, expectedMessageParts[i], actualMessage));
        }
    }
}
