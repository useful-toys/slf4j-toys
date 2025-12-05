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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.usefultoys.test.CharsetConsistency;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(CharsetConsistency.class)
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
    void testWriteAndExtractString() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("", stream.getLoggedData());
    }

    @Test
    void testFlushDoesNotLog() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        stream.flush();
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("", stream.getLoggedData());
    }

    @Test
    void testCloseLogsData() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        stream.close();
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("Hello, World!", stream.getLoggedData());
    }

    @Test
    void testWriteWithOffset() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        final byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        stream.write(data, 7, 6); // Write "World!"
        stream.close();
        assertEquals("World!", stream.extractString());
        assertEquals("World!", stream.getLoggedData());
    }

    @Test
    void testWrite() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        stream.close();
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("Hello, World!", stream.getLoggedData());
    }

    @Test
    void testWriteByte() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write('H');
        stream.close();
        assertEquals("H", stream.extractString());
        assertEquals("H", stream.getLoggedData());
    }

    @Test
    void testToString() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello, World!", stream.toString ());
    }
}
