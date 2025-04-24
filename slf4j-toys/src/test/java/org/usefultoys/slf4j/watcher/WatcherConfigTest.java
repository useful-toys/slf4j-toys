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

import org.junit.jupiter.api.*;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class WatcherConfigTest {

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

    @Test
    void testDefaultValues() {
        WatcherConfig.init();
        assertEquals("watcher", WatcherConfig.name, "Default value for name should be 'watcher'");
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "Default value for delayMilliseconds should be 60000");
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "Default value for periodMilliseconds should be 600000");
        assertEquals("", WatcherConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", WatcherConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertFalse(WatcherConfig.dataEnabled, "Default value for dataEnabled should be false");
        assertEquals("", WatcherConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", WatcherConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
    }

    @Test
    void testResetValues() {
        WatcherConfig.reset();
        assertEquals("watcher", WatcherConfig.name, "Default value for name should be 'watcher'");
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "Default value for delayMilliseconds should be 60000");
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "Default value for periodMilliseconds should be 600000");
        assertEquals("", WatcherConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", WatcherConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertFalse(WatcherConfig.dataEnabled, "Default value for dataEnabled should be false");
        assertEquals("", WatcherConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", WatcherConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
    }

    @Test
    void testDelayMillisecondsProperty() {
        System.setProperty(WatcherConfig.PROP_DELAY, "120000ms");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "120000");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "120s");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "2m");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "2min");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_DELAY, "1h");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(3600000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");
    }

    @Test
    void testPeriodMillisecondsProperty() {
        System.setProperty(WatcherConfig.PROP_PERIOD, "120000ms");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "120000");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "120s");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "2m");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "2min");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");

        System.setProperty(WatcherConfig.PROP_PERIOD, "1h");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(3600000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");
    }

    @Test
    void testDataPrefixProperty() {
        System.setProperty(WatcherConfig.PROP_DATA_PREFIX, "data.");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals("data.", WatcherConfig.dataPrefix, "dataPrefix should reflect the system property value");
    }

    @Test
    void testDataSuffixProperty() {
        System.setProperty(WatcherConfig.PROP_DATA_SUFFIX, ".data");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(".data", WatcherConfig.dataSuffix, "dataSuffix should reflect the system property value");
    }

    @Test
    void testDataEnabledProperty() {
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "true");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertTrue(WatcherConfig.dataEnabled, "dataEnabled should reflect the system property value");
    }

    @Test
    void testMessagePrefixProperty() {
        System.setProperty(WatcherConfig.PROP_MESSAGE_PREFIX, "message.");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals("message.", WatcherConfig.messagePrefix, "messagePrefix should reflect the system property value");
    }

    @Test
    void testMessageSuffixProperty() {
        System.setProperty(WatcherConfig.PROP_MESSAGE_SUFFIX, ".message");
        WatcherConfig.init(); // Reinitialize to apply new system properties
        assertEquals(".message", WatcherConfig.messageSuffix, "messageSuffix should reflect the system property value");
    }
}
