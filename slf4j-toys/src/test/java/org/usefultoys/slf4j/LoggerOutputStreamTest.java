package org.usefultoys.slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class LoggerOutputStreamTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    static class TestLoggerOutputStream extends LoggerOutputStream {
        private StringBuilder loggedData = new StringBuilder();

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
        stream.write("Hello, World!".getBytes());
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("", stream.getLoggedData());
    }

    @Test
    void testFlushDoesNotLog() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes());
        stream.flush();
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("", stream.getLoggedData());
    }

    @Test
    void testCloseLogsData() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes());
        stream.close();
        assertEquals("Hello, World!", stream.extractString());
        assertEquals("Hello, World!", stream.getLoggedData());
    }

    @Test
    void testWriteWithOffset() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        final byte[] data = "Hello, World!".getBytes();
        stream.write(data, 7, 6); // Write "World!"
        stream.close();
        assertEquals("World!", stream.extractString());
        assertEquals("World!", stream.getLoggedData());
    }

    @Test
    void testWrite() throws IOException {
        final TestLoggerOutputStream stream = new TestLoggerOutputStream();
        stream.write("Hello, World!".getBytes());
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
        stream.write("Hello, World!".getBytes());
        assertEquals("Hello, World!", stream.toString ());
    }
}
