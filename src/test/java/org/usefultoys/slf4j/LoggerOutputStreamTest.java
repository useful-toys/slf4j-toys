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

package org.usefultoys.slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ValidateCharset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LoggerOutputStream}.
 * <p>
 * Tests validate that LoggerOutputStream correctly writes data and logs it when closed,
 * with proper handling of various write operations.
 */
@ValidateCharset
class LoggerOutputStreamTest {

    static class TestLoggerOutputStream extends LoggerOutputStream {
        private final StringBuilder loggedData = new StringBuilder();

        @Override
        protected void writeToLogger() {
            loggedData.append(extractString());
        }

        public String getLoggedData() {
            return loggedData.toString();
        }
    }

    @Test
    @DisplayName("should write and extract string without logging on flush")
    void shouldWriteAndExtractStringWithoutLoggingOnFlush() throws IOException {
        // Given: a new TestLoggerOutputStream instance
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        // When: data is written and flushed
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        // Then: data should be extracted but not logged
        assertEquals("Hello, World!", stream.extractString(), "should extract written data");
        assertEquals("", stream.getLoggedData(), "should not log on flush");
    }

    @Test
    @DisplayName("should flush without logging data")
    void shouldFlushWithoutLoggingData() throws IOException {
        // Given: a new TestLoggerOutputStream instance with data
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        // When: flush is called
        stream.flush();
        // Then: data should be extracted but not logged
        assertEquals("Hello, World!", stream.extractString(), "should extract written data after flush");
        assertEquals("", stream.getLoggedData(), "should not log after flush");
    }

    @Test
    @DisplayName("should log data when closed")
    void shouldLogDataWhenClosed() throws IOException {
        // Given: a new TestLoggerOutputStream instance with data
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        // When: stream is closed
        stream.close();
        // Then: data should be extracted and logged
        assertEquals("Hello, World!", stream.extractString(), "should extract written data");
        assertEquals("Hello, World!", stream.getLoggedData(), "should log data on close");
    }

    @Test
    @DisplayName("should write byte array with offset and length correctly")
    void shouldWriteByteArrayWithOffsetAndLengthCorrectly() throws IOException {
        // Given: a new TestLoggerOutputStream instance and byte array
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        final byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        // When: write with offset and length is called
        stream.write(data, 7, 6); // Write "World!"
        stream.close();
        // Then: should write only specified range and log it
        assertEquals("World!", stream.extractString(), "should extract written substring");
        assertEquals("World!", stream.getLoggedData(), "should log substring on close");
    }

    @Test
    @DisplayName("should write full byte array correctly")
    void shouldWriteFullByteArrayCorrectly() throws IOException {
        // Given: a new TestLoggerOutputStream instance
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        // When: full byte array is written
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        stream.close();
        // Then: should write and log all data
        assertEquals("Hello, World!", stream.extractString(), "should extract written data");
        assertEquals("Hello, World!", stream.getLoggedData(), "should log data on close");
    }

    @Test
    @DisplayName("should write single byte correctly")
    void shouldWriteSingleByteCorrectly() throws IOException {
        // Given: a new TestLoggerOutputStream instance
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        // When: single byte is written
        stream.write('H');
        stream.close();
        // Then: should write and log single byte
        assertEquals("H", stream.extractString(), "should extract written byte");
        assertEquals("H", stream.getLoggedData(), "should log byte on close");
    }

    @Test
    @DisplayName("should convert to string correctly")
    void shouldConvertToStringCorrectly() throws IOException {
        // Given: a new TestLoggerOutputStream instance with data
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        // When: toString() is called
        final String result = stream.toString();
        // Then: should return the written data
        assertEquals("Hello, World!", result, "should return written data as string");
    }
}
