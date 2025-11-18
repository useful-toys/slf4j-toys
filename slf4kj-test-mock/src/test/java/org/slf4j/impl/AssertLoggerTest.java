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
import org.slf4j.MarkerFactory;
import org.slf4j.Marker;
import org.slf4j.impl.MockLoggerEvent.Level;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AssertLogger}.
 */
@DisplayName("AssertLogger")
class AssertLoggerTest {

    @Nested
    @DisplayName("assertEvent with message part")
    class AssertEventWithMessagePart {

        @Test
        @DisplayName("should pass when message contains expected part")
        void shouldPassWhenMessageContainsExpectedPart() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");

            AssertLogger.assertEvent(logger, 0, "World");
        }

        @Test
        @DisplayName("should throw when message does not contain expected part")
        void shouldThrowWhenMessageDoesNotContainExpectedPart() {
            final Logger logger = new MockLogger("test");
            logger.info("Hello World");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, "Universe"));
                
            final String expected = "should contain expected message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected, 
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when event index is out of bounds")
        void shouldThrowWhenEventIndexIsOutOfBounds() {
            final Logger logger = new MockLogger("test");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, "test"));
                
            final String expected = "should have enough logger events";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, marker2));
                
            final String expected = "should have expected marker";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, Level.ERROR, "Info"));
                
            final String expected = "should have expected log level";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("logger type conversion")
    class LoggerTypeConversion {

        @Test
        @DisplayName("should throw when logger is not MockLogger instance")
        void shouldThrowWhenLoggerIsNotMockLoggerInstance() {
            final Logger logger = new TestLogger(); // Fake logger implementation

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, "test"));
                
            final String expected = "should be MockLogger instance";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when logger is null")
        void shouldThrowWhenLoggerIsNull() {
            final Logger logger = null;

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, "test"));
                
            final String expected = "should be MockLogger instance";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEvent(logger, 0, Level.TRACE, marker, "Performance", "missing"));
                
            final String expected = "should contain expected message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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
    }

    @Nested
    @DisplayName("static utility class behavior")
    class StaticUtilityClassBehavior {

        @Test
        @DisplayName("should not be instantiable")
        void shouldNotBeInstantiable() {
            final AssertionError error = assertThrows(AssertionError.class, () -> {
                try {
                    AssertLogger.class.getDeclaredConstructor().newInstance();
                } catch (final Exception e) {
                    throw new AssertionError("should prevent instantiation", e);
                }
            });
            
            final String expected = "should prevent instantiation";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertHasEvent with message part")
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
        @DisplayName("should throw when no event contains expected message part")
        void shouldThrowWhenNoEventContainsExpectedMessagePart() {
            final Logger logger = new MockLogger("test");
            logger.info("First message");
            logger.warn("Second message");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, "Missing"));
                
            final String expected = "should have at least one event containing expected message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when no events exist")
        void shouldThrowWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, "Any"));
                
            final String expected = "should have at least one event containing expected message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, marker2));
                
            final String expected = "should have at least one event with expected marker";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, Level.ERROR, "Info"));
                
            final String expected = "should have at least one event with expected level and message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when level matches but message does not")
        void shouldThrowWhenLevelMatchesButMessageDoesNot() {
            final Logger logger = new MockLogger("test");
            logger.info("Different message");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, Level.INFO, "Expected"));
                
            final String expected = "should have at least one event with expected level and message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, marker, "Expected"));
                
            final String expected = "should have at least one event with expected marker and message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, Level.WARN, marker2, "Performance"));
                
            final String expected = "should have at least one event with expected level, marker and message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, Level.INFO, marker, "CPU", "memory"));
                
            final String expected = "should have at least one event with expected level, marker and all message parts";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should pass with empty message parts array")
        void shouldPassWithEmptyMessagePartsArray() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("EMPTY");
            
            logger.warn(marker, "Any message");

            AssertLogger.assertHasEvent(logger, Level.WARN, marker);
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEvent(logger, Level.ERROR, marker));
                
            final String expected = "should have at least one event with expected level and marker";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertHasEvent edge cases")
    class AssertHasEventEdgeCases {

        @Test
        @DisplayName("should work with null markers in events")
        void shouldWorkWithNullMarkersInEvents() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            
            logger.info("Message without marker");
            logger.info(marker, "Message with marker");

            AssertLogger.assertHasEvent(logger, "without marker");
            AssertLogger.assertHasEvent(logger, marker, "with marker");
        }

        @Test
        @DisplayName("should handle empty message in events")
        void shouldHandleEmptyMessageInEvents() {
            final Logger logger = new MockLogger("test");
            
            logger.info("");
            logger.info("Non-empty message");

            AssertLogger.assertHasEvent(logger, "Non-empty");
        }

        @Test
        @DisplayName("should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            final Logger logger = new MockLogger("test");
            
            logger.info("Message with special chars: []{}.?*+^$|\\()");

            AssertLogger.assertHasEvent(logger, "special chars");
            AssertLogger.assertHasEvent(logger, "[]{}.?*+^$|\\()");
        }
    }

    @Nested
    @DisplayName("assertEventWithThrowable with throwable class")
    class AssertEventWithThrowableClass {

        @Test
        @DisplayName("should pass when event has expected throwable type")
        void shouldPassWhenEventHasExpectedThrowableType() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Test exception");
            logger.error("Error occurred", exception);

            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class);
        }

        @Test
        @DisplayName("should pass when throwable is subclass of expected type")
        void shouldPassWhenThrowableIsSubclassOfExpectedType() {
            final Logger logger = new MockLogger("test");
            final IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
            logger.error("Error occurred", exception);

            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class);
            AssertLogger.assertEventWithThrowable(logger, 0, IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.error("Error without exception");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class));
                
            final String expected = "should have a throwable";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when throwable type does not match")
        void shouldThrowWhenThrowableTypeDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Test exception");
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventWithThrowable(logger, 0, IllegalStateException.class));
                
            final String expected = "should have expected throwable type";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertEventWithThrowable with throwable class and message")
    class AssertEventWithThrowableClassAndMessage {

        @Test
        @DisplayName("should pass when throwable type and message match")
        void shouldPassWhenThrowableTypeAndMessageMatch() {
            final Logger logger = new MockLogger("test");
            final IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter: userId");
            logger.error("Validation failed", exception);

            AssertLogger.assertEventWithThrowable(logger, 0, IllegalArgumentException.class, "Invalid parameter");
        }

        @Test
        @DisplayName("should throw when throwable message does not contain expected text")
        void shouldThrowWhenThrowableMessageDoesNotContainExpectedText() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("Different message");
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Expected text"));
                
            final String expected = "should contain expected throwable message part";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when throwable has null message")
        void shouldThrowWhenThrowableHasNullMessage() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException((String) null);
            logger.error("Error occurred", exception);

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class, "Any text"));
                
            final String expected = "should have throwable message";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertEventHasThrowable")
    class AssertEventHasThrowable {

        @Test
        @DisplayName("should pass when event has any throwable")
        void shouldPassWhenEventHasAnyThrowable() {
            final Logger logger = new MockLogger("test");
            final Exception exception = new Exception("Any exception");
            logger.debug("Debug with exception", exception);

            AssertLogger.assertEventHasThrowable(logger, 0);
        }

        @Test
        @DisplayName("should throw when event has no throwable")
        void shouldThrowWhenEventHasNoThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Info without exception");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventHasThrowable(logger, 0));
                
            final String expected = "should have a throwable";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertHasEventWithThrowable with throwable class")
    class AssertHasEventWithThrowableClass {

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
        @DisplayName("should throw when no event has expected throwable type")
        void shouldThrowWhenNoEventHasExpectedThrowableType() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Error with different exception", new RuntimeException("Runtime error"));

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger, IllegalStateException.class));
                
            final String expected = "should have at least one event with expected throwable type";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when no events have throwables")
        void shouldThrowWhenNoEventsHaveThrowables() {
            final Logger logger = new MockLogger("test");
            logger.info("Message without exception");
            logger.error("Error without exception");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class));
                
            final String expected = "should have at least one event with expected throwable type";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("assertHasEventWithThrowable with throwable class and message")
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
            logger.error("Error 2", new IllegalStateException("Connection failed"));

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class, "Connection"));
                
            final String expected = "should have at least one event with expected throwable type and message";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should handle throwables with null messages")
        void shouldHandleThrowablesWithNullMessages() {
            final Logger logger = new MockLogger("test");
            logger.error("Error", new RuntimeException((String) null));

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class, "Any message"));
                
            final String expected = "should have at least one event with expected throwable type and message";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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
            logger.warn("Warning message");
            logger.error("Error with exception", new Exception("Any exception"));

            AssertLogger.assertHasEventWithThrowable(logger);
        }

        @Test
        @DisplayName("should throw when no event has any throwable")
        void shouldThrowWhenNoEventHasAnyThrowable() {
            final Logger logger = new MockLogger("test");
            logger.info("Regular message");
            logger.error("Error without exception");
            logger.warn("Warning message");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger));
                
            final String expected = "should have at least one event with a throwable";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when no events exist")
        void shouldThrowWhenNoEventsExist() {
            final Logger logger = new MockLogger("test");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertHasEventWithThrowable(logger));
                
            final String expected = "should have at least one event with a throwable";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }
    }

    @Nested
    @DisplayName("throwable assertion edge cases")
    class ThrowableAssertionEdgeCases {

        @Test
        @DisplayName("should work with inheritance hierarchies")
        void shouldWorkWithInheritanceHierarchies() {
            final Logger logger = new MockLogger("test");
            final IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
            logger.error("Validation error", exception);

            // Should pass for exact type
            AssertLogger.assertEventWithThrowable(logger, 0, IllegalArgumentException.class);
            // Should pass for parent type
            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class);
            // Should pass for grandparent type
            AssertLogger.assertEventWithThrowable(logger, 0, Exception.class);
            // Should pass for ultimate parent type
            AssertLogger.assertEventWithThrowable(logger, 0, Throwable.class);
        }

        @Test
        @DisplayName("should work with custom exception types")
        void shouldWorkWithCustomExceptionTypes() {
            final Logger logger = new MockLogger("test");
            final CustomException exception = new CustomException("Custom error message");
            logger.error("Custom error", exception);

            AssertLogger.assertEventWithThrowable(logger, 0, CustomException.class);
            AssertLogger.assertEventWithThrowable(logger, 0, CustomException.class, "Custom error");
            AssertLogger.assertHasEventWithThrowable(logger, CustomException.class);
            AssertLogger.assertHasEventWithThrowable(logger, CustomException.class, "Custom");
        }

        @Test
        @DisplayName("should handle empty throwable messages")
        void shouldHandleEmptyThrowableMessages() {
            final Logger logger = new MockLogger("test");
            final RuntimeException exception = new RuntimeException("");
            logger.error("Error with empty message", exception);

            AssertLogger.assertEventWithThrowable(logger, 0, RuntimeException.class);
            AssertLogger.assertEventHasThrowable(logger, 0);
            AssertLogger.assertHasEventWithThrowable(logger, RuntimeException.class);
            AssertLogger.assertHasEventWithThrowable(logger);
        }

        /**
         * Custom exception for testing
         */
        private class CustomException extends Exception {
            public CustomException(final String message) {
                super(message);
            }
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
            logger.error("Message 3");

            AssertLogger.assertEventCount(logger, 3);
        }

        @Test
        @DisplayName("should pass when count is zero")
        void shouldPassWhenCountIsZero() {
            final Logger logger = new MockLogger("test");

            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should throw when event count does not match")
        void shouldThrowWhenEventCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Single message");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventCount(logger, 3));
                
            final String expected = "should have expected number of events";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should throw when expected count is higher than actual")
        void shouldThrowWhenExpectedCountIsHigherThanActual() {
            final Logger logger = new MockLogger("test");
            logger.info("Message 1");
            logger.info("Message 2");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventCount(logger, 5));
                
            final String expected = "should have expected number of events";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertNoEvents(logger));
                
            final String expected = "should have expected number of events";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
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
            logger.error("Error");
            logger.info("Info 3");

            AssertLogger.assertEventCountByLevel(logger, Level.INFO, 3);
            AssertLogger.assertEventCountByLevel(logger, Level.WARN, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.ERROR, 1);
        }

        @Test
        @DisplayName("should pass when level count is zero")
        void shouldPassWhenLevelCountIsZero() {
            final Logger logger = new MockLogger("test");
            logger.info("Info message");
            logger.warn("Warning message");

            AssertLogger.assertEventCountByLevel(logger, Level.ERROR, 0);
            AssertLogger.assertEventCountByLevel(logger, Level.DEBUG, 0);
        }

        @Test
        @DisplayName("should throw when level count does not match")
        void shouldThrowWhenLevelCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Info 1");
            logger.info("Info 2");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventCountByLevel(logger, Level.INFO, 5));
                
            final String expected = "should have expected number of events with level INFO";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should handle all log levels correctly")
        void shouldHandleAllLogLevelsCorrectly() {
            final Logger logger = new MockLogger("test");
            logger.trace("Trace message");
            logger.debug("Debug message");
            logger.info("Info message");
            logger.warn("Warn message");
            logger.error("Error message");

            AssertLogger.assertEventCountByLevel(logger, Level.TRACE, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.DEBUG, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.INFO, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.WARN, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.ERROR, 1);
        }
    }

    @Nested
    @DisplayName("assertEventCountByMarker")
    class AssertEventCountByMarker {

        @Test
        @DisplayName("should pass when marker count matches expected")
        void shouldPassWhenMarkerCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            final Marker securityMarker = MarkerFactory.getMarker("SECURITY");
            final Marker auditMarker = MarkerFactory.getMarker("AUDIT");
            
            logger.info("Regular message");
            logger.warn(securityMarker, "Security warning 1");
            logger.error(securityMarker, "Security error");
            logger.info(auditMarker, "Audit message");
            logger.debug(securityMarker, "Security debug");

            AssertLogger.assertEventCountByMarker(logger, securityMarker, 3);
            AssertLogger.assertEventCountByMarker(logger, auditMarker, 1);
        }

        @Test
        @DisplayName("should pass when marker count is zero")
        void shouldPassWhenMarkerCountIsZero() {
            final Logger logger = new MockLogger("test");
            final Marker securityMarker = MarkerFactory.getMarker("SECURITY");
            final Marker auditMarker = MarkerFactory.getMarker("AUDIT");
            
            logger.info("Regular message");
            logger.warn(securityMarker, "Security message");

            AssertLogger.assertEventCountByMarker(logger, auditMarker, 0);
        }

        @Test
        @DisplayName("should throw when marker count does not match")
        void shouldThrowWhenMarkerCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            
            logger.warn(marker, "Message 1");
            logger.error(marker, "Message 2");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventCountByMarker(logger, marker, 5));
                
            final String expected = "should have expected number of events with marker";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should not count events without markers")
        void shouldNotCountEventsWithoutMarkers() {
            final Logger logger = new MockLogger("test");
            final Marker marker = MarkerFactory.getMarker("TEST");
            
            logger.info("Message without marker");
            logger.warn(marker, "Message with marker");
            logger.error("Another message without marker");

            AssertLogger.assertEventCountByMarker(logger, marker, 1);
        }
    }

    @Nested
    @DisplayName("assertEventCountByMessage")
    class AssertEventCountByMessage {

        @Test
        @DisplayName("should pass when message count matches expected")
        void shouldPassWhenMessageCountMatchesExpected() {
            final Logger logger = new MockLogger("test");
            logger.info("Processing user john");
            logger.warn("User validation failed");
            logger.debug("Processing user alice");
            logger.error("Database error occurred");
            logger.info("Processing user bob");

            AssertLogger.assertEventCountByMessage(logger, "Processing", 3);
            AssertLogger.assertEventCountByMessage(logger, "user", 4);
            AssertLogger.assertEventCountByMessage(logger, "error", 1);
        }

        @Test
        @DisplayName("should pass when message count is zero")
        void shouldPassWhenMessageCountIsZero() {
            final Logger logger = new MockLogger("test");
            logger.info("Information message");
            logger.warn("Warning message");

            AssertLogger.assertEventCountByMessage(logger, "nonexistent", 0);
        }

        @Test
        @DisplayName("should throw when message count does not match")
        void shouldThrowWhenMessageCountDoesNotMatch() {
            final Logger logger = new MockLogger("test");
            logger.info("Test message 1");
            logger.info("Test message 2");

            final AssertionError error = assertThrows(AssertionError.class, () -> 
                AssertLogger.assertEventCountByMessage(logger, "Test", 5));
                
            final String expected = "should have expected number of events containing message part 'Test'";
            final boolean containsExpected = error.getMessage().contains(expected);
            org.junit.jupiter.api.Assertions.assertTrue(containsExpected,
                "should contain expected error message part; expected: " + expected + "; actual: " + error.getMessage());
        }

        @Test
        @DisplayName("should handle case sensitive matching")
        void shouldHandleCaseSensitiveMatching() {
            final Logger logger = new MockLogger("test");
            logger.info("Error occurred");
            logger.warn("error in processing");
            logger.debug("ERROR: critical issue");

            AssertLogger.assertEventCountByMessage(logger, "Error", 1);
            AssertLogger.assertEventCountByMessage(logger, "error", 1);
            AssertLogger.assertEventCountByMessage(logger, "ERROR", 1);
        }

        @Test
        @DisplayName("should handle special characters in message parts")
        void shouldHandleSpecialCharactersInMessageParts() {
            final Logger logger = new MockLogger("test");
            logger.info("Pattern: [a-z]+");
            logger.warn("Regex: \\d{3}");
            logger.error("Special chars: $^*()");

            AssertLogger.assertEventCountByMessage(logger, "[a-z]", 1);
            AssertLogger.assertEventCountByMessage(logger, "\\d{3}", 1);
            AssertLogger.assertEventCountByMessage(logger, "$^*(", 1);
        }

        @Test
        @DisplayName("should handle empty and whitespace message parts")
        void shouldHandleEmptyAndWhitespaceMessageParts() {
            final Logger logger = new MockLogger("test");
            logger.info("Message with spaces");
            logger.warn("Another message");

            AssertLogger.assertEventCountByMessage(logger, " ", 1);
            AssertLogger.assertEventCountByMessage(logger, "", 2); // Empty string matches all
        }
    }

    @Nested
    @DisplayName("event counting edge cases")
    class EventCountingEdgeCases {

        @Test
        @DisplayName("should handle large event counts")
        void shouldHandleLargeEventCounts() {
            final Logger logger = new MockLogger("test");
            final int eventCount = 1000;
            
            for (int i = 0; i < eventCount; i++) {
                logger.info("Message " + i);
            }

            AssertLogger.assertEventCount(logger, eventCount);
            AssertLogger.assertEventCountByLevel(logger, Level.INFO, eventCount);
            AssertLogger.assertEventCountByMessage(logger, "Message", eventCount);
        }

        @Test
        @DisplayName("should work with disabled logger levels")
        void shouldWorkWithDisabledLoggerLevels() {
            final MockLogger logger = new MockLogger("test");
            logger.setDebugEnabled(false);
            logger.setTraceEnabled(false);
            
            logger.trace("Trace message");
            logger.debug("Debug message");
            logger.info("Info message");
            logger.warn("Warn message");

            AssertLogger.assertEventCount(logger, 2);
            AssertLogger.assertEventCountByLevel(logger, Level.TRACE, 0);
            AssertLogger.assertEventCountByLevel(logger, Level.DEBUG, 0);
            AssertLogger.assertEventCountByLevel(logger, Level.INFO, 1);
            AssertLogger.assertEventCountByLevel(logger, Level.WARN, 1);
        }
    }
}