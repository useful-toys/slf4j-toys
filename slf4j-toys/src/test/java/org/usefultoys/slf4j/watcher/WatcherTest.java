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
package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatcherTest {

    private static final String TEST_WATCHER_NAME = "myWatcher";
    private MockLogger messageMockLogger;
    private MockLogger dataMockLogger;

    @BeforeEach
    void setUp() {
        ConfigParser.clearInitializationErrors();
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();

        // Set distinct suffixes for message and data loggers in WatcherConfig
        // This ensures Watcher's constructor gets distinct MockLogger instances
        System.setProperty(WatcherConfig.PROP_MESSAGE_SUFFIX, ".msg");
        System.setProperty(WatcherConfig.PROP_DATA_SUFFIX, ".data");
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "true"); // Ensure data logging is enabled by default for setup
        WatcherConfig.init(); // Apply these properties

        // Get the specific MockLogger instances that Watcher will use
        Logger messageLogger = LoggerFactory.getLogger(WatcherConfig.messagePrefix + TEST_WATCHER_NAME + WatcherConfig.messageSuffix);
        messageMockLogger = (MockLogger) messageLogger;
        messageMockLogger.clearEvents();
        messageMockLogger.setInfoEnabled(true); // Ensure INFO level is enabled for message logger

        Logger dataLogger = LoggerFactory.getLogger(WatcherConfig.dataPrefix + TEST_WATCHER_NAME + WatcherConfig.dataSuffix);
        dataMockLogger = (MockLogger) dataLogger;
        dataMockLogger.clearEvents();
        dataMockLogger.setTraceEnabled(true); // Ensure TRACE level is enabled for data logger
    }

    @AfterEach
    void tearDown() {
        // Clear properties set in setUp or tests
        System.clearProperty(WatcherConfig.PROP_MESSAGE_SUFFIX);
        System.clearProperty(WatcherConfig.PROP_DATA_SUFFIX);
        System.clearProperty(WatcherConfig.PROP_DATA_ENABLED);

        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    @Test
    void testWatcherLogsInfoAndTraceWhenDataEnabled() {
        // Config already set in setUp: PROP_DATA_ENABLED="true", suffixes set, levels enabled

        Watcher watcher = new Watcher(TEST_WATCHER_NAME);
        watcher.run();

        // Verify messageLogger (INFO)
        assertEquals(1, messageMockLogger.getEventCount(), "messageMockLogger should have 1 event");
        messageMockLogger.assertEvent(0, MockLoggerEvent.Level.INFO, Markers.MSG_WATCHER);
        assertTrue(messageMockLogger.getEvent(0).getFormattedMessage().contains("Memory:"), "Message logger output should contain Memory info");

        // Verify dataMockLogger (TRACE)
        assertEquals(1, dataMockLogger.getEventCount(), "dataMockLogger should have 1 event");
        dataMockLogger.assertEvent(0, MockLoggerEvent.Level.TRACE, Markers.DATA_WATCHER);
        assertTrue(dataMockLogger.getEvent(0).getFormattedMessage().contains("_:"), "Data logger output should contain sessionUuid");

        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testWatcherLogsInfoOnlyWhenDataDisabled() {
        // Disable data logging for this test
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "false");
        WatcherConfig.init(); // Apply the change

        // Ensure message logger is enabled
        messageMockLogger.setInfoEnabled(true);
        // dataMockLogger's traceEnabled is true, but Watcher's dataLogger will be NullLogger.INSTANCE

        Watcher watcher = new Watcher(TEST_WATCHER_NAME);
        watcher.run();

        // Verify messageLogger (INFO)
        assertEquals(1, messageMockLogger.getEventCount(), "messageMockLogger should have 1 event");
        messageMockLogger.assertEvent(0, MockLoggerEvent.Level.INFO, Markers.MSG_WATCHER);
        assertTrue(messageMockLogger.getEvent(0).getFormattedMessage().contains("Memory:"), "Message logger output should contain Memory info");

        // Verify dataMockLogger (should be NullLogger, so no events)
        assertEquals(0, dataMockLogger.getEventCount(), "dataMockLogger should have 0 events when data is disabled");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testWatcherLogsNothingWhenBothDisabled() {
        // Disable data logging
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "false");
        WatcherConfig.init();

        // Disable info for message logger
        messageMockLogger.setInfoEnabled(false);
        // dataMockLogger's traceEnabled is true, but Watcher's dataLogger will be NullLogger.INSTANCE, so no need to disable dataMockLogger's traceEnabled explicitly here.

        Watcher watcher = new Watcher(TEST_WATCHER_NAME);
        watcher.run();

        // Verify no events are logged
        assertEquals(0, messageMockLogger.getEventCount(), "messageMockLogger should have 0 events");
        assertEquals(0, dataMockLogger.getEventCount(), "dataMockLogger should have 0 events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testWatcherCollectsDataOnlyWhenLoggingEnabled() {
        // Ensure data logging is enabled in config
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "true");
        WatcherConfig.init();

        // Disable info for message logger and trace for data logger
        messageMockLogger.setInfoEnabled(false);
        dataMockLogger.setTraceEnabled(false);

        Watcher watcher = new Watcher(TEST_WATCHER_NAME);
        watcher.run();

        // No events should be logged because logger levels are disabled
        assertEquals(0, messageMockLogger.getEventCount(), "messageMockLogger should have 0 events");
        assertEquals(0, dataMockLogger.getEventCount(), "dataMockLogger should have 0 events");

        // But the internal position counter should still increment
        assertEquals(1, watcher.getPosition());
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
