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
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLoggerEvent.Level;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockLoggerEvent}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockLoggerEvent Tests")
class MockLoggerEventTest {

    @Test
    @DisplayName("Should create event with basic parameters")
    void shouldCreateEventWithBasicParameters() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.INFO;
        String message = "Test message";
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message);
        
        // Then
        assertEquals(loggerName, event.getLoggerName());
        assertEquals(level, event.getLevel());
        assertNull(event.getMdc());
        assertNull(event.getMarker());
        assertNull(event.getThrowable());
        assertEquals(message, event.getMessage());
        // Arguments array is created as empty array, not null
        assertNotNull(event.getArguments());
        assertEquals(0, event.getArguments().length);
    }

    @Test
    @DisplayName("Should create event with all parameters")
    void shouldCreateEventWithAllParameters() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.ERROR;
        Map<String, String> mdc = new HashMap<>();
        mdc.put("userId", "123");
        mdc.put("sessionId", "abc");
        Marker marker = MarkerFactory.getMarker("TEST_MARKER");
        Throwable throwable = new RuntimeException("Test exception");
        String message = "Error occurred: {}";
        Object[] arguments = {"argument1"};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, mdc, marker, throwable, message, arguments);
        
        // Then
        assertEquals(loggerName, event.getLoggerName());
        assertEquals(level, event.getLevel());
        assertEquals(mdc, event.getMdc());
        assertEquals(marker, event.getMarker());
        assertEquals(throwable, event.getThrowable());
        assertEquals(message, event.getMessage());
        assertArrayEquals(arguments, event.getArguments());
    }

    @Test
    @DisplayName("Should extract throwable from last argument when no explicit throwable")
    void shouldExtractThrowableFromLastArgument() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.WARN;
        String message = "Warning with exception: {} {}";
        RuntimeException exception = new RuntimeException("Test exception");
        Object[] arguments = {"arg1", "arg2", exception};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, arguments);
        
        // Then
        assertEquals(exception, event.getThrowable());
        assertEquals(2, event.getArguments().length);
        assertEquals("arg1", event.getArguments()[0]);
        assertEquals("arg2", event.getArguments()[1]);
    }

    @Test
    @DisplayName("Should not extract throwable when already provided explicitly")
    void shouldNotExtractThrowableWhenExplicitlyProvided() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.ERROR;
        String message = "Error message";
        RuntimeException explicitException = new RuntimeException("Explicit exception");
        RuntimeException argumentException = new RuntimeException("Argument exception");
        Object[] arguments = {"arg1", argumentException};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, explicitException, message, arguments);
        
        // Then
        assertEquals(explicitException, event.getThrowable());
        assertEquals(2, event.getArguments().length);
        assertEquals("arg1", event.getArguments()[0]);
        assertEquals(argumentException, event.getArguments()[1]);
    }

    @Test
    @DisplayName("Should not extract throwable from single argument")
    void shouldNotExtractThrowableFromSingleArgument() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.DEBUG;
        String message = "Debug message: {}";
        RuntimeException exception = new RuntimeException("Test exception");
        Object[] arguments = {exception};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, arguments);
        
        // Then
        assertNull(event.getThrowable());
        assertEquals(1, event.getArguments().length);
        assertEquals(exception, event.getArguments()[0]);
    }

    @Test
    @DisplayName("Should format message correctly with arguments")
    void shouldFormatMessageCorrectlyWithArguments() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.INFO;
        String message = "User {} logged in from IP {}";
        Object[] arguments = {"john.doe", "192.168.1.1"};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, arguments);
        
        // Then
        assertEquals("User john.doe logged in from IP 192.168.1.1", event.getFormattedMessage());
    }

    @Test
    @DisplayName("Should format message without arguments")
    void shouldFormatMessageWithoutArguments() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.TRACE;
        String message = "Simple trace message";
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message);
        
        // Then
        assertEquals("Simple trace message", event.getFormattedMessage());
    }

    @Test
    @DisplayName("Should handle null arguments in formatting")
    void shouldHandleNullArgumentsInFormatting() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.WARN;
        String message = "Value is {}";
        Object[] arguments = {null};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, arguments);
        
        // Then
        assertEquals("Value is null", event.getFormattedMessage());
    }

    @Test
    @DisplayName("Should generate toString with all fields")
    void shouldGenerateToStringWithAllFields() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.ERROR;
        Marker marker = MarkerFactory.getMarker("ERROR_MARKER");
        String message = "Test message";
        Object[] arguments = {"arg1"};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, marker, null, message, arguments);
        String toStringResult = event.toString();
        
        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains(loggerName));
        assertTrue(toStringResult.contains(level.toString()));
        assertTrue(toStringResult.contains(marker.toString()));
        assertTrue(toStringResult.contains(message));
    }

    @Test
    @DisplayName("Should test all log levels")
    void shouldTestAllLogLevels() {
        Level[] levels = Level.values();
        
        assertEquals(5, levels.length);
        assertEquals(Level.ERROR, levels[0]);
        assertEquals(Level.WARN, levels[1]);
        assertEquals(Level.INFO, levels[2]);
        assertEquals(Level.DEBUG, levels[3]);
        assertEquals(Level.TRACE, levels[4]);
    }

    @Test
    @DisplayName("Should handle complex formatting scenarios")
    void shouldHandleComplexFormattingScenarios() {
        // Given
        String loggerName = "complex.logger";
        Level level = Level.INFO;
        String message = "Processing {} items: {} successful, {} failed";
        Object[] arguments = {100, 95, 5};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, arguments);
        
        // Then
        assertEquals("Processing 100 items: 95 successful, 5 failed", event.getFormattedMessage());
    }

    @Test
    @DisplayName("Should preserve original arguments array reference")
    void shouldPreserveOriginalArgumentsArrayReference() {
        // Given
        String loggerName = "test.logger";
        Level level = Level.DEBUG;
        String message = "Test message";
        Object[] originalArguments = {"arg1", "arg2"};
        
        // When
        MockLoggerEvent event = new MockLoggerEvent(loggerName, level, null, null, null, message, originalArguments);
        
        // Then
        assertSame(originalArguments, event.getArguments());
    }
}