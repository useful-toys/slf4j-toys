package org.usefultoys.slf4j;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LoggerFactoryTest {

    @Test
    void testGetLoggerByName() {
        final Logger logger = LoggerFactory.getLogger("testLogger");
        assertNotNull(logger);
        assertTrue(logger instanceof MockLogger);
        assertEquals("testLogger", logger.getName());
    }

    @Test
    void testGetLoggerByClass() {
        final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        assertNotNull(logger);
        assertTrue(logger instanceof MockLogger);
        assertEquals(LoggerFactoryTest.class.getName(), logger.getName());
    }

    @Test
    void testGetLoggerByClassAndName() {
        final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class, "feature");
        assertNotNull(logger);
        assertTrue(logger instanceof MockLogger);
        assertEquals(LoggerFactoryTest.class.getName() + ".feature", logger.getName());
    }

    @Test
    void testGetLoggerByParentLoggerAndName() {
        final Logger parentLogger = LoggerFactory.getLogger("parentLogger");
        final Logger childLogger = LoggerFactory.getLogger(parentLogger, "child");
        assertNotNull(childLogger);
        assertTrue(childLogger instanceof MockLogger);
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
        assertTrue(traceStream instanceof NullPrintStream);
    }

    @Test
    void testGetDebugPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream debugStream = LoggerFactory.getDebugPrintStream(logger);
        assertNotNull(debugStream);
        assertTrue(debugStream instanceof NullPrintStream);
    }

    @Test
    void testGetInfoPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream infoStream = LoggerFactory.getInfoPrintStream(logger);
        assertNotNull(infoStream);
        assertTrue(infoStream instanceof NullPrintStream);
    }

    @Test
    void testGetWarnPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final PrintStream warnStream = LoggerFactory.getWarnPrintStream(logger);
        assertNotNull(warnStream);
        assertTrue(warnStream instanceof NullPrintStream);
    }

    @Test
    void testGetErrorPrintStreamWithLoggerDisabled() {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);

        final PrintStream errorStream = LoggerFactory.getErrorPrintStream(logger);
        assertNotNull(errorStream);
        assertTrue(errorStream instanceof NullPrintStream);
    }

    @Test
    void testGetTraceOutputStream() throws Exception {
        final Logger logger = LoggerFactory.getLogger("traceLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setTraceEnabled(true);
        mockLogger.clearEvents();

        final OutputStream traceStream = LoggerFactory.getTraceOutputStream(logger);
        assertNotNull(traceStream);
        traceStream.write("Trace message".getBytes());
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
        debugStream.write("Debug message".getBytes());
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
        infoStream.write("Info message".getBytes());
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
        warnStream.write("Warn message".getBytes());
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
        errorStream.write("Error message".getBytes());
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
        assertTrue(traceStream instanceof NullOutputStream);
    }

    @Test
    void testGetDebugOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("debugLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream debugStream = LoggerFactory.getDebugOutputStream(logger);
        assertNotNull(debugStream);
        assertTrue(debugStream instanceof NullOutputStream);
    }

    @Test
    void testGetInfoOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("infoLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream infoStream = LoggerFactory.getInfoOutputStream(logger);
        assertNotNull(infoStream);
        assertTrue(infoStream instanceof NullOutputStream);
    }

    @Test
    void testGetWarnOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("warnLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream warnStream = LoggerFactory.getWarnOutputStream(logger);
        assertNotNull(warnStream);
        assertTrue(warnStream instanceof NullOutputStream);
    }

    @Test
    void testGetErrorOutputStreamWithLoggerDisabled() throws Exception {
        final Logger logger = LoggerFactory.getLogger("errorLogger");
        final MockLogger mockLogger = (MockLogger) logger;
        mockLogger.setEnabled(false);
        mockLogger.clearEvents();

        final OutputStream errorStream = LoggerFactory.getErrorOutputStream(logger);
        assertNotNull(errorStream);
        assertTrue(errorStream instanceof NullOutputStream);
    }
}
