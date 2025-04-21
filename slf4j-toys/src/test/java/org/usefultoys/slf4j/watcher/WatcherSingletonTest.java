package org.usefultoys.slf4j.watcher;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WatcherSingletonTest {

    private static TestLogger testLogger;

    @BeforeAll
    static void setupLogger() {
        testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);;
        testLogger.clearEvents();
    }

    @AfterEach
    void stopAllSchedulers() {
        WatcherSingleton.stopDefaultWatcherExecutor();
        WatcherSingleton.stopDefaultWatcherTimer();
        testLogger.clearEvents();
    }

    @Test
    void shouldIncrementPositionAndTime() {
        final long position = WatcherSingleton.DEFAULT_WATCHER.getPosition();
        final long time = WatcherSingleton.DEFAULT_WATCHER.getTime();
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getPosition() == position + 1);
        assertTrue(WatcherSingleton.DEFAULT_WATCHER.getTime() > time);
    }

    @Test
    void shouldLogStatusWithExecutor() {
        // Arrange
        WatcherConfig.delayMilliseconds = 200;
        WatcherConfig.periodMilliseconds = 200;
        WatcherSingleton.startDefaultWatcherExecutor();

        // Act & Assert
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() ->
                testLogger.getEventCount() >= 2
        );

        assertTrue(testLogger.getEventCount() > 0);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        assertTrue(testLogger.getEvent(1).getFormattedMessage().contains("W{"));
    }

    @Test
    void shouldLogStatusWithTimer() {
        // Arrange
        WatcherConfig.delayMilliseconds = 200;
        WatcherConfig.periodMilliseconds = 200;
        WatcherSingleton.startDefaultWatcherTimer();

        // Act & Assert
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() ->
                testLogger.getEventCount() >= 2
        );

        assertTrue(testLogger.getEventCount() > 0);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        assertTrue(testLogger.getEvent(1).getFormattedMessage().contains("W{"));
    }

    @Test
    void shouldStartAndStopExecutorWithoutError() {
        assertDoesNotThrow(() -> WatcherSingleton.startDefaultWatcherExecutor());
        assertNotNull(WatcherSingleton.defaultWatcherExecutor);
        assertNotNull(WatcherSingleton.scheduledDefaultWatcher);
        assertDoesNotThrow(() -> WatcherSingleton.stopDefaultWatcherExecutor());
        assertNull(WatcherSingleton.defaultWatcherExecutor);
        assertNull(WatcherSingleton.scheduledDefaultWatcher);
    }

    @Test
    void shouldStartAndStopTimerWithoutError() {
        assertDoesNotThrow(() -> WatcherSingleton.startDefaultWatcherTimer());
        assertNotNull(WatcherSingleton.defaultWatcherTimer);
        assertNotNull(WatcherSingleton.defaultWatcherTask);
        assertDoesNotThrow(() -> WatcherSingleton.stopDefaultWatcherTimer());
        assertNull(WatcherSingleton.defaultWatcherTimer);
        assertNull(WatcherSingleton.defaultWatcherTask);
    }
}
