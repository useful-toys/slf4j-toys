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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Watcher}.
 * <p>
 * Tests validate that Watcher correctly logs messages and data based on configuration,
 * with proper handling of various logger states and prefix/suffix combinations.
 */
@ValidateCharset
@ResetWatcherConfig
class WatcherTest {
    private static final String TEST_WATCHER_NAME = "myWatcher";

    /**
     * Test scenario class containing all parameters for a watcher test case.
     * Simulates a record/tuple structure for Java 8 compatibility.
     */
    private static final class WatcherTestScenario {
        final String testName;
        final String messagePrefix;
        final String messageSuffix;
        final String dataPrefix;
        final String dataSuffix;
        final boolean dataEnabled;
        final boolean messageLoggerEnabled;
        final boolean dataLoggerEnabled;
        final boolean expectMessageLog;
        final boolean expectDataLog;
        final String expectedMessageContent;
        final String expectedDataContent;

        private WatcherTestScenario(final String testName, final String messagePrefix, final String messageSuffix, final String dataPrefix, final String dataSuffix, final boolean dataEnabled, final boolean messageLoggerEnabled, final boolean dataLoggerEnabled, final boolean expectMessageLog, final boolean expectDataLog, final String expectedMessageContent, final String expectedDataContent) {
            this.testName = testName;
            this.messagePrefix = messagePrefix;
            this.messageSuffix = messageSuffix;
            this.dataPrefix = dataPrefix;
            this.dataSuffix = dataSuffix;
            this.dataEnabled = dataEnabled;
            this.messageLoggerEnabled = messageLoggerEnabled;
            this.dataLoggerEnabled = dataLoggerEnabled;
            this.expectMessageLog = expectMessageLog;
            this.expectDataLog = expectDataLog;
            this.expectedMessageContent = expectedMessageContent;
            this.expectedDataContent = expectedDataContent;
        }

        @Override
        public String toString() {
            return testName; // Used by @ParameterizedTest(name = "{0}")
        }
    }

    private static Stream<WatcherTestScenario> provideWatcherLoggingScenarios() {
        return Stream.of(
            new WatcherTestScenario("Both loggers enabled", "", ".msg", "", ".data", true, true, true, true, true, "Memory:", "_:"),
            new WatcherTestScenario("Data logger disabled by config", "", ".msg", "", ".data", false, true, true, true, false, "Memory:", null),
            new WatcherTestScenario("Message logger level disabled", "", ".msg", "", ".data", true, false, true, false, true, null, "_:"),
            new WatcherTestScenario("Data logger level disabled", "", ".msg", "", ".data", true, true, false, true, false, "Memory:", null),
            new WatcherTestScenario("Both loggers level disabled", "", ".msg", "", ".data", true, false, false, false, false, null, null),
            new WatcherTestScenario("Custom prefixes and suffixes", "p-msg-", ".s-msg", "p-data-", ".s-data", true, true, true, true, true, "Memory:", "_:")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideWatcherLoggingScenarios")
    @DisplayName("should log watcher messages and data according to configuration")
    void shouldLogWatcherMessagesAndDataAccordingToConfiguration(final WatcherTestScenario scenario) {
        /*
           Note: This test requires creating a dedicated Watcher instance for each scenario.
           And requires setting up loggers with specific configurations for each scenario.
           Therefore, we cannot make use of MockLogger JUnit Extensions.
         */
        // Given: system properties and loggers configured for test scenario
        // 1. Set system properties for the current test scenario
        System.setProperty(WatcherConfig.PROP_MESSAGE_PREFIX, scenario.messagePrefix);
        System.setProperty(WatcherConfig.PROP_MESSAGE_SUFFIX, scenario.messageSuffix);
        System.setProperty(WatcherConfig.PROP_DATA_PREFIX, scenario.dataPrefix);
        System.setProperty(WatcherConfig.PROP_DATA_SUFFIX, scenario.dataSuffix);
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, String.valueOf(scenario.dataEnabled));
        WatcherConfig.init();
        assertTrue(ConfigParser.isInitializationOK(), "ConfigParser should have no errors for scenario: " + scenario.testName + " - " + ConfigParser.initializationErrors);

        // 2. Get the mock loggers that the Watcher will use
        final String messageLoggerName = scenario.messagePrefix + TEST_WATCHER_NAME + scenario.messageSuffix;
        final MockLogger messageMockLogger = (MockLogger) LoggerFactory.getLogger(messageLoggerName);
        messageMockLogger.clearEvents();
        messageMockLogger.setEnabled(scenario.messageLoggerEnabled);

        final String dataLoggerName = scenario.dataPrefix + TEST_WATCHER_NAME + scenario.dataSuffix;
        final MockLogger dataMockLogger = (MockLogger) LoggerFactory.getLogger(dataLoggerName);
        dataMockLogger.clearEvents();
        dataMockLogger.setEnabled(scenario.dataLoggerEnabled);

        // When: watcher is executed
        final Watcher watcher = new Watcher(TEST_WATCHER_NAME);
        watcher.run();

        // Then: assertions should match expected logging behavior
        if (scenario.expectMessageLog) {
            assertEquals(1, messageMockLogger.getEventCount(), "messageMockLogger should have 1 event for scenario: " + scenario.testName);
            messageMockLogger.assertEvent(0, MockLoggerEvent.Level.INFO, Markers.MSG_WATCHER);
            assertTrue(messageMockLogger.getEvent(0).getFormattedMessage().contains(scenario.expectedMessageContent), "Message content should match for scenario: " + scenario.testName);
        } else {
            assertEquals(0, messageMockLogger.getEventCount(), "messageMockLogger should have 0 events for scenario: " + scenario.testName);
        }

        if (scenario.expectDataLog) {
            assertEquals(1, dataMockLogger.getEventCount(), "dataMockLogger should have 1 event for scenario: " + scenario.testName);
            dataMockLogger.assertEvent(0, MockLoggerEvent.Level.TRACE, Markers.DATA_WATCHER);
            assertTrue(dataMockLogger.getEvent(0).getFormattedMessage().contains(scenario.expectedDataContent), "Data content should match for scenario: " + scenario.testName);
        } else {
            assertEquals(0, dataMockLogger.getEventCount(), "dataMockLogger should have 0 events for scenario: " + scenario.testName);
        }

        messageMockLogger.clearEvents();
        dataMockLogger.clearEvents();
    }
}
