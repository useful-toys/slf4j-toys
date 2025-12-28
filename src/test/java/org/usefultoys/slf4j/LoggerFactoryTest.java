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
    class GetEnabledPrintStream {
        @Test
        @DisplayName("should get trace print stream when logger enabled")
        void shouldGetTracePrintStreamWhenLoggerEnabled() {
            // Given: a logger with trace enabled
            final Logger logger = LoggerFactory.getLogger("traceLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setTraceEnabled(true);
            mockLogger.clearEvents();
            // When: getTracePrintStream is called
            final PrintStream traceStream = LoggerFactory.getTracePrintStream(logger);
            // Then: should return print stream that logs trace messages
            assertNotNull(traceStream, "should return non-null print stream");
            traceStream.print("Trace message");
            traceStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel(), "should be trace level");
        }

        @Test
        @DisplayName("should get debug print stream when logger enabled")
        void shouldGetDebugPrintStreamWhenLoggerEnabled() {
            // Given: a logger with debug enabled
            final Logger logger = LoggerFactory.getLogger("debugLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setDebugEnabled(true);
            mockLogger.clearEvents();
            // When: getDebugPrintStream is called
            final PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger);
            // Then: should return print stream that logs debug messages
            assertNotNull(debugStream, "should return non-null print stream");
            debugStream.print("Debug message");
            debugStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel(), "should be debug level");
        }

        @Test
        @DisplayName("should get info print stream when logger enabled")
        void shouldGetInfoPrintStreamWhenLoggerEnabled() {
            // Given: a logger with info enabled
            final Logger logger = LoggerFactory.getLogger("infoLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setInfoEnabled(true);
            mockLogger.clearEvents();
            // When: getInfoPrintStream is called
            final PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger);
            // Then: should return print stream that logs info messages
            assertNotNull(infoStream, "should return non-null print stream");
            infoStream.print("Info message");
            infoStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel(), "should be info level");
        }

        @Test
        @DisplayName("should get warn print stream when logger enabled")
        void shouldGetWarnPrintStreamWhenLoggerEnabled() {
            // Given: a logger with warn enabled
            final Logger logger = LoggerFactory.getLogger("warnLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setWarnEnabled(true);
            mockLogger.clearEvents();
            // When: getWarnPrintStream is called
            final PrintStream warnStream = LoggerFactory.getWarnPrintStream(logger);
            // Then: should return print stream that logs warn messages
            assertNotNull(warnStream, "should return non-null print stream");
            warnStream.print("Warn message");
            warnStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel(), "should be warn level");
        }

        @Test
        @DisplayName("should get error print stream when logger enabled")
        void shouldGetErrorPrintStreamWhenLoggerEnabled() {
            // Given: a logger with error enabled
            final Logger logger = LoggerFactory.getLogger("errorLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setErrorEnabled(true);
            mockLogger.clearEvents();
            // When: getErrorPrintStream is called
            final PrintStream errorStream = LoggerFactory.getErrorPrintStream(logger);
            // Then: should return print stream that logs error messages
            assertNotNull(errorStream, "should return non-null print stream");
            errorStream.print("Error message");
            errorStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel(), "should be error level");
        }
    }

    @Nested
    class GetDisabledPrintStream {
        @Test
        @DisplayName("should return NullPrintStream when trace logger disabled")
        void shouldReturnNullPrintStreamWhenTraceLoggerDisabled() {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("traceLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getTracePrintStream is called
            final PrintStream traceStream = LoggerFactory.getTracePrintStream(logger);
            traceStream.println("Trace message");
            traceStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(traceStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, traceStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when debug logger disabled")
        void shouldReturnNullPrintStreamWhenDebugLoggerDisabled() {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("debugLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getDebugPrintStream is called
            final PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger);
            debugStream.println("Debug message");
            debugStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(debugStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, debugStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when info logger disabled")
        void shouldReturnNullPrintStreamWhenInfoLoggerDisabled() {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("infoLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getInfoPrintStream is called
            final PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger);
            infoStream.println("Info message");
            infoStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(infoStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, infoStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when warn logger disabled")
        void shouldReturnNullPrintStreamWhenWarnLoggerDisabled() {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("warnLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getWarnPrintStream is called
            final PrintStream warnStream = LoggerFactory.getWarnPrintStream(logger);
            warnStream.println("Warn message");
            warnStream.close();
            // Then: should return NullPrintStream and not log
            assertNotNull(warnStream, "should return non-null print stream");
            assertInstanceOf(NullPrintStream.class, warnStream, "should return NullPrintStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullPrintStream when error logger disabled")
        void shouldReturnNullPrintStreamWhenErrorLoggerDisabled() {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("errorLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getErrorPrintStream is called
            final PrintStream errorStream = LoggerFactory.getErrorPrintStream(logger);
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
    class GetEnabledOutputStream {
        @Test
        @DisplayName("should get trace output stream when logger enabled")
        void shouldGetTraceOutputStreamWhenLoggerEnabled() throws Exception {
            // Given: a logger with trace enabled
            final Logger logger = LoggerFactory.getLogger("traceLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setTraceEnabled(true);
            mockLogger.clearEvents();
            // When: getTraceOutputStream is called
            final OutputStream traceStream = LoggerFactory.getTraceOutputStream(logger);
            // Then: should return output stream that logs trace messages
            assertNotNull(traceStream, "should return non-null output stream");
            traceStream.write("Trace message".getBytes(StandardCharsets.UTF_8));
            traceStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel(), "should be trace level");
        }

        @Test
        @DisplayName("should get debug output stream when logger enabled")
        void shouldGetDebugOutputStreamWhenLoggerEnabled() throws Exception {
            // Given: a logger with debug enabled
            final Logger logger = LoggerFactory.getLogger("debugLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setDebugEnabled(true);
            mockLogger.clearEvents();
            // When: getDebugOutputStream is called
            final OutputStream debugStream = LoggerFactory.getDebugOutputStream(logger);
            // Then: should return output stream that logs debug messages
            assertNotNull(debugStream, "should return non-null output stream");
            debugStream.write("Debug message".getBytes(StandardCharsets.UTF_8));
            debugStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel(), "should be debug level");
        }

        @Test
        @DisplayName("should get info output stream when logger enabled")
        void shouldGetInfoOutputStreamWhenLoggerEnabled() throws Exception {
            // Given: a logger with info enabled
            final Logger logger = LoggerFactory.getLogger("infoLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setInfoEnabled(true);
            mockLogger.clearEvents();
            // When: getInfoOutputStream is called
            final OutputStream infoStream = LoggerFactory.getInfoOutputStream(logger);
            // Then: should return output stream that logs info messages
            assertNotNull(infoStream, "should return non-null output stream");
            infoStream.write("Info message".getBytes(StandardCharsets.UTF_8));
            infoStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel(), "should be info level");
        }

        @Test
        @DisplayName("should get warn output stream when logger enabled")
        void shouldGetWarnOutputStreamWhenLoggerEnabled() throws Exception {
            // Given: a logger with warn enabled
            final Logger logger = LoggerFactory.getLogger("warnLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setWarnEnabled(true);
            mockLogger.clearEvents();
            // When: getWarnOutputStream is called
            final OutputStream warnStream = LoggerFactory.getWarnOutputStream(logger);
            // Then: should return output stream that logs warn messages
            assertNotNull(warnStream, "should return non-null output stream");
            warnStream.write("Warn message".getBytes(StandardCharsets.UTF_8));
            warnStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel(), "should be warn level");
        }

        @Test
        @DisplayName("should get error output stream when logger enabled")
        void shouldGetErrorOutputStreamWhenLoggerEnabled() throws Exception {
            // Given: a logger with error enabled
            final Logger logger = LoggerFactory.getLogger("errorLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setErrorEnabled(true);
            mockLogger.clearEvents();
            // When: getErrorOutputStream is called
            final OutputStream errorStream = LoggerFactory.getErrorOutputStream(logger);
            // Then: should return output stream that logs error messages
            assertNotNull(errorStream, "should return non-null output stream");
            errorStream.write("Error message".getBytes(StandardCharsets.UTF_8));
            errorStream.close();
            assertEquals(1, mockLogger.getEventCount(), "should have logged one event");
            assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage(), "should log message");
            assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel(), "should be error level");
        }
    }

    @Nested
    class GetDisabledOutputStream {
        @Test
        @DisplayName("should return NullOutputStream when trace logger disabled")
        void shouldReturnNullOutputStreamWhenTraceLoggerDisabled() throws Exception {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("traceLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getTraceOutputStream is called
            final OutputStream traceStream = LoggerFactory.getTraceOutputStream(logger);
            traceStream.close();
            // Then: should return NullOutputStream and not log
            assertNotNull(traceStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, traceStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when debug logger disabled")
        void shouldReturnNullOutputStreamWhenDebugLoggerDisabled() throws Exception {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("debugLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getDebugOutputStream is called
            final OutputStream debugStream = LoggerFactory.getDebugOutputStream(logger);
            // Then: should return NullOutputStream and not log
            assertNotNull(debugStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, debugStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when info logger disabled")
        void shouldReturnNullOutputStreamWhenInfoLoggerDisabled() throws Exception {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("infoLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getInfoOutputStream is called
            final OutputStream infoStream = LoggerFactory.getInfoOutputStream(logger);
            // Then: should return NullOutputStream and not log
            assertNotNull(infoStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, infoStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when warn logger disabled")
        void shouldReturnNullOutputStreamWhenWarnLoggerDisabled() throws Exception {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("warnLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getWarnOutputStream is called
            final OutputStream warnStream = LoggerFactory.getWarnOutputStream(logger);
            // Then: should return NullOutputStream and not log
            assertNotNull(warnStream, "should return non-null output stream");
            assertInstanceOf(NullOutputStream.class, warnStream, "should return NullOutputStream");
            assertEquals(0, mockLogger.getEventCount(), "should not log any events");
        }

        @Test
        @DisplayName("should return NullOutputStream when error logger disabled")
        void shouldReturnNullOutputStreamWhenErrorLoggerDisabled() throws Exception {
            // Given: a disabled logger
            final Logger logger = LoggerFactory.getLogger("errorLogger");
            final MockLogger mockLogger = (MockLogger) logger;
            mockLogger.setEnabled(false);
            mockLogger.clearEvents();
            // When: getErrorOutputStream is called
            final OutputStream errorStream = LoggerFactory.getErrorOutputStream(logger);
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
