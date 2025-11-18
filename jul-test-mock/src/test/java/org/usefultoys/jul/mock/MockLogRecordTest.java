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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("MockLogRecord Tests")
class MockLogRecordTest {

    @Test
    @DisplayName("should create MockLogRecord from LogRecord")
    void shouldCreateMockLogRecordFromLogRecord() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Test message");
        logRecord.setLoggerName("test.logger");

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals("test.logger", mockRecord.getLoggerName(), "should have correct logger name");
        assertEquals(Level.INFO, mockRecord.getLevel(), "should have correct level");
        assertEquals("Test message", mockRecord.getMessage(), "should have correct message");
    }

    @Test
    @DisplayName("should format message with parameters")
    void shouldFormatMessageWithParameters() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "User {0} logged in from {1}");
        logRecord.setParameters(new Object[]{"john", "192.168.1.1"});

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals("User john logged in from 192.168.1.1", mockRecord.getFormattedMessage(),
                "should format message with parameters");
    }

    @Test
    @DisplayName("should handle message without parameters")
    void shouldHandleMessageWithoutParameters() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Simple message");

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals("Simple message", mockRecord.getFormattedMessage(),
                "should return message as-is when no parameters");
    }

    @Test
    @DisplayName("should capture throwable")
    void shouldCaptureThrowable() {
        final Exception exception = new RuntimeException("Test exception");
        final LogRecord logRecord = new LogRecord(Level.SEVERE, "Error occurred");
        logRecord.setThrown(exception);

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertNotNull(mockRecord.getThrown(), "should have throwable");
        assertEquals("Test exception", mockRecord.getThrown().getMessage(),
                "should have correct exception message");
    }

    @Test
    @DisplayName("should capture source class and method")
    void shouldCaptureSourceClassAndMethod() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Test message");
        logRecord.setSourceClassName("com.example.TestClass");
        logRecord.setSourceMethodName("testMethod");

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals("com.example.TestClass", mockRecord.getSourceClassName(),
                "should have correct source class name");
        assertEquals("testMethod", mockRecord.getSourceMethodName(),
                "should have correct source method name");
    }

    @Test
    @DisplayName("should capture timestamp and sequence number")
    void shouldCaptureTimestampAndSequenceNumber() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "Test message");
        final long expectedMillis = logRecord.getMillis();
        final long expectedSequence = logRecord.getSequenceNumber();

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals(expectedMillis, mockRecord.getMillis(), "should have correct timestamp");
        assertEquals(expectedSequence, mockRecord.getSequenceNumber(), "should have correct sequence number");
    }

    @Test
    @DisplayName("should handle null message")
    void shouldHandleNullMessage() {
        final LogRecord logRecord = new LogRecord(Level.INFO, null);

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertNull(mockRecord.getMessage(), "should have null message");
        assertNull(mockRecord.getFormattedMessage(), "should have null formatted message");
    }

    @Test
    @DisplayName("should handle message format with invalid parameters")
    void shouldHandleMessageFormatWithInvalidParameters() {
        final LogRecord logRecord = new LogRecord(Level.INFO, "User {0} logged in");
        logRecord.setParameters(new Object[]{});

        final MockLogRecord mockRecord = new MockLogRecord(logRecord);

        assertEquals("User {0} logged in", mockRecord.getFormattedMessage(),
                "should return original message when parameters don't match");
    }
}
