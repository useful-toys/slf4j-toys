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
}