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
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.test.CharsetConsistency;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(CharsetConsistency.class)
class LoggerFactoryTest {

    @Test
    void testGetLoggerByName() {
        final Logger logger = LoggerFactory.getLogger("testLogger");
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals("testLogger", logger.getName());
    }

    @Test
    void testGetLoggerByClass() {
        final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(LoggerFactoryTest.class.getName(), logger.getName());
    }

    @Test
    void testGetLoggerByClassAndName() {
        final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class, "feature");
        assertNotNull(logger);
        assertInstanceOf(MockLogger.class, logger);
        assertEquals(LoggerFactoryTest.class.getName() + ".feature", logger.getName());
    }

    @Test
    void testGetLoggerByParentLoggerAndName() {
        final Logger parentLogger = LoggerFactory.getLogger("parentLogger");
        final Logger childLogger = LoggerFactory.getLogger(parentLogger, "child");
        assertNotNull(childLogger);
        assertInstanceOf(MockLogger.class, childLogger);
        assertEquals("parentLogger.child", childLogger.getName());
    }

    @Test
    void testGetTracePrintStream() {
        final Logger logger = LoggerFactory.getLogger("traceLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setTraceEnabled(true);
        mockLogger.clearEvents();

        final PrintStream traceStream = LoggerFactory.getTracePrintStream(logger);
        assertNotNull(traceStream);
        traceStream.print("Trace message");
        traceStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetDebugPrintStream() {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setDebugEnabled(true);
        mockLogger.clearEvents();

        final PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger);
        assertNotNull(debugStream);
        debugStream.print("Debug message");
        debugStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetInfoPrintStream() {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setInfoEnabled(true);
        mockLogger.clearEvents();

        final PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger);
        assertNotNull(infoStream);
        infoStream.print("Info message");
        infoStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetWarnPrintStream() {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setWarnEnabled(true);
        mockLogger.clearEvents();

        final PrintStream warnStream = LoggerFactory.getWarnPrintStream(logger);
        assertNotNull(warnStream);
        warnStream.print("Warn message");
        warnStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetErrorPrintStream() {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setErrorEnabled(true);
        mockLogger.clearEvents();

        final PrintStream errorStream = LoggerFactory.getErrorPrintStream(logger);
        assertNotNull(errorStream);
        errorStream.print("Error message");
        errorStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetTracePrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("traceLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream traceStream = LoggerFactory.getTracePrintStream(logger);
        assertNotNull(traceStream);
        assertInstanceOf(NullPrintStream.class, traceStream);
        // Ensure no events are logged
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetDebugPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger);
        assertNotNull(debugStream);
        assertInstanceOf(NullPrintStream.class, debugStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetInfoPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger);
        assertNotNull(infoStream);
        assertInstanceOf(NullPrintStream.class, infoStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetWarnPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream warnStream = LoggerFactory.getWarnPrintStream(logger);
        assertNotNull(warnStream);
        assertInstanceOf(NullPrintStream.class, warnStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetErrorPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream errorStream = LoggerFactory.getErrorPrintStream(logger);
        assertNotNull(errorStream);
        assertInstanceOf(NullPrintStream.class, errorStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testNullPrintStreamBehavior() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> {
            nullPrintStream.print("test");
            nullPrintStream.println("test");
            nullPrintStream.write(1);
            nullPrintStream.write("test".getBytes(StandardCharsets.UTF_8));
            nullPrintStream.write("test".getBytes(StandardCharsets.UTF_8), 0, 2);
            nullPrintStream.flush();
            nullPrintStream.close();
        });
    }

    @Test
    void testGetTraceOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("traceLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setTraceEnabled(true);
        mockLogger.clearEvents();

        final OutputStream traceStream = LoggerFactory.getTraceOutputStream(logger);
        assertNotNull(traceStream);
        traceStream.write("Trace message".getBytes(StandardCharsets.UTF_8));
        traceStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Trace message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.TRACE, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetDebugOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setDebugEnabled(true);
        mockLogger.clearEvents();

        final OutputStream debugStream = LoggerFactory.getDebugOutputStream(logger);
        assertNotNull(debugStream);
        debugStream.write("Debug message".getBytes(StandardCharsets.UTF_8));
        debugStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Debug message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.DEBUG, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetInfoOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setInfoEnabled(true);
        mockLogger.clearEvents();

        final OutputStream infoStream = LoggerFactory.getInfoOutputStream(logger);
        assertNotNull(infoStream);
        infoStream.write("Info message".getBytes(StandardCharsets.UTF_8));
        infoStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Info message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.INFO, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetWarnOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setWarnEnabled(true);
        mockLogger.clearEvents();

        final OutputStream warnStream = LoggerFactory.getWarnOutputStream(logger);
        assertNotNull(warnStream);
        warnStream.write("Warn message".getBytes(StandardCharsets.UTF_8));
        warnStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Warn message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.WARN, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetErrorOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setErrorEnabled(true);
        mockLogger.clearEvents();

        final OutputStream errorStream = LoggerFactory.getErrorOutputStream(logger);
        assertNotNull(errorStream);
        errorStream.write("Error message".getBytes(StandardCharsets.UTF_8));
        errorStream.close();

        assertEquals(1, mockLogger.getEventCount());
        assertEquals("Error message", mockLogger.getEvent(0).getFormattedMessage());
        assertEquals(MockLoggerEvent.Level.ERROR, mockLogger.getEvent(0).getLevel());
    }

    @Test
    void testGetTraceOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("traceLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);

        final OutputStream traceStream = LoggerFactory.getTraceOutputStream(logger);
        assertNotNull(traceStream);
        assertInstanceOf(NullOutputStream.class, traceStream);
        // Ensure no events are logged
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetDebugOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream debugStream = LoggerFactory.getDebugOutputStream(logger);
        assertNotNull(debugStream);
        assertInstanceOf(NullOutputStream.class, debugStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetInfoOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream infoStream = LoggerFactory.getInfoOutputStream(logger);
        assertNotNull(infoStream);
        assertInstanceOf(NullOutputStream.class, infoStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetWarnOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream warnStream = LoggerFactory.getWarnOutputStream(logger);
        assertNotNull(warnStream);
        assertInstanceOf(NullOutputStream.class, warnStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testGetErrorOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setErrorEnabled(false); // Disable error
        mockLogger.clearEvents();

        final OutputStream errorStream = LoggerFactory.getErrorOutputStream(logger);
        assertNotNull(errorStream);
        assertInstanceOf(NullOutputStream.class, errorStream);
        assertEquals(0, mockLogger.getEventCount());
    }

    @Test
    void testNullOutputStreamBehavior() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> {
            nullOutputStream.write(1);
            nullOutputStream.write("test".getBytes(StandardCharsets.UTF_8));
            nullOutputStream.write("test".getBytes(StandardCharsets.UTF_8), 0, 2);
            nullOutputStream.flush();
            nullOutputStream.close();
        });
    }
}
