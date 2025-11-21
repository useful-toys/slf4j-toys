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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssertLogger}.
 */
@DisplayName("AssertLogger")
class AssertLoggerTest {

    @Nested
    @DisplayName("assertEvent with message parts")
    class AssertEventWithMessagePart {

        @Test
        @DisplayName("should pass when message contains expected part")
        void shouldPassWhenMessageContainsExpectedPart() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");
            AssertLogger.assertEvent(logger, 0, "World");
        }

        @Test
        @DisplayName("should pass with multiple message parts")
        void shouldPassWithMultipleMessageParts() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello beautiful World");
            AssertLogger.assertEvent(logger, 0, "Hello", "World");
        }

        @Test
        @DisplayName("should throw when message does not contain expected part")
        void shouldThrowWhenMessageDoesNotContainExpectedPart() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, "Universe"));
            assertTrue(error.getMessage().contains("should contain expected message part"));
        }

        @Test
        @DisplayName("should throw when one of multiple message parts is missing")
        void shouldThrowWhenOneOfMultipleMessagePartsIsMissing() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello beautiful World");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, "Hello", "Universe"));
            assertTrue(error.getMessage().contains("should contain expected message part"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEvent with marker")
    class AssertEventWithMarker {

        @Test
        @DisplayName("should pass when marker matches")
        void shouldPassWhenMarkerMatches() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            logger.info(marker, "Test message");
            AssertLogger.assertEvent(logger, 0, marker);
        }

        @Test
        @DisplayName("should throw when marker does not match")
        void shouldThrowWhenMarkerDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("TEST1");
            final Marker marker2 = MarkerFactory.getMarker("TEST2");
            logger.info(marker1, "Test message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, marker2));
            assertTrue(error.getMessage().contains("should have expected marker"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, marker));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEvent with level and message")
    class AssertEventWithLevelAndMessage {

        @Test
        @DisplayName("should pass when level and message match")
        void shouldPassWhenLevelAndMessageMatch() {
            final Logger logger = new MockLogger("test");
            logger.warn("Warning message");
            AssertLogger.assertEvent(logger, 0, Level.WARN, "Warning");
        }

        @Test
        @DisplayName("should throw when level does not match")
        void shouldThrowWhenLevelDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Info message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.ERROR, "Info"));
            assertTrue(error.getMessage().contains("should have expected log level"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.INFO, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("logger type conversion")
    class LoggerTypeConversion {

        @Test
        @DisplayName("should throw when logger is not MockLogger instance")
        void shouldThrowWhenLoggerIsNotMockLoggerInstance() {
            final Logger logger = new TestLogger(); // Fake logger implementation
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, "test"));
            assertTrue(error.getMessage().contains("should be MockLogger instance"));
        }

        @Test
        @DisplayName("should throw when logger is null")
        void shouldThrowWhenLoggerIsNull() {
            final Logger logger = null;
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, "test"));
            assertTrue(error.getMessage().contains("should be MockLogger instance"));
        }

        /**
         * Test implementation of Logger that is not MockLogger
         */
        private class TestLogger implements Logger {
            @Override public String getName() { return "test"; }
            @Override public boolean isTraceEnabled() { return false; }
            @Override public void trace(String msg) {}
            @Override public void trace(String format, Object arg) {}
            @Override public void trace(String format, Object arg1, Object arg2) {}
            @Override public void trace(String format, Object... arguments) {}
            @Override public void trace(String msg, Throwable t) {}
            @Override public boolean isTraceEnabled(Marker marker) { return false; }
            @Override public void trace(Marker marker, String msg) {}
            @Override public void trace(Marker marker, String format, Object arg) {}
            @Override public void trace(Marker marker, String format, Object arg1, Object arg2) {}
            @Override public void trace(Marker marker, String format, Object... argArray) {}
            @Override public void trace(Marker marker, String msg, Throwable t) {}
            @Override public boolean isDebugEnabled() { return false; }
            @Override public void debug(String msg) {}
            @Override public void debug(String format, Object arg) {}
            @Override public void debug(String format, Object arg1, Object arg2) {}
            @Override public void debug(String format, Object... arguments) {}
            @Override public void debug(String msg, Throwable t) {}
            @Override public boolean isDebugEnabled(Marker marker) { return false; }
            @Override public void debug(Marker marker, String msg) {}
            @Override public void debug(Marker marker, String format, Object arg) {}
            @Override public void debug(Marker marker, String format, Object arg1, Object arg2) {}
            @Override public void debug(Marker marker, String format, Object... argArray) {}
            @Override public void debug(Marker marker, String msg, Throwable t) {}
            @Override public boolean isInfoEnabled() { return false; }
            @Override public void info(String msg) {}
            @Override public void info(String format, Object arg) {}
            @Override public void info(String format, Object arg1, Object arg2) {}
            @Override public void info(String format, Object... arguments) {}
            @Override public void info(String msg, Throwable t) {}
            @Override public boolean isInfoEnabled(Marker marker) { return false; }
            @Override public void info(Marker marker, String msg) {}
            @Override public void info(Marker marker, String format, Object arg) {}
            @Override public void info(Marker marker, String format, Object arg1, Object arg2) {}
            @Override public void info(Marker marker, String format, Object... argArray) {}
            @Override public void info(Marker marker, String msg, Throwable t) {}
            @Override public boolean isWarnEnabled() { return false; }
            @Override public void warn(String msg) {}
            @Override public void warn(String format, Object arg) {}
            @Override public void warn(String format, Object... arguments) {}
            @Override public void warn(String format, Object arg1, Object arg2) {}
            @Override public void warn(String msg, Throwable t) {}
            @Override public boolean isWarnEnabled(Marker marker) { return false; }
            @Override public void warn(Marker marker, String msg) {}
            @Override public void warn(Marker marker, String format, Object arg) {}
            @Override public void warn(Marker marker, String format, Object arg1, Object arg2) {}
            @Override public void warn(Marker marker, String format, Object... argArray) {}
            @Override public void warn(Marker marker, String msg, Throwable t) {}
            @Override public boolean isErrorEnabled() { return false; }
            @Override public void error(String msg) {}
            @Override public void error(String format, Object arg) {}
            @Override public void error(String format, Object arg1, Object arg2) {}
            @Override public void error(String format, Object... arguments) {}
            @Override public void error(String msg, Throwable t) {}
            @Override public boolean isErrorEnabled(Marker marker) { return false; }
            @Override public void error(Marker marker, String msg) {}
            @Override public void error(Marker marker, String format, Object arg) {}
            @Override public void error(Marker marker, String format, Object arg1, Object arg2) {}
            @Override public void error(Marker marker, String format, Object... argArray) {}
            @Override public void error(Marker marker, String msg, Throwable t) {}
        }
    }

    @Nested
    @DisplayName("assertEvent with marker and message")
    class AssertEventWithMarkerAndMessage {

        @Test
        @DisplayName("should pass when marker and message match")
        void shouldPassWhenMarkerAndMessageMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("SECURITY");
            logger.error(marker, "Security violation");
            AssertLogger.assertEvent(logger, 0, marker, "violation");
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, marker, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEvent with level, marker and message")
    class AssertEventWithLevelMarkerAndMessage {

        @Test
        @DisplayName("should pass when all parameters match")
        void shouldPassWhenAllParametersMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("AUDIT");
            logger.debug(marker, "Debug audit message");
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, marker, "audit");
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.INFO, marker, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEvent with level, marker and multiple message parts")
    class AssertEventWithLevelMarkerAndMultipleMessageParts {

        @Test
        @DisplayName("should pass when all message parts are present")
        void shouldPassWhenAllMessagePartsArePresent() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("PERF");
            logger.trace(marker, "Performance measurement: execution took 150ms");
            AssertLogger.assertEvent(logger, 0, Level.TRACE, marker, "Performance", "execution", "150ms");
        }

        @Test
        @DisplayName("should throw when one message part is missing")
        void shouldThrowWhenOneMessagePartIsMissing() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("PERF");
            logger.trace(marker, "Performance measurement");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.TRACE, marker, "Performance", "missing"));
            assertTrue(error.getMessage().contains("should contain expected message part"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.INFO, marker, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEvent with level and marker only")
    class AssertEventWithLevelAndMarkerOnly {

        @Test
        @DisplayName("should pass when level and marker match")
        void shouldPassWhenLevelAndMarkerMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("CONFIG");
            logger.info(marker, "Configuration loaded");
            AssertLogger.assertEvent(logger, 0, Level.INFO, marker);
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEvent(logger, 0, Level.INFO, marker));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with message parts")
    class AssertHasEventWithMessagePart {

        @Test
        @DisplayName("should pass when any event contains expected message part")
        void shouldPassWhenAnyEventContainsExpectedMessagePart() {
            final Logger logger = new MockLogger("test");
            logger.info("First message");
            logger.warn("Second message");
            logger.error("Third message");
            AssertLogger.assertHasEvent(logger, "Second");
        }

        @Test
        @DisplayName("should pass with multiple message parts")
        void shouldPassWithMultipleMessageParts() {
            final Logger logger = new MockLogger("test");
            logger.info("First message");
            logger.warn("Second beautiful message");
            logger.error("Third message");
            AssertLogger.assertHasEvent(logger, "Second", "beautiful");
        }

        @Test
        @DisplayName("should throw when no event contains expected message part")
        void shouldThrowWhenNoEventContainsExpectedMessagePart() {
            final Logger logger = new MockLogger("test");
            logger.info("First message");
            logger.warn("Second message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, "Missing"));
            assertTrue(error.getMessage().contains("should have at least one event containing expected message parts"));
        }

        @Test
        @DisplayName("should throw when no event contains all message parts")
        void shouldThrowWhenNoEventContainsAllMessageParts() {
            final Logger logger = new MockLogger("test");
            logger.info("First message");
            logger.warn("Second beautiful message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, "Second", "Missing"));
            assertTrue(error.getMessage().contains("should have at least one event containing expected message parts"));
        }

        @Test
        @DisplayName("should throw when no events exist")
        void shouldThrowWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, "Any"));
            assertTrue(error.getMessage().contains("should have at least one event containing expected message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with marker")
    class AssertHasEventWithMarker {

        @Test
        @DisplayName("should pass when any event has expected marker")
        void shouldPassWhenAnyEventHasExpectedMarker() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("SECURITY");
            final Marker marker2 = MarkerFactory.getMarker("AUDIT");
            logger.info("Regular message");
            logger.warn(marker1, "Security message");
            logger.error(marker2, "Audit message");
            AssertLogger.assertHasEvent(logger, marker1);
        }

        @Test
        @DisplayName("should throw when no event has expected marker")
        void shouldThrowWhenNoEventHasExpectedMarker() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("SECURITY");
            final Marker marker2 = MarkerFactory.getMarker("AUDIT");
            logger.info("Regular message");
            logger.warn(marker1, "Security message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, marker2));
            assertTrue(error.getMessage().contains("should have at least one event with expected marker"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with level and message")
    class AssertHasEventWithLevelAndMessage {

        @Test
        @DisplayName("should pass when any event has expected level and message")
        void shouldPassWhenAnyEventHasExpectedLevelAndMessage() {
            final Logger logger = new MockLogger("test");
            logger.debug("Debug message");
            logger.info("Info message");
            logger.error("Error message");
            AssertLogger.assertHasEvent(logger, Level.INFO, "Info");
        }

        @Test
        @DisplayName("should throw when no event has expected level and message combination")
        void shouldThrowWhenNoEventHasExpectedLevelAndMessageCombination() {
            final Logger logger = new MockLogger("test");
            logger.debug("Debug message");
            logger.info("Info message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, Level.ERROR, "Info"));
            assertTrue(error.getMessage().contains("should have at least one event with expected level and message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with marker and message")
    class AssertHasEventWithMarkerAndMessage {

        @Test
        @DisplayName("should pass when any event has expected marker and message")
        void shouldPassWhenAnyEventHasExpectedMarkerAndMessage() {
            final Logger logger = new MockLogger("test");
            final Marker securityMarker = MarkerFactory.getMarker("SECURITY");
            final Marker auditMarker = MarkerFactory.getMarker("AUDIT");
            logger.info("Regular message");
            logger.warn(securityMarker, "Security violation detected");
            logger.info(auditMarker, "Audit trail recorded");
            AssertLogger.assertHasEvent(logger, securityMarker, "violation");
        }

        @Test
        @DisplayName("should throw when no event has expected marker and message combination")
        void shouldThrowWhenNoEventHasExpectedMarkerAndMessageCombination() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("SECURITY");
            logger.warn(marker, "Different message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, marker, "Expected"));
            assertTrue(error.getMessage().contains("should have at least one event with expected marker and message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with level, marker and message")
    class AssertHasEventWithLevelMarkerAndMessage {

        @Test
        @DisplayName("should pass when any event has expected level, marker and message")
        void shouldPassWhenAnyEventHasExpectedLevelMarkerAndMessage() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("PERF");
            logger.debug("Debug message");
            logger.warn(marker, "Performance warning");
            logger.error("Error message");
            AssertLogger.assertHasEvent(logger, Level.WARN, marker, "Performance");
        }

        @Test
        @DisplayName("should throw when no event has all three criteria")
        void shouldThrowWhenNoEventHasAllThreeCriteria() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("PERF");
            final Marker marker2 = MarkerFactory.getMarker("SECURITY");
            logger.warn(marker1, "Performance warning");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, Level.WARN, marker2, "Performance"));
            assertTrue(error.getMessage().contains("should have at least one event with expected level, marker and all message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with level, marker and multiple message parts")
    class AssertHasEventWithLevelMarkerAndMultipleMessageParts {

        @Test
        @DisplayName("should pass when any event has all criteria and message parts")
        void shouldPassWhenAnyEventHasAllCriteriaAndMessageParts() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("METRICS");
            logger.debug("Debug message");
            logger.info(marker, "Metrics: CPU usage 85%, memory usage 70%");
            logger.error("Error message");
            AssertLogger.assertHasEvent(logger, Level.INFO, marker, "CPU", "85%", "memory");
        }

        @Test
        @DisplayName("should throw when event has level and marker but missing message part")
        void shouldThrowWhenEventHasLevelAndMarkerButMissingMessagePart() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("METRICS");
            logger.info(marker, "Metrics: CPU usage 85%");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, Level.INFO, marker, "CPU", "memory"));
            assertTrue(error.getMessage().contains("should have at least one event with expected level, marker and all message parts"));
        }
    }

    @Nested
    @DisplayName("assertHasEvent with level and marker only")
    class AssertHasEventWithLevelAndMarkerOnly {

        @Test
        @DisplayName("should pass when any event has expected level and marker")
        void shouldPassWhenAnyEventHasExpectedLevelAndMarker() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("CONFIG");
            logger.debug("Debug message");
            logger.info(marker, "Configuration loaded successfully");
            logger.error("Error message");
            AssertLogger.assertHasEvent(logger, Level.INFO, marker);
        }

        @Test
        @DisplayName("should throw when no event has both level and marker")
        void shouldThrowWhenNoEventHasBothLevelAndMarker() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("CONFIG");
            logger.info(marker, "Config message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEvent(logger, Level.ERROR, marker));
            assertTrue(error.getMessage().contains("should have at least one event with expected level and marker"));
        }
    }

    @Nested
    @DisplayName("assertEventHasThrowable")
    class AssertEventHasThrowable {

        @Test
        @DisplayName("should pass when event has any throwable")
        void shouldPassWhenEventHasAnyThrowable() {
            final Logger logger = new MockLogger("test");
            logger.error("Error", new RuntimeException("any"));
            AssertLogger.assertEventHasThrowable(logger, 0);
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.error("Error");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventHasThrowable(logger, 0));
            assertTrue(error.getMessage().contains("should have a throwable"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventHasThrowable(logger, 0));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEventWithThrowable with class")
    class AssertEventWithThrowableByClass {

        @Test
        @DisplayName("should pass when event has matching throwable class")
        void shouldPassWhenEventHasMatchingThrowableClass() {
            final Logger logger = new MockLogger("test");
            logger.error("Error", new IOException("any"));
            AssertLogger.assertEventWithThrowable(logger, 0, IOException.class);
        }

        @Test
        @DisplayName("should pass when throwable is subclass of expected type")
        void shouldPassWhenThrowableIsSubclassOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
            logger.error("Error occurred", exception);
            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when throwable class does not match")
        void shouldThrowWhenThrowableClassDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.error("Error", new RuntimeException("any"));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, IOException.class));
            assertTrue(error.getMessage().contains("should have expected throwable type"));
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.error("Error message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have a throwable"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, Throwable.class));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertEventWithThrowable with class and message parts")
    class AssertEventWithThrowableClassAndMessage {

        @Test
        @DisplayName("should pass when throwable type and message match")
        void shouldPassWhenThrowableTypeAndMessageMatch() {
            final Logger logger = new MockLogger("test");
            logger.error("Validation failed", new IllegalArgumentException("Invalid parameter: userId"));
            AssertLogger.assertEventWithThrowable(logger, 0, IllegalArgumentException.class, "Invalid parameter");
        }

        @Test
        @DisplayName("should throw when throwable message does not contain expected text")
        void shouldThrowWhenThrowableMessageDoesNotContainExpectedText() {
            final Logger logger = new MockLogger("test");
            logger.error("Error occurred", new RuntimeException("Different message"));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Expected text"));
            assertTrue(error.getMessage().contains("should contain expected throwable message part"));
        }

        @Test
        @DisplayName("should throw when throwable has null message")
        void shouldThrowWhenThrowableHasNullMessage() {
            final Logger logger = new MockLogger("test");
            logger.error("Error occurred", new RuntimeException((String) null));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Any text"));
            assertTrue(error.getMessage().contains("should have throwable message"));
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventWithThrowable(logger, 0, Throwable.class, "test"));
            assertTrue(error.getMessage().contains("should have enough logger events"));
        }
    }

    @Nested
    @DisplayName("assertHasEventWithThrowable any type")
    class AssertHasEventWithThrowableAnyType {

        @Test
        @DisplayName("should pass when any event has any throwable")
        void shouldPassWhenAnyEventHasAnyThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Error with exception", new Exception("Any exception"));
            AssertLogger.assertHasEventWithThrowable(logger);
        }

        @Test
        @DisplayName("should throw when no event has any throwable")
        void shouldThrowWhenNoEventHasAnyThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Error without exception");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger));
            assertTrue(error.getMessage().contains("should have at least one event with a throwable"));
        }

        @Test
        @DisplayName("should throw when no events exist")
        void shouldThrowWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger));
            assertTrue(error.getMessage().contains("should have at least one event with a throwable"));
        }
    }

    @Nested
    @DisplayName("assertHasEventWithThrowable with class")
    class AssertHasEventWithThrowableByClass {

        @Test
        @DisplayName("should pass when any event has expected throwable type")
        void shouldPassWhenAnyEventHasExpectedThrowableType() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Error with exception", new IllegalStateException("State error"));
            logger.warn("Warning message");
            AssertLogger.assertHasEventWithThrowable(logger, IllegalStateException.class);
        }

        @Test
        @DisplayName("should pass when throwable is subclass of expected type")
        void shouldPassWhenThrowableIsSubclassOfExpectedType() {
            final Logger logger = new MockLogger("test");
            logger.error("Error occurred", new IllegalArgumentException("Invalid argument"));
            AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class);
        }

        @Test
        @DisplayName("should throw when no event has expected throwable type")
        void shouldThrowWhenNoEventHasExpectedThrowableType() {
            final Logger logger = new MockLogger("test");
            logger.error("Error with different exception", new RuntimeException("Runtime error"));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger, IllegalStateException.class));
            assertTrue(error.getMessage().contains("should have at least one event with expected throwable type"));
        }

        @Test
        @DisplayName("should throw when no events have throwables")
        void shouldThrowWhenNoEventsHaveThrowables() {
            final Logger logger = new MockLogger("test");
            logger.info("Message without exception");
            logger.error("Error without exception");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have at least one event with expected throwable type"));
        }

        @Test
        @DisplayName("should throw when no events exist")
        void shouldThrowWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class));
            assertTrue(error.getMessage().contains("should have at least one event with expected throwable type"));
        }
    }

    @Nested
    @DisplayName("assertHasEventWithThrowable with class and message parts")
    class AssertHasEventWithThrowableClassAndMessage {

        @Test
        @DisplayName("should pass when any event has expected throwable type and message")
        void shouldPassWhenAnyEventHasExpectedThrowableTypeAndMessage() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Database error", new RuntimeException("Connection failed"));
            logger.warn("Network error", new IllegalStateException("Different error"));
            AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class, "Connection");
        }

        @Test
        @DisplayName("should throw when no event has matching throwable type and message")
        void shouldThrowWhenNoEventHasMatchingThrowableTypeAndMessage() {
            final Logger logger = new MockLogger("test");
            logger.error("Error 1", new RuntimeException("Different message"));
            logger.error("Error 2", new IOException("Connection failed"));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class, "Connection"));
            assertTrue(error.getMessage().contains("should have at least one event with expected throwable type and message parts"));
        }

        @Test
        @DisplayName("should handle throwables with null messages")
        void shouldHandleThrowablesWithNullMessages() {
            final Logger logger = new MockLogger("test");
            logger.error("Error", new RuntimeException((String) null));
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class, "Any message"));
            assertTrue(error.getMessage().contains("should have at least one event with expected throwable type and message parts"));
        }
    }

    @Nested
    @DisplayName("assertEventCount")
    class AssertEventCount {

        @Test
        @DisplayName("should pass when event count matches expected")
        void shouldPassWhenEventCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.warn("Message 2");
            AssertLogger.assertEventCount(logger, 2);
        }

        @Test
        @DisplayName("should throw when event count does not match")
        void shouldThrowWhenEventCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Single message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventCount(logger, 3));
            assertTrue(error.getMessage().contains("should have expected number of events"));
        }
    }

    @Nested
    @DisplayName("assertNoEvents")
    class AssertNoEvents {

        @Test
        @DisplayName("should pass when no events exist")
        void shouldPassWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");
            AssertLogger.assertNoEvents(logger);
        }

        @Test
        @DisplayName("should throw when events exist")
        void shouldThrowWhenEventsExist() {
            final Logger logger = new MockLogger("test");
            logger.debug("Debug message");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertNoEvents(logger));
            assertTrue(error.getMessage().contains("should have expected number of events"));
        }
    }

    @Nested
    @DisplayName("assertEventCountByLevel")
    class AssertEventCountByLevel {

        @Test
        @DisplayName("should pass when level count matches expected")
        void shouldPassWhenLevelCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            logger.info("Info 1");
            logger.warn("Warning");
            logger.info("Info 2");
            AssertLogger.assertEventCountByLevel(logger, Level.INFO, 2);
        }

        @Test
        @DisplayName("should throw when level count does not match")
        void shouldThrowWhenLevelCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Info 1");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventCountByLevel(logger, Level.INFO, 2));
            assertTrue(error.getMessage().contains("should have expected number of events with level INFO"));
        }
    }

    @Nested
    @DisplayName("assertEventCountByMarker")
    class AssertEventCountByMarker {

        @Test
        @DisplayName("should pass when marker count matches expected")
        void shouldPassWhenMarkerCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            logger.info(marker, "Message 1");
            logger.warn(marker, "Message 2");
            AssertLogger.assertEventCountByMarker(logger, marker, 2);
        }

        @Test
        @DisplayName("should throw when marker count does not match")
        void shouldThrowWhenMarkerCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            logger.info(marker, "Message 1");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventCountByMarker(logger, marker, 2));
            assertTrue(error.getMessage().contains("should have expected number of events with marker"));
        }
    }

    @Nested
    @DisplayName("assertEventCountByMessage")
    class AssertEventCountByMessage {

        @Test
        @DisplayName("should pass when message count matches expected")
        void shouldPassWhenMessageCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");
            logger.warn("Hello Universe");
            AssertLogger.assertEventCountByMessage(logger, "Hello", 2);
        }

        @Test
        @DisplayName("should throw when message count does not match")
        void shouldThrowWhenMessageCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventCountByMessage(logger, "Hello", 2));
            assertTrue(error.getMessage().contains("should have expected number of events containing message part"));
        }
    }

    @Nested
    @DisplayName("assertEventSequence with levels")
    class AssertEventSequenceWithLevels {

        @Test
        @DisplayName("should pass when level sequence matches")
        void shouldPassWhenLevelSequenceMatches() {
            final Logger logger = new MockLogger("test");
            logger.info("Info");
            logger.warn("Warn");
            AssertLogger.assertEventSequence(logger, Level.INFO, Level.WARN);
        }

        @Test
        @DisplayName("should throw when level sequence does not match")
        void shouldThrowWhenLevelSequenceDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Info");
            logger.warn("Warn");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventSequence(logger, Level.WARN, Level.INFO));
            assertTrue(error.getMessage().contains("should have expected level at position 0"));
        }
    }

    @Nested
    @DisplayName("assertEventSequence with markers")
    class AssertEventSequenceWithMarkers {

        @Test
        @DisplayName("should pass when marker sequence matches")
        void shouldPassWhenMarkerSequenceMatches() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("M1");
            final Marker marker2 = MarkerFactory.getMarker("M2");
            logger.info(marker1, "M1");
            logger.warn(marker2, "M2");
            AssertLogger.assertEventSequence(logger, marker1, marker2);
        }

        @Test
        @DisplayName("should throw when marker sequence does not match")
        void shouldThrowWhenMarkerSequenceDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker1 = MarkerFactory.getMarker("M1");
            final Marker marker2 = MarkerFactory.getMarker("M2");
            logger.info(marker1, "M1");
            logger.warn(marker2, "M2");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventSequence(logger, marker2, marker1));
            assertTrue(error.getMessage().contains("should have expected marker at position 0"));
        }
    }

    @Nested
    @DisplayName("assertEventSequence with message parts")
    class AssertEventSequenceWithMessageParts {

        @Test
        @DisplayName("should pass when message sequence matches")
        void shouldPassWhenMessageSequenceMatches() {
            final Logger logger = new MockLogger("test");
            logger.info("First step");
            logger.warn("Second step");
            AssertLogger.assertEventSequence(logger, "First", "Second");
        }

        @Test
        @DisplayName("should throw when message sequence does not match")
        void shouldThrowWhenMessageSequenceDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("First step");
            logger.warn("Second step");
            final AssertionError error = assertThrows(AssertionError.class, () -> AssertLogger.assertEventSequence(logger, "Second", "First"));
            assertTrue(error.getMessage().contains("should contain expected message part at position 0"));
        }
    }
}
