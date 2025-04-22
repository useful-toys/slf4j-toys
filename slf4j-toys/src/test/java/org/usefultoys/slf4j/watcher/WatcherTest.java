package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
class WatcherTest {
    private TestLogger testLogger;
    private TestLogger messageLogger;
    private TestLogger dataLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize WatcherConfig to ensure clean configuration before each test
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize WatcherConfig to ensure clean configuration for further tests
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setupLogger() {
        testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);
        messageLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name + ".message");
        dataLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name + ".data");
        testLogger.clearEvents();
        messageLogger.clearEvents();
        dataLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        testLogger.setEnabled(true);
        testLogger.clearEvents();
        messageLogger.setEnabled(true);
        messageLogger.clearEvents();
        dataLogger.setEnabled(true);
        dataLogger.clearEvents();
    }

    @Test
    void shouldIncrementPositionAndTimeUsingSeparatedTraceLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = ".data";
        WatcherConfig.messageSuffix = ".message";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        testLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getTime();
        watcher.logCurrentStatus();
        assertTrue(watcher.getPosition() == position + 1);
        assertTrue(watcher.getTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Readable and encoded messages are written to the separated logs
        assertTrue(testLogger.getEventCount() == 0);
        assertTrue(messageLogger.getEventCount() == 1);
        assertTrue(messageLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        assertTrue(dataLogger.getEventCount() == 1);
        final String json5 = dataLogger.getEvent(0).getMessage();
        assertEquals(json5, watcher.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeUsingSameTraceLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        testLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getTime();
        watcher.logCurrentStatus();
        assertTrue(watcher.getPosition() == position + 1);
        assertTrue(watcher.getTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Readable and encoded messages are written to the same log
        assertTrue(messageLogger.getEventCount() == 0);
        assertTrue(dataLogger.getEventCount() == 0);
        assertTrue(testLogger.getEventCount() == 2);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        final String json5 = testLogger.getEvent(1).getMessage();
        assertEquals(json5, watcher.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeDisabledDataLogger() {
        WatcherConfig.dataEnabled = false;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        testLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getTime();
        watcher.logCurrentStatus();
        assertTrue(watcher.getPosition() == position + 1);
        assertTrue(watcher.getTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Only readable message is written to log
        assertTrue(messageLogger.getEventCount() == 0);
        assertTrue(dataLogger.getEventCount() == 0);
        assertTrue(testLogger.getEventCount() == 1);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    @Test
    void shouldIncrementPositionAndTimeInfoLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        testLogger.setTraceEnabled(false);
        testLogger.setDebugEnabled(false);
        testLogger.setInfoEnabled(true);
        testLogger.setWarnEnabled(true);
        testLogger.setErrorEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getTime();
        watcher.logCurrentStatus();
        assertTrue(watcher.getPosition() == position + 1);
        assertTrue(watcher.getTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Only readable message is written to log
        assertTrue(messageLogger.getEventCount() == 0);
        assertTrue(dataLogger.getEventCount() == 0);
        assertTrue(testLogger.getEventCount() == 1);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    @Test
    void shouldIncrementPositionAndTimeErrorLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        testLogger.setTraceEnabled(false);
        testLogger.setDebugEnabled(false);
        testLogger.setInfoEnabled(false);
        testLogger.setWarnEnabled(false);
        testLogger.setErrorEnabled(false);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getTime();
        watcher.logCurrentStatus();
        assertTrue(watcher.getPosition() == position + 1);
        assertTrue(watcher.getTime() > time);
        // As nothing is logged, the memory usage won't be collected and should be 0
        assertTrue(watcher.getRuntime_usedMemory() == 0);

        // No messages a written to log
        assertTrue(messageLogger.getEventCount() == 0);
        assertTrue(dataLogger.getEventCount() == 0);
        assertTrue(testLogger.getEventCount() == 0);
    }
}