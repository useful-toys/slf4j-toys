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
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link WatcherConfig}.
 * <p>
 * Tests validate that WatcherConfig correctly initializes default values, parses system properties,
 * handles invalid configurations, and properly resets to default state.
 */
@DisplayName("WatcherConfig")
@ValidateCharset
@ResetWatcherConfig
class WatcherConfigTest {

    @Test
    @DisplayName("should initialize with default values")
    void testDefaultValues() {
        // Given: default WatcherConfig state
        // When: init() is called
        WatcherConfig.init();

        // Then: all properties should have expected default values
        assertEquals("watcher", WatcherConfig.name, "Default value for name should be 'watcher'");
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "Default value for delayMilliseconds should be 60000");
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "Default value for periodMilliseconds should be 600000");
        assertEquals("", WatcherConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", WatcherConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertFalse(WatcherConfig.dataEnabled, "Default value for dataEnabled should be false");
        assertEquals("", WatcherConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", WatcherConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for default values");
    }

    @Test
    @DisplayName("should reset to default values")
    void testResetValues() {
        // Given: WatcherConfig in potentially modified state
        // When: reset() is called
        WatcherConfig.reset();

        // Then: all properties should return to default values
        assertEquals("watcher", WatcherConfig.name, "Default value for name should be 'watcher'");
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "Default value for delayMilliseconds should be 60000");
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "Default value for periodMilliseconds should be 600000");
        assertEquals("", WatcherConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", WatcherConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertFalse(WatcherConfig.dataEnabled, "Default value for dataEnabled should be false");
        assertEquals("", WatcherConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", WatcherConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported after reset");
    }

    @Test
    @DisplayName("should parse delay milliseconds from system property")
    void testDelayMillisecondsProperty() {
        // Given: system property set to valid delay value
        System.setProperty(WatcherConfig.PROP_DELAY, "120000ms");

        // When: init() is called
        WatcherConfig.init();

        // Then: delayMilliseconds should reflect the property value with no errors
        assertEquals(120000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid delayMilliseconds");
    }

    @Test
    @DisplayName("should fall back to default for invalid delay milliseconds format")
    void testDelayMillisecondsInvalidFormat() {
        // Given: system property set to invalid delay format
        System.setProperty(WatcherConfig.PROP_DELAY, "invalid");

        // When: init() is called
        WatcherConfig.init();

        // Then: delayMilliseconds should use default and report error
        assertEquals(60000L, WatcherConfig.delayMilliseconds, "delayMilliseconds should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid delayMilliseconds format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains(String.format("Invalid time value for property '%s", WatcherConfig.PROP_DELAY)));
    }

    @Test
    @DisplayName("should parse period milliseconds from system property")
    void testPeriodMillisecondsProperty() {
        // Given: system property set to valid period value
        System.setProperty(WatcherConfig.PROP_PERIOD, "120000ms");

        // When: init() is called
        WatcherConfig.init();

        // Then: periodMilliseconds should reflect the property value with no errors
        assertEquals(120000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid periodMilliseconds");
    }

    @Test
    @DisplayName("should fall back to default for invalid period milliseconds format")
    void testPeriodMillisecondsInvalidFormat() {
        // Given: system property set to invalid period format
        System.setProperty(WatcherConfig.PROP_PERIOD, "invalid");

        // When: init() is called
        WatcherConfig.init();

        // Then: periodMilliseconds should use default and report error
        assertEquals(600000L, WatcherConfig.periodMilliseconds, "periodMilliseconds should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid periodMilliseconds format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains(String.format("Invalid time value for property '%s", WatcherConfig.PROP_PERIOD)));
    }

    @Test
    @DisplayName("should parse data prefix from system property")
    void testDataPrefixProperty() {
        // Given: system property set to data prefix value
        System.setProperty(WatcherConfig.PROP_DATA_PREFIX, "data.");

        // When: init() is called
        WatcherConfig.init();

        // Then: dataPrefix should reflect the property value with no errors
        assertEquals("data.", WatcherConfig.dataPrefix, "dataPrefix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid dataPrefix");
    }

    @Test
    @DisplayName("should parse data suffix from system property")
    void testDataSuffixProperty() {
        // Given: system property set to data suffix value
        System.setProperty(WatcherConfig.PROP_DATA_SUFFIX, ".data");

        // When: init() is called
        WatcherConfig.init();

        // Then: dataSuffix should reflect the property value with no errors
        assertEquals(".data", WatcherConfig.dataSuffix, "dataSuffix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid dataSuffix");
    }

    @Test
    @DisplayName("should parse data enabled from system property")
    void testDataEnabledProperty() {
        // Given: system property set to enable data
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "true");

        // When: init() is called
        WatcherConfig.init();

        // Then: dataEnabled should be true with no errors
        assertTrue(WatcherConfig.dataEnabled, "dataEnabled should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid dataEnabled");
    }

    @Test
    @DisplayName("should fall back to default for invalid data enabled format")
    void testDataEnabledInvalidFormat() {
        // Given: system property set to invalid boolean format
        System.setProperty(WatcherConfig.PROP_DATA_ENABLED, "invalid");

        // When: init() is called
        WatcherConfig.init();

        // Then: dataEnabled should use default and report error
        assertFalse(WatcherConfig.dataEnabled, "dataEnabled should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid dataEnabled format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains(String.format("Invalid boolean value for property '%s", WatcherConfig.PROP_DATA_ENABLED)));
    }

    @Test
    @DisplayName("should parse message prefix from system property")
    void testMessagePrefixProperty() {
        // Given: system property set to message prefix value
        System.setProperty(WatcherConfig.PROP_MESSAGE_PREFIX, "message.");

        // When: init() is called
        WatcherConfig.init();

        // Then: messagePrefix should reflect the property value with no errors
        assertEquals("message.", WatcherConfig.messagePrefix, "messagePrefix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid messagePrefix");
    }

    @Test
    @DisplayName("should parse message suffix from system property")
    void testMessageSuffixProperty() {
        // Given: system property set to message suffix value
        System.setProperty(WatcherConfig.PROP_MESSAGE_SUFFIX, ".message");

        // When: init() is called
        WatcherConfig.init();

        // Then: messageSuffix should reflect the property value with no errors
        assertEquals(".message", WatcherConfig.messageSuffix, "messageSuffix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid messageSuffix");
    }
}
