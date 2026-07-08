/*
 * Copyright 2026 Daniel Felix Ferber
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

package org.usefultoys.slf4j.watcher;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link WatcherExecutorController}.
 * <p>
 * Tests validate that the controller correctly starts and stops scheduled watchers
 * using a {@link java.util.concurrent.ScheduledExecutorService}, with proper event logging
 * and idempotent lifecycle operations.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Lifecycle:</b> Verifies start/stop idempotency and resource cleanup</li>
 *   <li><b>Scheduling:</b> Confirms the watcher runs and produces log events</li>
 *   <li><b>Failure recovery:</b> Confirms a single watcher exception is logged and does not
 *       suppress future executions or change the running state</li>
 * </ul>
 */
@ValidateCharset
@ResetWatcherConfig
@WithMockLogger
class WatcherExecutorControllerTest {

    @Slf4jMock("watcher")
    private MockLogger logger;

    @AfterEach
    void stopController() {
        // No global state to clean; each test owns its controller instance.
    }

    @Test
    @DisplayName("should create controller with defaults from WatcherConfig")
    void shouldCreateControllerWithDefaults() {
        // When: creating with the no-arg shortcut
        final WatcherExecutorController controller = WatcherExecutorController.create();

        // Then: it is not running and can be started/stopped
        assertNotNull(controller, "controller should be created");
        assertFalse(controller.isRunning(), "controller should not be running initially");
        assertDoesNotThrow(controller::start, "should start without throwing");
        assertTrue(controller.isRunning(), "controller should be running after start");
        assertDoesNotThrow(controller::stop, "should stop without throwing");
        assertFalse(controller.isRunning(), "controller should not be running after stop");
    }

    @Test
    @DisplayName("should create controller with custom name and schedule")
    void shouldCreateControllerWithOverrides() {
        // Given: a custom name and a logger bound to that name
        final String customName = "custom-watcher";
        final MockLogger customLogger = (MockLogger) LoggerFactory.getLogger(customName);
        customLogger.clearEvents();

        // When: creating with explicit name, delay and period
        final WatcherExecutorController controller =
                WatcherExecutorController.create(customName, 200, 200);

        // Then: it runs the watcher under the requested name
        assertNotNull(controller);
        controller.start();
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> customLogger.getEventCount() > 0);
        AssertLogger.assertEventCount(customLogger, 1);
        AssertLogger.assertEvent(customLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
        controller.stop();
    }

    @Test
    @DisplayName("should log status when using executor")
    void shouldLogStatusWithExecutor() {
        // Given: short schedule
        WatcherConfig.delayMilliseconds = 200;
        WatcherConfig.periodMilliseconds = 200;

        // When: creating and starting the controller
        final WatcherExecutorController controller = WatcherExecutorController.create();
        assertDoesNotThrow(controller::start, "should start without throwing");

        // Then: a log event is generated
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> logger.getEventCount() > 0);
        AssertLogger.assertEventCount(logger, 1);
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Memory:");

        // When: stopping
        assertDoesNotThrow(controller::stop, "should stop without throwing");
        assertFalse(controller.isRunning(), "controller should not be running after stop");
    }

    @Test
    @DisplayName("should support calling start multiple times")
    void shouldSupportCallingStartMultipleTimes() {
        // Given: a fresh controller
        final WatcherExecutorController controller = WatcherExecutorController.create();

        // When: starting twice
        assertDoesNotThrow(controller::start, "first start should not throw");
        assertDoesNotThrow(controller::start, "second start should not throw");

        // Then: it remains running
        assertTrue(controller.isRunning(), "controller should be running");

        controller.stop();
    }

    @Test
    @DisplayName("should support calling stop multiple times")
    void shouldSupportCallingStopMultipleTimes() {
        // Given: a started controller
        final WatcherExecutorController controller = WatcherExecutorController.create();
        controller.start();
        assertTrue(controller.isRunning());

        // When: stopping twice
        assertDoesNotThrow(controller::stop, "first stop should not throw");
        assertDoesNotThrow(controller::stop, "second stop should not throw");

        // Then: it is not running
        assertFalse(controller.isRunning(), "controller should not be running");
    }

    @Test
    @DisplayName("should stop and clear resources")
    void shouldStopAndClearResources() {
        // Given: a started controller
        final WatcherExecutorController controller = WatcherExecutorController.create();
        controller.start();
        assertTrue(controller.isRunning());

        // When: stopped
        controller.stop();

        // Then: it reports as not running and can be restarted
        assertFalse(controller.isRunning(), "controller should not be running after stop");
        controller.start();
        assertTrue(controller.isRunning(), "controller should be running after restart");
        controller.stop();
    }

    @Test
    @DisplayName("should close via try-with-resources")
    void shouldCloseViaTryWithResources() {
        // Given: a controller started inside a try-with-resources block
        try (WatcherExecutorController controller = WatcherExecutorController.create()) {
            controller.start();
            assertTrue(controller.isRunning(), "controller should be running inside block");
        }

        // When/Then: after the block the controller is stopped
        // (cannot query isRunning() because the resource is out of scope, but no exception means success)
    }

    @Test
    @DisplayName("should keep schedule alive and log error when watcher throws exception")
    void shouldKeepScheduleAliveAndLogErrorWhenWatcherThrowsException() throws Exception {
        // Given: a controller with a watcher that fails once then succeeds
        final String watcherName = "failing-executor-watcher";
        final WatcherExecutorController controller = WatcherExecutorController.create(watcherName, 0, 200);
        final AtomicInteger executionCount = new AtomicInteger(0);
        final Watcher failingWatcher = new Watcher(watcherName) {
            @Override
            public void run() {
                if (executionCount.incrementAndGet() == 1) {
                    throw new RuntimeException("Simulated watcher failure");
                }
            }
        };
        final Field watcherField = WatcherExecutorController.class.getDeclaredField("watcher");
        watcherField.setAccessible(true);
        watcherField.set(controller, failingWatcher);

        final MockLogger controllerLogger = (MockLogger) LoggerFactory.getLogger(WatcherExecutorController.class);
        controllerLogger.clearEvents();

        // When: starting the controller
        controller.start();

        // Then: the schedule survives the failure and runs at least twice
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> executionCount.get() >= 2);
        assertTrue(controller.isRunning(), "controller should still be running after watcher failure");
        AssertLogger.assertEvent(controllerLogger, 0, MockLoggerEvent.Level.ERROR,
                "Watcher execution failed; next executions remain scheduled.");

        controller.stop();
    }
}
