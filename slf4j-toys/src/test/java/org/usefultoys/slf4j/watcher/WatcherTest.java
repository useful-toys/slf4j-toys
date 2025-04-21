package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import static org.junit.jupiter.api.Assertions.*;
class WatcherTest {
    private static final TestLogger testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);

    @BeforeEach
    void setupLogger() {
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