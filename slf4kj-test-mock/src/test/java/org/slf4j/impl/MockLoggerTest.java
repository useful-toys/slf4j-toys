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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockLogger}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockLogger Tests")
class MockLoggerTest {

    private MockLogger logger;
    private final String loggerName = "test.logger";

    @BeforeEach
    void setUp() {
        logger = new MockLogger(loggerName);
    }

    @Test
    @DisplayName("Should return correct logger name")
    void shouldReturnCorrectLoggerName() {
        assertEquals(loggerName, logger.getName());
    }

    @Test
    @DisplayName("Should have all log levels enabled by default")
    void shouldHaveAllLogLevelsEnabledByDefault() {
        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
        
        // Test with marker
        Marker marker = MarkerFactory.getMarker("TEST");
        assertTrue(logger.isTraceEnabled(marker));
        assertTrue(logger.isDebugEnabled(marker));
        assertTrue(logger.isInfoEnabled(marker));
        assertTrue(logger.isWarnEnabled(marker));
        assertTrue(logger.isErrorEnabled(marker));
    }

    @Test
    @DisplayName("Should disable all log levels when setEnabled(false)")
    void shouldDisableAllLogLevelsWhenSetEnabledFalse() {
        // When
        logger.setEnabled(false);
        
        // Then
        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(logger.isInfoEnabled());
        assertFalse(logger.isWarnEnabled());
        assertFalse(logger.isErrorEnabled());
    }

    @Test
    @DisplayName("Should enable all log levels when setEnabled(true)")
    void shouldEnableAllLogLevelsWhenSetEnabledTrue() {
        // Given
        logger.setEnabled(false);
        
        // When
        logger.setEnabled(true);
        
        // Then
        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    @DisplayName("Should log TRACE messages")
    void shouldLogTraceMessages() {
        // When
        logger.trace("Simple trace message");
        logger.trace("Trace with arg: {}", "value");
        logger.trace("Trace with args: {} {}", "arg1", "arg2");
        logger.trace("Trace with varargs: {} {} {}", "a", "b", "c");
        logger.trace("Trace with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        assertEquals(Level.TRACE, logger.getEvent(0).getLevel());
        assertEquals("Simple trace message", logger.getEvent(0).getMessage());
        assertEquals("Trace with arg: value", logger.getEvent(1).getFormattedMessage());
        assertEquals("Trace with args: arg1 arg2", logger.getEvent(2).getFormattedMessage());
        assertEquals("Trace with varargs: a b c", logger.getEvent(3).getFormattedMessage());
        assertNotNull(logger.getEvent(4).getThrowable());
    }

    @Test
    @DisplayName("Should log TRACE messages with marker")
    void shouldLogTraceMessagesWithMarker() {
        // Given
        Marker marker = MarkerFactory.getMarker("TRACE_MARKER");
        
        // When
        logger.trace(marker, "Simple trace with marker");
        logger.trace(marker, "Trace marker with arg: {}", "value");
        logger.trace(marker, "Trace marker with args: {} {}", "arg1", "arg2");
        logger.trace(marker, "Trace marker with varargs: {} {} {}", "a", "b", "c");
        logger.trace(marker, "Trace marker with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertEquals(Level.TRACE, logger.getEvent(i).getLevel());
            assertEquals(marker, logger.getEvent(i).getMarker());
        }
    }

    @Test
    @DisplayName("Should log DEBUG messages")
    void shouldLogDebugMessages() {
        // When
        logger.debug("Simple debug message");
        logger.debug("Debug with arg: {}", "value");
        logger.debug("Debug with args: {} {}", "arg1", "arg2");
        logger.debug("Debug with varargs: {} {} {}", "a", "b", "c");
        logger.debug("Debug with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertEquals(Level.DEBUG, logger.getEvent(i).getLevel());
        }
    }

    @Test
    @DisplayName("Should log INFO messages")
    void shouldLogInfoMessages() {
        // When
        logger.info("Simple info message");
        logger.info("Info with arg: {}", "value");
        logger.info("Info with args: {} {}", "arg1", "arg2");
        logger.info("Info with varargs: {} {} {}", "a", "b", "c");
        logger.info("Info with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertEquals(Level.INFO, logger.getEvent(i).getLevel());
        }
    }

    @Test
    @DisplayName("Should log WARN messages")
    void shouldLogWarnMessages() {
        // When
        logger.warn("Simple warn message");
        logger.warn("Warn with arg: {}", "value");
        logger.warn("Warn with args: {} {}", "arg1", "arg2");
        logger.warn("Warn with varargs: {} {} {}", "a", "b", "c");
        logger.warn("Warn with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertEquals(Level.WARN, logger.getEvent(i).getLevel());
        }
    }

    @Test
    @DisplayName("Should log ERROR messages")
    void shouldLogErrorMessages() {
        // When
        logger.error("Simple error message");
        logger.error("Error with arg: {}", "value");
        logger.error("Error with args: {} {}", "arg1", "arg2");
        logger.error("Error with varargs: {} {} {}", "a", "b", "c");
        logger.error("Error with exception", new RuntimeException("test"));
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertEquals(Level.ERROR, logger.getEvent(i).getLevel());
        }
    }

    @Test
    @DisplayName("Should clear events")
    void shouldClearEvents() {
        // Given
        logger.info("Test message 1");
        logger.info("Test message 2");
        assertEquals(2, logger.getEventCount());
        
        // When
        logger.clearEvents();
        
        // Then
        assertEquals(0, logger.getEventCount());
    }

    @Test
    @DisplayName("Should return unmodifiable list of events")
    void shouldReturnUnmodifiableListOfEvents() {
        // Given
        logger.info("Test message");
        
        // When
        List<MockLoggerEvent> events = logger.getLoggerEvents();
        
        // Then
        assertThrows(UnsupportedOperationException.class, () -> events.add(null));
        assertThrows(UnsupportedOperationException.class, () -> events.clear());
    }

    @Test
    @DisplayName("Should assert event with message part")
    void shouldAssertEventWithMessagePart() {
        // Given
        logger.info("This is a test message");
        
        // When/Then
        assertDoesNotThrow(() -> logger.assertEvent(0, "test message"));
        assertThrows(AssertionFailedError.class, () -> logger.assertEvent(0, "nonexistent"));
    }

    @Test
    @DisplayName("Should assert event with level and message part")
    void shouldAssertEventWithLevelAndMessagePart() {
        // Given
        logger.warn("This is a warning message");
        
        // When/Then
        assertDoesNotThrow(() -> logger.assertEvent(0, Level.WARN, "warning"));
        assertThrows(AssertionFailedError.class, () -> logger.assertEvent(0, Level.ERROR, "warning"));
        assertThrows(AssertionFailedError.class, () -> logger.assertEvent(0, Level.WARN, "nonexistent"));
    }

    @Test
    @DisplayName("Should assert event with marker")
    void shouldAssertEventWithMarker() {
        // Given
        Marker marker = MarkerFactory.getMarker("TEST_MARKER");
        logger.info(marker, "Test message with marker");
        
        // When/Then
        assertDoesNotThrow(() -> logger.assertEvent(0, marker));
        assertDoesNotThrow(() -> logger.assertEvent(0, marker, "Test message"));
        assertDoesNotThrow(() -> logger.assertEvent(0, Level.INFO, marker, "Test message"));
    }

    @Test
    @DisplayName("Should assert event with level and marker")
    void shouldAssertEventWithLevelAndMarker() {
        // Given
        Marker marker = MarkerFactory.getMarker("ERROR_MARKER");
        logger.error(marker, "Error with marker");
        
        // When/Then
        assertDoesNotThrow(() -> logger.assertEvent(0, Level.ERROR, marker));
    }

    @Test
    @DisplayName("Should generate text output from all events")
    void shouldGenerateTextOutputFromAllEvents() {
        // Given
        logger.info("First message");
        logger.warn("Second message");
        logger.error("Third message");
        
        // When
        String text = logger.toText();
        
        // Then
        assertNotNull(text);
        assertTrue(text.contains("First message"));
        assertTrue(text.contains("Second message"));
        assertTrue(text.contains("Third message"));
    }

    @Test
    @DisplayName("Should handle stdout and stderr printing configuration")
    void shouldHandleStdoutAndStderrPrintingConfiguration() {
        // Given
        logger.setStdoutEnabled(true);
        logger.setStderrEnabled(true);
        
        // When/Then
        assertTrue(logger.isStdoutEnabled());
        assertTrue(logger.isStderrEnabled());
        
        logger.setStdoutEnabled(false);
        logger.setStderrEnabled(false);
        
        assertFalse(logger.isStdoutEnabled());
        assertFalse(logger.isStderrEnabled());
    }

    @Test
    @DisplayName("Should handle individual level enabling/disabling")
    void shouldHandleIndividualLevelEnablingDisabling() {
        // When
        logger.setTraceEnabled(false);
        logger.setDebugEnabled(false);
        logger.setInfoEnabled(false);
        logger.setWarnEnabled(false);
        logger.setErrorEnabled(false);
        
        // Then
        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(logger.isInfoEnabled());
        assertFalse(logger.isWarnEnabled());
        assertFalse(logger.isErrorEnabled());
        
        // When
        logger.setTraceEnabled(true);
        logger.setDebugEnabled(true);
        logger.setInfoEnabled(true);
        logger.setWarnEnabled(true);
        logger.setErrorEnabled(true);
        
        // Then
        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    @DisplayName("Should throw exception when accessing invalid event index")
    void shouldThrowExceptionWhenAccessingInvalidEventIndex() {
        // Given
        logger.info("Single message");
        
        // When/Then
        assertThrows(IndexOutOfBoundsException.class, () -> logger.getEvent(1));
        assertThrows(IndexOutOfBoundsException.class, () -> logger.getEvent(-1));
    }

    @Test
    @DisplayName("Should handle empty logger events")
    void shouldHandleEmptyLoggerEvents() {
        // When/Then
        assertEquals(0, logger.getEventCount());
        assertTrue(logger.getLoggerEvents().isEmpty());
        assertEquals("", logger.toText());
    }

    @Test
    @DisplayName("Should handle multiple message parts assertion")
    void shouldHandleMultipleMessagePartsAssertion() {
        // Given
        logger.info("This message contains multiple keywords: test, assertion, validation");
        
        // When/Then
        assertDoesNotThrow(() -> logger.assertEvent(0, Level.INFO, null, "test", "assertion", "validation"));
    }

    @Test
    @DisplayName("Should log DEBUG messages with marker and various argument formats")
    void shouldLogDebugMessagesWithMarkerAndVariousArgumentFormats() {
        // Given
        final Marker debugMarker = MarkerFactory.getMarker("DEBUG_MARKER");
        
        // When
        logger.debug(debugMarker, "Debug marker with single arg: {}", "value");
        logger.debug(debugMarker, "Debug marker with two args: {} {}", "arg1", "arg2");
        logger.debug(debugMarker, "Debug marker with varargs: {} {} {}", "a", "b", "c");
        logger.debug(debugMarker, "Debug marker with exception", new IllegalArgumentException("test exception"));
        
        // Then
        assertEquals(4, logger.getEventCount());
        
        final MockLoggerEvent event0 = logger.getEvent(0);
        assertEquals(Level.DEBUG, event0.getLevel());
        assertEquals(debugMarker, event0.getMarker());
        assertEquals("Debug marker with single arg: value", event0.getFormattedMessage());
        
        final MockLoggerEvent event1 = logger.getEvent(1);
        assertEquals(Level.DEBUG, event1.getLevel());
        assertEquals(debugMarker, event1.getMarker());
        assertEquals("Debug marker with two args: arg1 arg2", event1.getFormattedMessage());
        
        final MockLoggerEvent event2 = logger.getEvent(2);
        assertEquals(Level.DEBUG, event2.getLevel());
        assertEquals(debugMarker, event2.getMarker());
        assertEquals("Debug marker with varargs: a b c", event2.getFormattedMessage());
        
        final MockLoggerEvent event3 = logger.getEvent(3);
        assertEquals(Level.DEBUG, event3.getLevel());
        assertEquals(debugMarker, event3.getMarker());
        assertEquals("Debug marker with exception", event3.getMessage());
        assertNotNull(event3.getThrowable());
        assertEquals(IllegalArgumentException.class, event3.getThrowable().getClass());
        assertEquals("test exception", event3.getThrowable().getMessage());
    }

    @Test
    @DisplayName("Should log INFO messages with marker and various argument formats")
    void shouldLogInfoMessagesWithMarkerAndVariousArgumentFormats() {
        // Given
        final Marker infoMarker = MarkerFactory.getMarker("INFO_MARKER");
        
        // When
        logger.info(infoMarker, "Info marker with varargs: {} {} {}", "x", "y", "z");
        logger.info(infoMarker, "Info marker with exception", new NullPointerException("null value"));
        
        // Then
        assertEquals(2, logger.getEventCount());
        
        final MockLoggerEvent event0 = logger.getEvent(0);
        assertEquals(Level.INFO, event0.getLevel());
        assertEquals(infoMarker, event0.getMarker());
        assertEquals("Info marker with varargs: x y z", event0.getFormattedMessage());
        
        final MockLoggerEvent event1 = logger.getEvent(1);
        assertEquals(Level.INFO, event1.getLevel());
        assertEquals(infoMarker, event1.getMarker());
        assertEquals("Info marker with exception", event1.getMessage());
        assertNotNull(event1.getThrowable());
        assertEquals(NullPointerException.class, event1.getThrowable().getClass());
        assertEquals("null value", event1.getThrowable().getMessage());
    }

    @Test
    @DisplayName("Should log WARN messages with marker and various argument formats")
    void shouldLogWarnMessagesWithMarkerAndVariousArgumentFormats() {
        // Given
        final Marker warnMarker = MarkerFactory.getMarker("WARN_MARKER");
        
        // When
        logger.warn(warnMarker, "Warn marker with single arg: {}", "warning");
        logger.warn(warnMarker, "Warn marker with two args: {} {}", "first", "second");
        logger.warn(warnMarker, "Warn marker with varargs: {} {} {}", "1", "2", "3");
        logger.warn(warnMarker, "Warn marker with exception", new RuntimeException("runtime error"));
        
        // Then
        assertEquals(4, logger.getEventCount());
        
        final MockLoggerEvent event0 = logger.getEvent(0);
        assertEquals(Level.WARN, event0.getLevel());
        assertEquals(warnMarker, event0.getMarker());
        assertEquals("Warn marker with single arg: warning", event0.getFormattedMessage());
        
        final MockLoggerEvent event1 = logger.getEvent(1);
        assertEquals(Level.WARN, event1.getLevel());
        assertEquals(warnMarker, event1.getMarker());
        assertEquals("Warn marker with two args: first second", event1.getFormattedMessage());
        
        final MockLoggerEvent event2 = logger.getEvent(2);
        assertEquals(Level.WARN, event2.getLevel());
        assertEquals(warnMarker, event2.getMarker());
        assertEquals("Warn marker with varargs: 1 2 3", event2.getFormattedMessage());
        
        final MockLoggerEvent event3 = logger.getEvent(3);
        assertEquals(Level.WARN, event3.getLevel());
        assertEquals(warnMarker, event3.getMarker());
        assertEquals("Warn marker with exception", event3.getMessage());
        assertNotNull(event3.getThrowable());
        assertEquals(RuntimeException.class, event3.getThrowable().getClass());
        assertEquals("runtime error", event3.getThrowable().getMessage());
    }

    @Test
    @DisplayName("Should log ERROR messages with marker and various argument formats")
    void shouldLogErrorMessagesWithMarkerAndVariousArgumentFormats() {
        // Given
        final Marker errorMarker = MarkerFactory.getMarker("ERROR_MARKER");
        
        // When
        logger.error(errorMarker, "Error marker with single arg: {}", "critical");
        logger.error(errorMarker, "Error marker with two args: {} {}", "error", "occurred");
        logger.error(errorMarker, "Error marker with varargs: {} {} {}", "fatal", "system", "failure");
        logger.error(errorMarker, "Error marker with exception", new Exception("system error"));
        
        // Then
        assertEquals(4, logger.getEventCount());
        
        final MockLoggerEvent event0 = logger.getEvent(0);
        assertEquals(Level.ERROR, event0.getLevel());
        assertEquals(errorMarker, event0.getMarker());
        assertEquals("Error marker with single arg: critical", event0.getFormattedMessage());
        
        final MockLoggerEvent event1 = logger.getEvent(1);
        assertEquals(Level.ERROR, event1.getLevel());
        assertEquals(errorMarker, event1.getMarker());
        assertEquals("Error marker with two args: error occurred", event1.getFormattedMessage());
        
        final MockLoggerEvent event2 = logger.getEvent(2);
        assertEquals(Level.ERROR, event2.getLevel());
        assertEquals(errorMarker, event2.getMarker());
        assertEquals("Error marker with varargs: fatal system failure", event2.getFormattedMessage());
        
        final MockLoggerEvent event3 = logger.getEvent(3);
        assertEquals(Level.ERROR, event3.getLevel());
        assertEquals(errorMarker, event3.getMarker());
        assertEquals("Error marker with exception", event3.getMessage());
        assertNotNull(event3.getThrowable());
        assertEquals(Exception.class, event3.getThrowable().getClass());
        assertEquals("system error", event3.getThrowable().getMessage());
    }

    @Test
    @DisplayName("Should handle all marker-based logging methods with null markers")
    void shouldHandleAllMarkerBasedLoggingMethodsWithNullMarkers() {
        // When - Testing with null markers to ensure robustness
        logger.trace((Marker) null, "Trace with null marker");
        logger.debug((Marker) null, "Debug with null marker");
        logger.info((Marker) null, "Info with null marker");
        logger.warn((Marker) null, "Warn with null marker");
        logger.error((Marker) null, "Error with null marker");
        
        // Then
        assertEquals(5, logger.getEventCount());
        for (int i = 0; i < 5; i++) {
            assertNull(logger.getEvent(i).getMarker());
        }
    }

    @Test
    @DisplayName("Should handle complex message formatting with markers")
    void shouldHandleComplexMessageFormattingWithMarkers() {
        // Given
        final Marker complexMarker = MarkerFactory.getMarker("COMPLEX");
        
        // When - Testing edge cases in message formatting
        logger.info(complexMarker, "Message with {} and {}", null, "non-null");
        logger.warn(complexMarker, "Message with empty args: {} {}", "", "");
        logger.error(complexMarker, "Message with special chars: {} {}", "special!@#", "chars$%^");
        
        // Then
        assertEquals(3, logger.getEventCount());
        
        assertEquals("Message with null and non-null", logger.getEvent(0).getFormattedMessage());
        assertEquals("Message with empty args:  ", logger.getEvent(1).getFormattedMessage());
        assertEquals("Message with special chars: special!@# chars$%^", logger.getEvent(2).getFormattedMessage());
        
        // All should have the same marker
        for (int i = 0; i < 3; i++) {
            assertEquals(complexMarker, logger.getEvent(i).getMarker());
        }
    }
}