package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
class WatcherTest {
    private static final TestLogger testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);

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
        testLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        testLogger.clearEvents();
    }

    @AfterAll
    static void resetLogger() {
        testLogger.setTraceEnabled(true);
        testLogger.setDebugEnabled(true);
        testLogger.setInfoEnabled(true);
        testLogger.setWarnEnabled(true);
        testLogger.setErrorEnabled(true);
        testLogger.clearEvents();
    }

    @Test
    void shouldIncrementPositionAndTimeUsingTraceLogger() {
        WatcherConfig.dataEnabled = true;
        testLogger.setTraceEnabled(true);
        testLogger.setDebugEnabled(true);
        testLogger.setInfoEnabled(true);
        testLogger.setWarnEnabled(true);
        testLogger.setErrorEnabled(true);
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
        assertTrue(testLogger.getEventCount() == 2);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        final String json5 = testLogger.getEvent(1).getMessage();
        final WatcherData data = new WatcherData();
        data.readJson5(json5);
        assertEquals(json5, data.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeDisabledDataLogger() {
        WatcherConfig.dataEnabled = false;
        testLogger.setTraceEnabled(true);
        testLogger.setDebugEnabled(true);
        testLogger.setInfoEnabled(true);
        testLogger.setWarnEnabled(true);
        testLogger.setErrorEnabled(true);
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
        assertTrue(testLogger.getEventCount() == 2);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        final String json5 = testLogger.getEvent(1).getMessage();
        final WatcherData data = new WatcherData();
        data.readJson5(json5);
        assertEquals(json5, data.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeInfoLogger() {
        testLogger.setTraceEnabled(false);
        testLogger.setDebugEnabled(false);
        testLogger.setInfoEnabled(true);
        testLogger.setWarnEnabled(true);
        testLogger.setErrorEnabled(true);
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
        assertTrue(testLogger.getEventCount() == 1);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    @Test
    void shouldIncrementPositionAndTimeErrorLogger() {
        testLogger.setTraceEnabled(false);
        testLogger.setDebugEnabled(false);
        testLogger.setInfoEnabled(false);
        testLogger.setWarnEnabled(false);
        testLogger.setErrorEnabled(true);
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
        assertTrue(testLogger.getEventCount() == 0);
    }
}