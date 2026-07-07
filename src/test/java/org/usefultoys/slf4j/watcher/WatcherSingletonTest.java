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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link WatcherSingleton}.
 * <p>
 * Since push execution has been migrated to {@link WatcherExecutorController} and
 * {@link WatcherTimerController}, this class now only verifies the lazy default {@link Watcher}
 * instance used by the servlet pull path.
 */
@ValidateCharset
@ResetWatcherConfig
@WithMockLogger
class WatcherSingletonTest {

    @Slf4jMock("watcher")
    private MockLogger logger;

    @Test
    @DisplayName("should return the same default watcher instance")
    void shouldReturnSameDefaultWatcherInstance() {
        // When: the default watcher is requested twice
        final Watcher first = WatcherSingleton.getDefaultWatcher();
        final Watcher second = WatcherSingleton.getDefaultWatcher();

        // Then: both references point to the same lazily-created instance
        assertNotNull(first, "default watcher should be created on first access");
        assertSame(first, second, "default watcher should be a singleton");
    }

    @Test
    @DisplayName("should run the default watcher using the configured name")
    void shouldRunDefaultWatcherUsingConfiguredName() {
        // Given: the default watcher created from WatcherConfig.name ("watcher")
        final Watcher watcher = WatcherSingleton.getDefaultWatcher();

        // When: the watcher is executed
        watcher.run();

        // Then: it logs to the logger named after WatcherConfig.name
        AssertLogger.assertEventCount(logger, 1);
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }
}
