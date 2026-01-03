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

package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.ClearParserErrors;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MeterConfig}.
 * <p>
 * Tests validate that MeterConfig correctly parses system properties and provides
 * appropriate default values and error handling for invalid inputs.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Default Values:</b> Verifies all configuration properties have correct defaults</li>
 *   <li><b>Property Parsing:</b> Tests parsing of all system properties with valid values</li>
 *   <li><b>Error Handling:</b> Validates fallback to defaults and error reporting for invalid property values</li>
 *   <li><b>Reset Functionality:</b> Ensures reset() restores all properties to defaults</li>
 * </ul>
 */
@DisplayName("MeterConfig")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
class MeterConfigTest {

    /**
     * Tests that MeterConfig initializes with correct default values.
     */
    @Test
    @DisplayName("should have correct default values")
    void testDefaultValues() {
        MeterConfig.init();
        assertFalse(MeterConfig.printCategory, "Default value for printCategory should be false");
        assertTrue(MeterConfig.printStatus, "Default value for printStatus should be true");
        assertFalse(MeterConfig.printPosition, "Default value for printPosition should be false");
        assertFalse(MeterConfig.printLoad, "Default value for printLoad should be false");
        assertFalse(MeterConfig.printMemory, "Default value for printMemory should be false");
        assertEquals("", MeterConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", MeterConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertEquals("", MeterConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", MeterConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for default values");
    }

    /**
     * Tests that MeterConfig reset restores all properties to their default values.
     */
    @Test
    @DisplayName("should reset to default values")
    void testResetValues() {
        MeterConfig.reset();
        assertEquals(2000L, MeterConfig.progressPeriodMilliseconds, "Default value for progressPeriodMilliseconds should be 2000ms");
        assertFalse(MeterConfig.printCategory, "Default value for printCategory should be false");
        assertTrue(MeterConfig.printStatus, "Default value for printStatus should be true");
        assertFalse(MeterConfig.printPosition, "Default value for printPosition should be false");
        assertFalse(MeterConfig.printLoad, "Default value for printLoad should be false");
        assertFalse(MeterConfig.printMemory, "Default value for printMemory should be false");
        assertEquals("", MeterConfig.dataPrefix, "Default value for dataPrefix should be an empty string");
        assertEquals("", MeterConfig.dataSuffix, "Default value for dataSuffix should be an empty string");
        assertEquals("", MeterConfig.messagePrefix, "Default value for messagePrefix should be an empty string");
        assertEquals("", MeterConfig.messageSuffix, "Default value for messageSuffix should be an empty string");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported after reset");
    }


    /**
     * Tests that progressPeriodMilliseconds property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse progressPeriodMilliseconds property correctly")
    void testProgressPeriodMillisecondsProperty() {
        System.setProperty(MeterConfig.PROP_PROGRESS_PERIOD, "5000");
        MeterConfig.init();
        assertEquals(5000L, MeterConfig.progressPeriodMilliseconds, "progressPeriodMilliseconds should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid progressPeriodMilliseconds");
    }

    /**
     * Tests that invalid progressPeriodMilliseconds property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid progressPeriodMilliseconds format")
    void testProgressPeriodMillisecondsInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PROGRESS_PERIOD, "invalid");
        MeterConfig.init();
        assertEquals(2000L, MeterConfig.progressPeriodMilliseconds, "progressPeriodMilliseconds should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid progressPeriodMilliseconds format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid time value for property '" + MeterConfig.PROP_PROGRESS_PERIOD));
    }

    /**
     * Tests that printCategory property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse printCategory property correctly")
    void testPrintCategoryProperty() {
        System.setProperty(MeterConfig.PROP_PRINT_CATEGORY, "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printCategory, "printCategory should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid printCategory");
    }

    /**
     * Tests that invalid printCategory property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid printCategory format")
    void testPrintCategoryInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PRINT_CATEGORY, "invalid");
        MeterConfig.init();
        assertFalse(MeterConfig.printCategory, "printCategory should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid printCategory format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + MeterConfig.PROP_PRINT_CATEGORY));
    }

    /**
     * Tests that printStatus property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse printStatus property correctly")
    void testPrintStatusProperty() {
        System.setProperty(MeterConfig.PROP_PRINT_STATUS, "false");
        MeterConfig.init();
        assertFalse(MeterConfig.printStatus, "printStatus should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid printStatus");
    }

    /**
     * Tests that invalid printStatus property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid printStatus format")
    void testPrintStatusInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PRINT_STATUS, "invalid");
        MeterConfig.init();
        assertTrue(MeterConfig.printStatus, "printStatus should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid printStatus format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + MeterConfig.PROP_PRINT_STATUS));
    }

    /**
     * Tests that printPosition property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse printPosition property correctly")
    void testPrintPositionProperty() {
        System.setProperty(MeterConfig.PROP_PRINT_POSITION, "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printPosition, "printPosition should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid printPosition");
    }

    /**
     * Tests that invalid printPosition property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid printPosition format")
    void testPrintPositionInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PRINT_POSITION, "invalid");
        MeterConfig.init();
        assertFalse(MeterConfig.printPosition, "printPosition should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid printPosition format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + MeterConfig.PROP_PRINT_POSITION));
    }

    /**
     * Tests that printLoad property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse printLoad property correctly")
    void testPrintLoadProperty() {
        System.setProperty(MeterConfig.PROP_PRINT_LOAD, "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printLoad, "printLoad should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid printLoad");
    }

    /**
     * Tests that invalid printLoad property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid printLoad format")
    void testPrintLoadInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PRINT_LOAD, "invalid");
        MeterConfig.init();
        assertFalse(MeterConfig.printLoad, "printLoad should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid printLoad format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + MeterConfig.PROP_PRINT_LOAD));
    }

    /**
     * Tests that printMemory property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse printMemory property correctly")
    void testPrintMemoryProperty() {
        System.setProperty(MeterConfig.PROP_PRINT_MEMORY, "true");
        MeterConfig.init();
        assertTrue(MeterConfig.printMemory, "printMemory should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid printMemory");
    }

    /**
     * Tests that invalid printMemory property falls back to default and reports error.
     */
    @Test
    @DisplayName("should handle invalid printMemory format")
    void testPrintMemoryInvalidFormat() {
        System.setProperty(MeterConfig.PROP_PRINT_MEMORY, "invalid");
        MeterConfig.init();
        assertFalse(MeterConfig.printMemory, "printMemory should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK(), "An error should be reported for invalid printMemory format");
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + MeterConfig.PROP_PRINT_MEMORY));
    }

    /**
     * Tests that dataPrefix property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse dataPrefix property correctly")
    void testDataPrefixProperty() {
        System.setProperty(MeterConfig.PROP_DATA_PREFIX, "data.");
        MeterConfig.init();
        assertEquals("data.", MeterConfig.dataPrefix, "dataPrefix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid dataPrefix");
    }

    /**
     * Tests that dataSuffix property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse dataSuffix property correctly")
    void testDataSuffixProperty() {
        System.setProperty(MeterConfig.PROP_DATA_SUFFIX, ".data");
        MeterConfig.init();
        assertEquals(".data", MeterConfig.dataSuffix, "dataSuffix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid dataSuffix");
    }

    /**
     * Tests that messagePrefix property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse messagePrefix property correctly")
    void testMessagePrefixProperty() {
        System.setProperty(MeterConfig.PROP_MESSAGE_PREFIX, "message.");
        MeterConfig.init();
        assertEquals("message.", MeterConfig.messagePrefix, "messagePrefix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid messagePrefix");
    }

    /**
     * Tests that messageSuffix property is correctly parsed from system property.
     */
    @Test
    @DisplayName("should parse messageSuffix property correctly")
    void testMessageSuffixProperty() {
        System.setProperty(MeterConfig.PROP_MESSAGE_SUFFIX, ".message");
        MeterConfig.init();
        assertEquals(".message", MeterConfig.messageSuffix, "messageSuffix should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid messageSuffix");
    }
}
