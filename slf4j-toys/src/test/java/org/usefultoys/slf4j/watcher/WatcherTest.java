package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import static org.junit.jupiter.api.Assertions.*;
class WatcherTest {
    private static TestLogger testLogger;

    @BeforeAll
    static void setupLogger() {
        testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);;
        testLogger.clearEvents();
    }

    @Test
    void shouldIncrementPositionAndTime() {
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
        assertTrue(testLogger.getEventCount() > 0);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        assertTrue(testLogger.getEvent(1).getFormattedMessage().contains("W{"));
    }
}