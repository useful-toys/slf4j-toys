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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ValidateCharset;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link LoggerFactory}.
 * <p>
 * Tests validate that LoggerFactory correctly creates loggers and print/output streams, with proper behavior for
 * enabled and disabled loggers.
 */
@ValidateCharset
class LoggerFactoryTest {

    @Nested
    class GetLogger {
        @Test
        @DisplayName("should get logger by name")
        void shouldGetLoggerByName() {
            // Given: a logger name
            // When: getLogger is called with the name
            final Logger logger = LoggerFactory.getLogger("testLogger");
            // Then: should return a non-null MockLogger with the correct name
            assertNotNull(logger, "should return non-null logger");
            assertInstanceOf(MockLogger.class, logger, "should return MockLogger instance");
            assertEquals("testLogger", logger.getName(), "should have correct logger name");
        }

        @Test
        @DisplayName("should get logger by class")
        void shouldGetLoggerByClass() {
            // Given: a test class
            // When: getLogger is called with the class
            final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
            // Then: should return a non-null MockLogger with class name
            assertNotNull(logger, "should return non-null logger");
            assertInstanceOf(MockLogger.class, logger, "should return MockLogger instance");
            assertEquals(LoggerFactoryTest.class.getName(), logger.getName(), "should have class name");
        }

        @Test
        @DisplayName("should get logger by class and feature name")
        void shouldGetLoggerByClassAndFeatureName() {
            // Given: a test class and feature name
            // When: getLogger is called with class and feature
            final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class, "feature");
            // Then: should return logger with combined name
            assertNotNull(logger, "should return non-null logger");
            assertInstanceOf(MockLogger.class, logger, "should return MockLogger instance");
            assertEquals(LoggerFactoryTest.class.getName() + ".feature", logger.getName(), "should have combined name");
        }

        @Test
        @DisplayName("should get logger by parent logger and child name")
        void shouldGetLoggerByParentLoggerAndChildName() {
            // Given: a parent logger and child name
            final Logger parentLogger = LoggerFactory.getLogger("parentLogger");
            // When: getLogger is called with parent and child name
            final Logger childLogger = LoggerFactory.getLogger(parentLogger, "child");
            // Then: should return child logger with hierarchical name
            assertNotNull(childLogger, "should return non-null logger");
            assertInstanceOf(MockLogger.class, childLogger, "should return MockLogger instance");
            assertEquals("parentLogger.child", childLogger.getName(), "should have hierarchical name");
        }
    }

    @Nested
    @WithMockLogger
    class GetEnabledPrintStream {
        @Test
        @DisplayName("should get trace print stream when logger enabled")
        void shouldGetTracePrintStreamWhenLoggerEnabled(@Slf4jMock(value="traceLogger", traceEnabled=true) final MockLogger mockLogger) {
            // Given: a logger with trace enabled
            // When: getTracePrintStream is called
            final PrintStream traceStream = LoggerFactory.getTracePrintStream(mockLogger);
            traceStream.print("Trace message");
            traceStream.close();
            // Then: should return print stream that logs trace messages
            assertNotNull(traceStream, "should return non-null print stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel(), "should be trace level");
        }

        @Test
        @DisplayName("should get debug print stream when logger enabled")
        void shouldGetDebugPrintStreamWhenLoggerEnabled(@Slf4jMock(value = "debugLogger", debugEnabled = true) final MockLogger mockLogger) {
            // Given: a logger with debug enabled
            // When: getDebugPrintStream is called
            final PrintStream debugStream = LoggerFactory.getDebugPrintStream(mockLogger);
            debugStream.print("Debug message");
            debugStream.close();
            // Then: should return print stream that logs debug messages
            assertNotNull(debugStream, "should return non-null print stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel(), "should be debug level");
        }

        @Test
        @DisplayName("should get info print stream when logger enabled")
        void shouldGetInfoPrintStreamWhenLoggerEnabled(@Slf4jMock(value = "infoLogger", infoEnabled = true) final MockLogger mockLogger) {
            // Given: a logger with info enabled
            // When: getInfoPrintStream is called
            final PrintStream infoStream = LoggerFactory.getInfoPrintStream(mockLogger);
            infoStream.print("Info message");
            infoStream.close();
            // Then: should return print stream that logs info messages
            assertNotNull(infoStream, "should return non-null print stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel(), "should be info level");
        }

        @Test
        @DisplayName("should get warn print stream when logger enabled")
        void shouldGetWarnPrintStreamWhenLoggerEnabled(@Slf4jMock(value = "warnLogger", warnEnabled = true) final MockLogger mockLogger) {
            // Given: a logger with warn enabled
            // When: getWarnPrintStream is called
            final PrintStream warnStream = LoggerFactory.getWarnPrintStream(mockLogger);
            warnStream.print("Warn message");
            warnStream.close();
            // Then: should return print stream that logs warn messages
            assertNotNull(warnStream, "should return non-null print stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel(), "should be warn level");
        }

        @Test
        @DisplayName("should get error print stream when logger enabled")
        void shouldGetErrorPrintStreamWhenLoggerEnabled(@Slf4jMock(value = "errorLogger", errorEnabled = true) final MockLogger mockLogger) {
            // Given: a logger with error enabled
            // When: getErrorPrintStream is called
            final PrintStream errorStream = LoggerFactory.getErrorPrintStream(mockLogger);
            errorStream.print("Error message");
            errorStream.close();
            // Then: should return print stream that logs error messages
            assertNotNull(errorStream, "should return non-null print stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel(), "should be error level");
        }
    }

    @Nested
    @WithMockLogger
    class GetDisabledPrintStream {
        @Test
        @DisplayName("should return NullPrintStream when trace logger disabled")
        void shouldReturnNullPrintStreamWhenTraceLoggerDisabled(@Slf4jMock(value = "traceLogger", enabled = false) final MockLogger mockLogger) {
            // Given: a disabled logger
            // When: getTracePrintStream is called
            final PrintStream traceStream = LoggerFactory.getTracePrintStream(mockLogger);
            traceStream.println("Trace message");
            traceStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(traceStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, traceStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when debug logger disabled")
        void shouldReturnNullPrintStreamWhenDebugLoggerDisabled(@Slf4jMock(value = "debugLogger", enabled = false) final MockLogger mockLogger) {
            // Given: a disabled logger
            // When: getDebugPrintStream is called
            final PrintStream debugStream = LoggerFactory.getDebugPrintStream(mockLogger);
            debugStream.println("Debug message");
            debugStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(debugStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, debugStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when info logger disabled")
        void shouldReturnNullPrintStreamWhenInfoLoggerDisabled(@Slf4jMock(value = "infoLogger", enabled = false) final MockLogger mockLogger) {
            // Given: a disabled logger
            // When: getInfoPrintStream is called
            final PrintStream infoStream = LoggerFactory.getInfoPrintStream(mockLogger);
            infoStream.println("Info message");
            infoStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(infoStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, infoStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when warn logger disabled")
        void shouldReturnNullPrintStreamWhenWarnLoggerDisabled(@Slf4jMock(value = "warnLogger", enabled = false) final MockLogger mockLogger) {
            // Given: a disabled logger
            // When: getWarnPrintStream is called
            final PrintStream warnStream = LoggerFactory.getWarnPrintStream(mockLogger);
            warnStream.println("Warn message");
            warnStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(warnStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, warnStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when error logger disabled")
        void shouldReturnNullPrintStreamWhenErrorLoggerDisabled(@Slf4jMock(value = "errorLogger", enabled = false) final MockLogger mockLogger) {
            // Given: a disabled logger
            // When: getErrorPrintStream is called
            final PrintStream errorStream = LoggerFactory.getErrorPrintStream(mockLogger);
            errorStream.println("Error message");
            errorStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(errorStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, errorStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }
    }

    @Test
    @DisplayName("should handle NullPrintStream operations without throwing exceptions")
    void shouldHandleNullPrintStreamOperationsWithoutThrowingExceptions() {
        // Given: a NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: various operations are called
        assertDoesNotThrow(() -> {
            nullPrintStream.print("test");
            nullPrintStream.println("test");
            nullPrintStream.write(1);
            nullPrintStream.write("test".getBytes(StandardCharsets.UTF_8));
            nullPrintStream.write("test".getBytes(StandardCharsets.UTF_8), 0, 2);
            nullPrintStream.flush();
            nullPrintStream.close();
        }, "should not throw exceptions on NullPrintStream operations");
    }

    @Nested
    @WithMockLogger
    class GetEnabledOutputStream {
        @Test
        @DisplayName("should get trace output stream when logger enabled")
        void shouldGetTraceOutputStreamWhenLoggerEnabled(@Slf4jMock(value = "traceLogger", traceEnabled = true) final MockLogger mockLogger) throws Exception {
            // Given: a logger with trace enabled
            // When: getTraceOutputStream is called
            final OutputStream traceStream = LoggerFactory.getTraceOutputStream(mockLogger);
            traceStream.write("Trace message".getBytes(StandardCharsets.UTF_8));
            traceStream.close();
            // Then: should return output stream that logs trace messages
            assertNotNull(traceStream, "should return non-null output stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel(), "should be trace level");
        }

        @Test
        @DisplayName("should get debug output stream when logger enabled")
        void shouldGetDebugOutputStreamWhenLoggerEnabled(@Slf4jMock(value = "debugLogger", debugEnabled = true) final MockLogger mockLogger) throws Exception {
            // Given: a logger with debug enabled
            // When: getDebugOutputStream is called
            final OutputStream debugStream = LoggerFactory.getDebugOutputStream(mockLogger);
            debugStream.write("Debug message".getBytes(StandardCharsets.UTF_8));
            debugStream.close();
            // Then: should return output stream that logs debug messages
            assertNotNull(debugStream, "should return non-null output stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel(), "should be debug level");
        }

        @Test
        @DisplayName("should get info output stream when logger enabled")
        void shouldGetInfoOutputStreamWhenLoggerEnabled(@Slf4jMock(value = "infoLogger", infoEnabled = true) final MockLogger mockLogger) throws Exception {
            // Given: a logger with info enabled
            // When: getInfoOutputStream is called
            final OutputStream infoStream = LoggerFactory.getInfoOutputStream(mockLogger);
            infoStream.write("Info message".getBytes(StandardCharsets.UTF_8));
            infoStream.close();
            // Then: should return output stream that logs info messages
            assertNotNull(infoStream, "should return non-null output stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel(), "should be info level");
        }

        @Test
        @DisplayName("should get warn output stream when logger enabled")
        void shouldGetWarnOutputStreamWhenLoggerEnabled(@Slf4jMock(value = "warnLogger", warnEnabled = true) final MockLogger mockLogger) throws Exception {
            // Given: a logger with warn enabled
            // When: getWarnOutputStream is called
            final OutputStream warnStream = LoggerFactory.getWarnOutputStream(mockLogger);
            warnStream.write("Warn message".getBytes(StandardCharsets.UTF_8));
            warnStream.close();
            // Then: should return output stream that logs warn messages
            assertNotNull(warnStream, "should return non-null output stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel(), "should be warn level");
        }

        @Test
        @DisplayName("should get error output stream when logger enabled")
        void shouldGetErrorOutputStreamWhenLoggerEnabled(@Slf4jMock(value = "errorLogger", errorEnabled = true) final MockLogger mockLogger) throws Exception {
            // Given: a logger with error enabled
            // When: getErrorOutputStream is called
            final OutputStream errorStream = LoggerFactory.getErrorOutputStream(mockLogger);
            errorStream.write("Error message".getBytes(StandardCharsets.UTF_8));
            errorStream.close();
            // Then: should return output stream that logs error messages
            assertNotNull(errorStream, "should return non-null output stream");
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel(), "should be error level");
        }
    }

    @Nested
    @WithMockLogger
    class GetDisabledOutputStream {
        @Test
        @DisplayName("should return NullOutputStream when trace logger disabled")
        void shouldReturnNullOutputStreamWhenTraceLoggerDisabled(@Slf4jMock(value = "traceLogger", enabled = false) final MockLogger mockLogger) throws Exception {
            // Given: a disabled logger
            // When: getTraceOutputStream is called
            final OutputStream traceStream = LoggerFactory.getTraceOutputStream(mockLogger);
            traceStream.write("Trace message".getBytes(StandardCharsets.UTF_8));
            traceStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(traceStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, traceStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when debug logger disabled")
        void shouldReturnNullOutputStreamWhenDebugLoggerDisabled(@Slf4jMock(value = "debugLogger", enabled = false) final MockLogger mockLogger) throws Exception {
            // Given: a disabled logger
            // When: getDebugOutputStream is called
            final OutputStream debugStream = LoggerFactory.getDebugOutputStream(mockLogger);
            debugStream.write("Debug message".getBytes(StandardCharsets.UTF_8));
            debugStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(debugStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, debugStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when info logger disabled")
        void shouldReturnNullOutputStreamWhenInfoLoggerDisabled(@Slf4jMock(value = "infoLogger", enabled = false) final MockLogger mockLogger) throws Exception {
            // Given: a disabled logger
            // When: getInfoOutputStream is called
            final OutputStream infoStream = LoggerFactory.getInfoOutputStream(mockLogger);
            infoStream.write("Info message".getBytes(StandardCharsets.UTF_8));
            infoStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(infoStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, infoStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when warn logger disabled")
        void shouldReturnNullOutputStreamWhenWarnLoggerDisabled(@Slf4jMock(value = "warnLogger", enabled = false) final MockLogger mockLogger) throws Exception {
            // Given: a disabled logger
            // When: getWarnOutputStream is called
            final OutputStream warnStream = LoggerFactory.getWarnOutputStream(mockLogger);
            warnStream.write("Warn message".getBytes(StandardCharsets.UTF_8));
            warnStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(warnStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, warnStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when error logger disabled")
        void shouldReturnNullOutputStreamWhenErrorLoggerDisabled(@Slf4jMock(value = "errorLogger", enabled = false) final MockLogger mockLogger) throws Exception {
            // Given: a disabled logger
            // When: getErrorOutputStream is called
            final OutputStream errorStream = LoggerFactory.getErrorOutputStream(mockLogger);
            errorStream.write("Error message".getBytes(StandardCharsets.UTF_8));
            errorStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(errorStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, errorStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }
    }

    @Test
    @DisplayName("should handle NullOutputStream operations without throwing exceptions")
    void shouldHandleNullOutputStreamOperationsWithoutThrowingExceptions() {
        // Given: a NullOutputStream instance
        final NullOutputStream nullOutputStream = new NullOutputStream();
        // When: various operations are called
        assertDoesNotThrow(() -> {
            nullOutputStream.write(1);
            nullOutputStream.write("test".getBytes(StandardCharsets.UTF_8));
            nullOutputStream.write("test".getBytes(StandardCharsets.UTF_8), 0, 2);
            nullOutputStream.flush();
            nullOutputStream.close();
        }, "should not throw exceptions on NullOutputStream operations");
    }
}
