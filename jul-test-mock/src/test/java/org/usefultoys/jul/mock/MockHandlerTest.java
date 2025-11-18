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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MockHandler Tests")
class MockHandlerTest {

    private Logger logger;
    private MockHandler handler;

    @BeforeEach
    @DisplayName("should setup logger and handler before each test")
    void setUp() {
        logger = Logger.getLogger("test.logger");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }

    @Test
    @DisplayName("should capture INFO log message")
    void shouldCaptureInfoLogMessage() {
        logger.info("Test info message");

        assertEquals(1, handler.getRecordCount(), "should capture one log record");
        final MockLogRecord record = handler.getRecord(0);
        assertEquals(Level.INFO, record.getLevel(), "should have INFO level");
        assertEquals("Test info message", record.getFormattedMessage(), "should have correct message");
    }

    @Test
    @DisplayName("should capture log message with parameters")
    void shouldCaptureLogMessageWithParameters() {
        logger.log(Level.INFO, "User {0} logged in from {1}", new Object[]{"john", "192.168.1.1"});

        assertEquals(1, handler.getRecordCount(), "should capture one log record");
        final MockLogRecord record = handler.getRecord(0);
        assertEquals("User john logged in from 192.168.1.1", record.getFormattedMessage(),
                "should format message with parameters");
    }

    @Test
    @DisplayName("should capture log message with throwable")
    void shouldCaptureLogMessageWithThrowable() {
        final IOException exception = new IOException("Connection failed");
        logger.log(Level.SEVERE, "Error occurred", exception);

        assertEquals(1, handler.getRecordCount(), "should capture one log record");
        final MockLogRecord record = handler.getRecord(0);
        assertEquals(Level.SEVERE, record.getLevel(), "should have SEVERE level");
        assertEquals("Error occurred", record.getFormattedMessage(), "should have correct message");
        assertNotNull(record.getThrown(), "should have throwable");
        assertTrue(record.getThrown() instanceof IOException, "should be IOException");
        assertEquals("Connection failed", record.getThrown().getMessage(), "should have correct exception message");
    }

    @Test
    @DisplayName("should capture multiple log messages")
    void shouldCaptureMultipleLogMessages() {
        logger.info("First message");
        logger.warning("Second message");
        logger.severe("Third message");

        assertEquals(3, handler.getRecordCount(), "should capture three log records");
        assertEquals("First message", handler.getRecord(0).getFormattedMessage(), "should have first message");
        assertEquals("Second message", handler.getRecord(1).getFormattedMessage(), "should have second message");
        assertEquals("Third message", handler.getRecord(2).getFormattedMessage(), "should have third message");
    }

    @Test
    @DisplayName("should clear records")
    void shouldClearRecords() {
        logger.info("Test message");
        assertEquals(1, handler.getRecordCount(), "should have one record before clearing");

        handler.clearRecords();
        assertEquals(0, handler.getRecordCount(), "should have no records after clearing");
    }

    @Test
    @DisplayName("should filter by log level")
    void shouldFilterByLogLevel() {
        handler.setLevel(Level.WARNING);

        logger.fine("Fine message");
        logger.info("Info message");
        logger.warning("Warning message");
        logger.severe("Severe message");

        assertEquals(2, handler.getRecordCount(), "should capture only WARNING and SEVERE messages");
        assertEquals(Level.WARNING, handler.getRecord(0).getLevel(), "should have WARNING level");
        assertEquals(Level.SEVERE, handler.getRecord(1).getLevel(), "should have SEVERE level");
    }

    @Test
    @DisplayName("should return all records as unmodifiable list")
    void shouldReturnAllRecordsAsUnmodifiableList() {
        logger.info("Message 1");
        logger.info("Message 2");

        assertEquals(2, handler.getLogRecords().size(), "should return all records");
    }

    @Test
    @DisplayName("should convert records to text")
    void shouldConvertRecordsToText() {
        logger.info("First line");
        logger.warning("Second line");

        final String text = handler.toText();
        assertTrue(text.contains("First line"), "should contain first message");
        assertTrue(text.contains("Second line"), "should contain second message");
    }

    @Test
    @DisplayName("should capture logger name")
    void shouldCaptureLoggerName() {
        logger.info("Test message");

        final MockLogRecord record = handler.getRecord(0);
        assertEquals("test.logger", record.getLoggerName(), "should have correct logger name");
    }

    @Test
    @DisplayName("should capture different log levels")
    void shouldCaptureDifferentLogLevels() {
        logger.finest("Finest message");
        logger.finer("Finer message");
        logger.fine("Fine message");
        logger.config("Config message");
        logger.info("Info message");
        logger.warning("Warning message");
        logger.severe("Severe message");

        assertEquals(7, handler.getRecordCount(), "should capture all log levels");
        assertEquals(Level.FINEST, handler.getRecord(0).getLevel(), "should have FINEST level");
        assertEquals(Level.FINER, handler.getRecord(1).getLevel(), "should have FINER level");
        assertEquals(Level.FINE, handler.getRecord(2).getLevel(), "should have FINE level");
        assertEquals(Level.CONFIG, handler.getRecord(3).getLevel(), "should have CONFIG level");
        assertEquals(Level.INFO, handler.getRecord(4).getLevel(), "should have INFO level");
        assertEquals(Level.WARNING, handler.getRecord(5).getLevel(), "should have WARNING level");
        assertEquals(Level.SEVERE, handler.getRecord(6).getLevel(), "should have SEVERE level");
    }

    @Test
    @DisplayName("should assert record using convenience methods")
    void shouldAssertRecordUsingConvenienceMethods() {
        logger.info("Test message with keyword");

        handler.assertRecord(0, "keyword");
        handler.assertRecord(0, Level.INFO, "keyword");
        handler.assertRecord(0, Level.INFO, "Test", "keyword");
    }

    @Test
    @DisplayName("should handle null message")
    void shouldHandleNullMessage() {
        logger.log(Level.INFO, (String) null);

        assertEquals(1, handler.getRecordCount(), "should capture record with null message");
        assertNull(handler.getRecord(0).getMessage(), "should have null message");
    }

    @Test
    @DisplayName("should handle empty message")
    void shouldHandleEmptyMessage() {
        logger.info("");

        assertEquals(1, handler.getRecordCount(), "should capture record with empty message");
        assertEquals("", handler.getRecord(0).getFormattedMessage(), "should have empty message");
    }
}
