package org.usefultoys.slf4j.watcher;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WatcherSingletonTest {

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

    private TestLogger testLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);;

    @BeforeEach
    void setupLogger() {
        testLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        testLogger.clearEvents();
    }

    @AfterEach
    void stopAllWatchers() {
        WatcherSingleton.stopDefaultWatcherExecutor();
        WatcherSingleton.stopDefaultWatcherTimer();
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
        final String json5 = testLogger.getEvent(1).getMessage();
        final WatcherData data = new WatcherData();
        data.readJson5(json5);
        assertEquals(json5, data.json5Message());
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
        final String json5 = testLogger.getEvent(1).getMessage();
        final WatcherData data = new WatcherData();
        data.readJson5(json5);
        assertEquals(json5, data.json5Message());
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
