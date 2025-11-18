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

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.util.List;

/**
 * Utility class providing static assertion methods for testing {@link MockLogger} instances.
 * <p>
 * This class contains methods to verify that logged events match expected criteria such as log level,
 * message content, and markers. All assertion methods are static and take a {@link Logger} instance as their
 * first parameter, automatically converting it to {@link MockLogger} for testing purposes.
 * <p>
 * Example usage:
 * <pre>{@code
 * Logger logger = LoggerFactory.getLogger("test");
 * logger.info("Test message");
 * 
 * AssertLogger.assertEvent(logger, 0, Level.INFO, "Test message");
 * }</pre>
 *
 * @author Daniel Felix Ferber
 */
public final class AssertLogger {

    private AssertLogger() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a Logger instance to MockLogger, throwing an assertion error if the conversion is not possible.
     *
     * @param logger the Logger instance to convert
     * @return the MockLogger instance
     * @throws AssertionError if the logger is not an instance of MockLogger
     */
    private static MockLogger toMockLogger(final Logger logger) {
        Assertions.assertTrue(logger instanceof MockLogger, 
            String.format("should be MockLogger instance; actual type: %s", 
                logger != null ? logger.getClass().getName() : "null"));
        return (MockLogger) logger;
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected message.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), 
            String.format("should contain expected message part; expected: %s; actual message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Marker expectedMarker) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedMarker, event.getMarker(), 
            String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and message.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), 
            String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), 
            String.format("should contain expected message part; expected: %s; actual message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected marker and message.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedMarker   the expected marker of the event
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Marker expectedMarker, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedMarker, event.getMarker(), 
            String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), 
            String.format("should contain expected message part; expected: %s; actual message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level, marker, and message.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), 
            String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), 
            String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), 
            String.format("should contain expected message part; expected: %s; actual message: %s", messagePart, event.getFormattedMessage()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level, marker, and message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     * @param messageParts     an array of substrings that should be present in the event's message
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), 
            String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), 
            String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
        for (final String messagePart : messageParts) {
            Assertions.assertTrue(event.getFormattedMessage().contains(messagePart), 
                String.format("should contain expected message part; expected: %s; actual message: %s", messagePart, event.getFormattedMessage()));
        }
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with the expected level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param expectedLevel    the expected log level of the event
     * @param expectedMarker   the expected marker of the event
     */
    public static void assertEvent(final Logger logger, final int eventIndex, final Level expectedLevel, final Marker expectedMarker) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertSame(expectedLevel, event.getLevel(), 
            String.format("should have expected log level; expected: %s, actual: %s", expectedLevel, event.getLevel()));
        Assertions.assertSame(expectedMarker, event.getMarker(), 
            String.format("should have expected marker; expected: %s, actual: %s", expectedMarker, event.getMarker()));
    }

    // Methods that assert existence of at least one event matching the criteria

    /**
     * Asserts that the logger has recorded at least one event containing the expected message part.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param messagePart      a substring that should be present in at least one event's message
     */
    public static void assertHasEvent(final Logger logger, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event containing expected message part; expected: %s", messagePart));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the expected marker
     */
    public static void assertHasEvent(final Logger logger, final Marker expectedMarker) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedMarker.equals(event.getMarker()));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected marker; expected: %s", expectedMarker));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level and message part.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertHasEvent(final Logger logger, final Level expectedLevel, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedLevel == event.getLevel() && 
                     event.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level and message part; expected level: %s, expected message: %s", 
                expectedLevel, messagePart));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected marker and message part.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedMarker   the expected marker
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertHasEvent(final Logger logger, final Marker expectedMarker, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedMarker.equals(event.getMarker()) && 
                     event.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected marker and message part; expected marker: %s, expected message: %s", 
                expectedMarker, messagePart));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level, marker, and message part.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param expectedMarker   the expected marker
     * @param messagePart      a substring that should be present in the event's message
     */
    public static void assertHasEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker, final String messagePart) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedLevel == event.getLevel() && 
                     expectedMarker.equals(event.getMarker()) && 
                     event.getFormattedMessage().contains(messagePart));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level, marker and message part; expected level: %s, expected marker: %s, expected message: %s", 
                expectedLevel, expectedMarker, messagePart));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level, marker, and all message parts.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param expectedMarker   the expected marker
     * @param messageParts     an array of substrings that should all be present in the event's message
     */
    public static void assertHasEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker, final String... messageParts) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> {
                if (expectedLevel != event.getLevel() || !expectedMarker.equals(event.getMarker())) {
                    return false;
                }
                for (final String messagePart : messageParts) {
                    if (!event.getFormattedMessage().contains(messagePart)) {
                        return false;
                    }
                }
                return true;
            });
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level, marker and all message parts; expected level: %s, expected marker: %s, expected messages: %s", 
                expectedLevel, expectedMarker, String.join(", ", messageParts)));
    }

    /**
     * Asserts that the logger has recorded at least one event with the expected level and marker.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param expectedLevel    the expected log level
     * @param expectedMarker   the expected marker
     */
    public static void assertHasEvent(final Logger logger, final Level expectedLevel, final Marker expectedMarker) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> expectedLevel == event.getLevel() && 
                     expectedMarker.equals(event.getMarker()));
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected level and marker; expected level: %s, expected marker: %s", 
                expectedLevel, expectedMarker));
    }

    // Methods for asserting throwable-related properties

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     * @param throwableClass   the expected throwable class
     */
    public static void assertEventWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        final Throwable throwable = event.getThrowable();
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(throwableClass.isInstance(throwable), 
            String.format("should have expected throwable type; expected: %s, actual: %s", 
                throwableClass.getName(), throwable.getClass().getName()));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with a throwable of the expected type and message.
     *
     * @param logger             the Logger instance to check (must be a MockLogger)
     * @param eventIndex         the index of the event to check
     * @param throwableClass     the expected throwable class
     * @param throwableMessage   a substring that should be present in the throwable's message
     */
    public static void assertEventWithThrowable(final Logger logger, final int eventIndex, final Class<? extends Throwable> throwableClass, final String throwableMessage) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        final Throwable throwable = event.getThrowable();
        Assertions.assertNotNull(throwable, "should have a throwable");
        Assertions.assertTrue(throwableClass.isInstance(throwable), 
            String.format("should have expected throwable type; expected: %s, actual: %s", 
                throwableClass.getName(), throwable.getClass().getName()));
        final String actualMessage = throwable.getMessage();
        Assertions.assertNotNull(actualMessage, "should have throwable message");
        Assertions.assertTrue(actualMessage.contains(throwableMessage), 
            String.format("should contain expected throwable message part; expected: %s; actual message: %s", throwableMessage, actualMessage));
    }

    /**
     * Asserts that the logger has recorded an event at the specified index with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param eventIndex       the index of the event to check
     */
    public static void assertEventHasThrowable(final Logger logger, final int eventIndex) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        Assertions.assertTrue(eventIndex < loggerEvents.size(), 
            String.format("should have enough logger events; requested event: %d, available events: %d", eventIndex, loggerEvents.size()));
        final MockLoggerEvent event = loggerEvents.get(eventIndex);
        Assertions.assertNotNull(event.getThrowable(), "should have a throwable");
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     * @param throwableClass   the expected throwable class
     */
    public static void assertHasEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> {
                final Throwable throwable = event.getThrowable();
                return throwable != null && throwableClass.isInstance(throwable);
            });
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected throwable type; expected: %s", throwableClass.getName()));
    }

    /**
     * Asserts that the logger has recorded at least one event with a throwable of the expected type and message.
     *
     * @param logger             the Logger instance to check (must be a MockLogger)
     * @param throwableClass     the expected throwable class
     * @param throwableMessage   a substring that should be present in the throwable's message
     */
    public static void assertHasEventWithThrowable(final Logger logger, final Class<? extends Throwable> throwableClass, final String throwableMessage) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> {
                final Throwable throwable = event.getThrowable();
                if (throwable == null || !throwableClass.isInstance(throwable)) {
                    return false;
                }
                final String actualMessage = throwable.getMessage();
                return actualMessage != null && actualMessage.contains(throwableMessage);
            });
        Assertions.assertTrue(hasEvent, 
            String.format("should have at least one event with expected throwable type and message; expected type: %s, expected message: %s", 
                throwableClass.getName(), throwableMessage));
    }

    /**
     * Asserts that the logger has recorded at least one event with any throwable.
     *
     * @param logger           the Logger instance to check (must be a MockLogger)
     */
    public static void assertHasEventWithThrowable(final Logger logger) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final boolean hasEvent = loggerEvents.stream()
            .anyMatch(event -> event.getThrowable() != null);
        Assertions.assertTrue(hasEvent, "should have at least one event with a throwable");
    }

    // Methods for asserting event counts

    /**
     * Asserts that the logger has recorded the expected number of events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param expectedCount  the expected number of events
     */
    public static void assertEventCount(final Logger logger, final int expectedCount) {
        final MockLogger mockLogger = toMockLogger(logger);
        final int actualCount = mockLogger.getEventCount();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events; expected: %d, actual: %d", expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded no events.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     */
    public static void assertNoEvents(final Logger logger) {
        assertEventCount(logger, 0);
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified level.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param level          the log level to count
     * @param expectedCount  the expected number of events with the specified level
     */
    public static void assertEventCountByLevel(final Logger logger, final Level level, final int expectedCount) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final long actualCount = loggerEvents.stream()
            .filter(event -> level == event.getLevel())
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events with level %s; expected: %d, actual: %d", 
                level, expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded the expected number of events with the specified marker.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param marker         the marker to count
     * @param expectedCount  the expected number of events with the specified marker
     */
    public static void assertEventCountByMarker(final Logger logger, final Marker marker, final int expectedCount) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final long actualCount = loggerEvents.stream()
            .filter(event -> marker.equals(event.getMarker()))
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events with marker %s; expected: %d, actual: %d", 
                marker, expectedCount, actualCount));
    }

    /**
     * Asserts that the logger has recorded the expected number of events containing the specified message part.
     *
     * @param logger         the Logger instance to check (must be a MockLogger)
     * @param messagePart    a substring that should be present in the event's message
     * @param expectedCount  the expected number of events containing the message part
     */
    public static void assertEventCountByMessage(final Logger logger, final String messagePart, final int expectedCount) {
        final MockLogger mockLogger = toMockLogger(logger);
        final List<MockLoggerEvent> loggerEvents = mockLogger.getLoggerEvents();
        final long actualCount = loggerEvents.stream()
            .filter(event -> event.getFormattedMessage().contains(messagePart))
            .count();
        Assertions.assertEquals(expectedCount, actualCount, 
            String.format("should have expected number of events containing message part '%s'; expected: %d, actual: %d", 
                messagePart, expectedCount, actualCount));
    }
}