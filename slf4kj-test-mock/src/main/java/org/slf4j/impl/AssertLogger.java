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
}