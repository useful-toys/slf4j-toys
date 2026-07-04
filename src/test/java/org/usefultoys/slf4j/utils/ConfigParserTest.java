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

package org.usefultoys.slf4j.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.usefultoys.test.ClearParserErrors;
import org.usefultoys.test.ResetSystemProperty;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ConfigParser}.
 * <p>
 * Tests validate that ConfigParser correctly parses and applies various property types,
 * with proper error handling for invalid values and correct defaults.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>String Properties:</b> Tests parsing of string properties with whitespace handling</li>
 *   <li><b>Boolean Properties:</b> Verifies parsing of boolean values with true/false and invalid formats</li>
 *   <li><b>Numeric Properties:</b> Covers parsing of int, long, and double properties with bounds and invalid formats</li>
 *   <li><b>Default Values:</b> Ensures correct fallback to default values when properties are missing or invalid</li>
 *   <li><b>Error Handling:</b> Validates error reporting for invalid property values</li>
 * </ul>
 */
@ValidateCharset
@ClearParserErrors
@WithLocale("en")
@ResetSystemProperty("test.property")
class ConfigParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"value", " value", "value ", " value "})
    @DisplayName("should parse string property correctly with various whitespace")
    void shouldParseStringPropertyCorrectlyWithVariousWhitespace(final String value) {
        // Given: system property set with whitespace
        System.setProperty("test.property", value);
        // When: property is retrieved
        final String result = ConfigParser.getProperty("test.property", "default");
        // Then: should return trimmed value
        assertEquals("value", result, "should return trimmed value");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @Test
    @DisplayName("should return default value when string property not found")
    void shouldReturnDefaultWhenStringPropertyNotFound() {
        // Given: property not set
        // When: property is retrieved with default
        final String result = ConfigParser.getProperty("nonexistent.property", "default");
        // Then: should return default value
        assertEquals("default", result, "should return default value");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "TRUE, true",
            " true , true",
            "false, false",
            "FALSE, false",
            " false , false"
    })
    @DisplayName("should parse boolean property correctly")
    void shouldParseBooleanPropertyCorrectly(final String input, final boolean expected) {
        // Given: system property set with boolean value
        System.setProperty("test.property", input);
        // When: boolean property is retrieved
        final boolean result = ConfigParser.getProperty("test.property", !expected);
        // Then: should return correct boolean value
        assertEquals(expected, result, "should return correct boolean value");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @Test
    @DisplayName("should report error when boolean property has invalid format")
    void shouldReportErrorWhenBooleanPropertyInvalid() {
        // Given: system property set to invalid boolean value
        System.setProperty("test.property", "abc");
        // When: boolean property is retrieved
        final boolean result = ConfigParser.getProperty("test.property", true);
        // Then: should return default and report error
        assertTrue(result, "should return default value true");
        assertFalse(ConfigParser.isInitializationOK(), "should report initialization error");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value"), "should report invalid boolean error");
    }

    @Test
    @DisplayName("should return default value when boolean property not found")
    void shouldReturnDefaultWhenBooleanPropertyNotFound() {
        // Given: property not set
        // When: boolean property is retrieved with default
        final boolean result = ConfigParser.getProperty("nonexistent.property", false);
        // Then: should return default value
        assertFalse(result, "should return default value false");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @ValueSource(strings = {"42", " 42", "42 ", " 42 "})
    @DisplayName("should parse integer property correctly")
    void shouldParseIntegerPropertyCorrectly(final String value) {
        // Given: system property set with integer value
        System.setProperty("test.property", value);
        // When: integer property is retrieved
        final int result = ConfigParser.getProperty("test.property", 0);
        // Then: should return correct integer value
        assertEquals(42, result, "should return integer value 42");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @Test
    @DisplayName("should report error when integer property has invalid format")
    void shouldReportErrorWhenIntegerPropertyInvalid() {
        // Given: system property set to invalid integer value
        System.setProperty("test.property", "invalid");
        // When: integer property is retrieved
        final int result = ConfigParser.getProperty("test.property", 0);
        // Then: should return default and report error
        assertEquals(0, result, "should return default value 0");
        assertFalse(ConfigParser.isInitializationOK(), "should report initialization error");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid integer value"), "should report invalid integer error");
    }

    @Test
    @DisplayName("should return default value when integer property not found")
    void shouldReturnDefaultWhenIntegerPropertyNotFound() {
        // Given: property not set
        // When: integer property is retrieved with default
        final int result = ConfigParser.getProperty("nonexistent.property", 0);
        // Then: should return default value
        assertEquals(0, result, "should return default value 0");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", " 10", "10 ", " 10 "})
    @DisplayName("should parse range property correctly")
    void shouldParseRangePropertyCorrectly(final String value) {
        // Given: system property set with value within range
        System.setProperty("test.property", value);
        // When: range property is retrieved
        final int result = ConfigParser.getRangeProperty("test.property", 0, 5, 15);
        // Then: should return correct value
        assertEquals(10, result, "should return value 10");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @CsvSource({
            "5, 5",
            "10, 10",
            "15, 15"
    })
    @DisplayName("should accept range property values within bounds")
    void shouldAcceptRangePropertyValuesWithinBounds(final String input, final int expected) {
        // Given: system property set to a value within range
        System.setProperty("test.property", input);
        // When: range property is retrieved (min=5, max=15)
        final int result = ConfigParser.getRangeProperty("test.property", 0, 5, 15);
        // Then: should return the parsed value without errors
        assertEquals(expected, result, "should return value " + input);
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors for value " + input);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 5",
            "0, 5",
            "-100, 5"
    })
    @DisplayName("should clamp range property below minimum and report error")
    void shouldClampRangePropertyBelowMinimumAndReportError(final String input, final int expected) {
        // Given: system property set to a value below the minimum
        System.setProperty("test.property", input);
        // When: range property is retrieved (min=5, max=15)
        final int result = ConfigParser.getRangeProperty("test.property", 0, 5, 15);
        // Then: should clamp to the minimum and report an error
        assertEquals(expected, result, "should clamp value " + input + " to minimum " + expected);
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error for value " + input);
        assertTrue(ConfigParser.initializationErrors.get(0).contains("below minimum"), "should report below-minimum error");
    }

    @ParameterizedTest
    @CsvSource({
            "16, 15",
            "100, 15"
    })
    @DisplayName("should clamp range property above maximum and report error")
    void shouldClampRangePropertyAboveMaximumAndReportError(final String input, final int expected) {
        // Given: system property set to a value above the maximum
        System.setProperty("test.property", input);
        // When: range property is retrieved (min=5, max=15)
        final int result = ConfigParser.getRangeProperty("test.property", 0, 5, 15);
        // Then: should clamp to the maximum and report an error
        assertEquals(expected, result, "should clamp value " + input + " to maximum " + expected);
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error for value " + input);
        assertTrue(ConfigParser.initializationErrors.get(0).contains("above maximum"), "should report above-maximum error");
    }

    @Test
    @DisplayName("should report error when range property has invalid format")
    void shouldReportErrorWhenRangePropertyInvalid() {
        // Given: system property set to invalid value
        System.setProperty("test.property", "invalid");
        // When: range property is retrieved
        final int result = ConfigParser.getRangeProperty("test.property", 0, 5, 15);
        // Then: should return default and report error
        assertEquals(0, result, "should return default value 0");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid integer value"), "should report invalid integer error");
    }

    @Test
    @DisplayName("should return default value when range property not found")
    void shouldReturnDefaultWhenRangePropertyNotFound() {
        // Given: property not set
        // When: range property is retrieved with default
        final int result = ConfigParser.getRangeProperty("nonexistent.property", 0, 5, 15);
        // Then: should return default value
        assertEquals(0, result, "should return default value 0");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "100", "-5"})
    @DisplayName("should return default and report error when range is inverted")
    void shouldReturnDefaultAndReportErrorWhenRangeIsInverted(final String input) {
        // Given: system property set with inverted range (min > max)
        System.setProperty("test.property", input);
        // When: range property is retrieved with inverted range
        final int result = ConfigParser.getRangeProperty("test.property", 0, 15, 5);
        // Then: should return default and report error
        assertEquals(0, result, "should return default value when range is inverted");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error for inverted range");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid range"), "should report invalid range error");
    }

    @Test
    @DisplayName("should return default and report error when range is inverted and property not set")
    void shouldReturnDefaultAndReportErrorWhenRangeIsInvertedAndPropertyNotSet() {
        // Given: property not set and inverted range
        // When: range property is retrieved with inverted range
        final int result = ConfigParser.getRangeProperty("nonexistent.property", 0, 15, 5);
        // Then: should return default and report error
        assertEquals(0, result, "should return default value when range is inverted and property not set");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error for inverted range");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid range"), "should report invalid range error");
    }

    @Test
    @DisplayName("should accept range property when min equals max")
    void shouldAcceptRangePropertyWhenMinEqualsMax() {
        // Given: system property set to value equal to min and max
        System.setProperty("test.property", "10");
        // When: range property is retrieved with min equals max
        final int result = ConfigParser.getRangeProperty("test.property", 0, 10, 10);
        // Then: should return the value without errors
        assertEquals(10, result, "should return value equal to min and max");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors when min equals max");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", " 123456789", "123456789 ", " 123456789 "})
    @DisplayName("should parse long property correctly")
    void shouldParseLongPropertyCorrectly(final String value) {
        // Given: system property set with long value
        System.setProperty("test.property", value);
        // When: long property is retrieved
        final long result = ConfigParser.getProperty("test.property", 0L);
        // Then: should return correct long value
        assertEquals(123456789L, result, "should return long value 123456789");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @Test
    @DisplayName("should report error when long property has invalid format")
    void shouldReportErrorWhenLongPropertyInvalid() {
        // Given: system property set to invalid long value
        System.setProperty("test.property", "invalid");
        // When: long property is retrieved
        final long result = ConfigParser.getProperty("test.property", 0L);
        // Then: should return default and report error
        assertEquals(0L, result, "should return default value 0");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid long value"), "should report invalid long error");
    }

    @Test
    @DisplayName("should return default value when long property not found")
    void shouldReturnDefaultWhenLongPropertyNotFound() {
        // Given: property not set
        // When: long property is retrieved with default
        final long result = ConfigParser.getProperty("nonexistent.property", 0L);
        // Then: should return default value
        assertEquals(0L, result, "should return default value 0");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @CsvSource({
            "10, 10",
            "10ms, 10",
            "10s, 10000",
            " 10s , 10000",
            "5min, 300000",
            "5m, 300000",
            "1h, 3600000",
            "5MIN, 300000",
            "1H, 3600000",
            "-10s, -10000"
    })
    @DisplayName("should parse milliseconds property correctly")
    void shouldParseMillisecondsPropertyCorrectly(final String input, final long expected) {
        // Given: system property set with time value
        System.setProperty("test.property", input);
        // When: milliseconds property is retrieved
        final long result = ConfigParser.getMillisecondsProperty("test.property", 0L);
        // Then: should return correct milliseconds value
        assertEquals(expected, result, "should return milliseconds value " + expected);
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "s", "1.5s"})
    @DisplayName("should report error when milliseconds property has invalid format")
    void shouldReportErrorWhenMillisecondsPropertyInvalid(final String value) {
        // Given: system property set to invalid time value
        System.setProperty("test.property", value);
        // When: milliseconds property is retrieved
        final long result = ConfigParser.getMillisecondsProperty("test.property", 0L);
        // Then: should return default and report error
        assertEquals(0L, result, "should return default value 0");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid time value"), "should report invalid time error");
    }

    @ParameterizedTest
    @CsvSource({
            "9223372036854775807ms, 9223372036854775807",
            "9223372036854775s, 9223372036854775000",
            "153722867280912m, 9223372036854720000",
            "2562047788015h, 9223372036854000000"
    })
    @DisplayName("should parse milliseconds property at boundary values")
    void shouldParseMillisecondsPropertyAtBoundaryValues(final String input, final long expected) {
        // Given: system property set with maximum representable time value
        System.setProperty("test.property", input);
        // When: milliseconds property is retrieved
        final long result = ConfigParser.getMillisecondsProperty("test.property", 0L);
        // Then: should return correct boundary value without errors
        assertEquals(expected, result, "should return boundary value " + expected);
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @ParameterizedTest
    @ValueSource(strings = {"9223372036854776s", "153722867280913m", "2562047788016h", "-9223372036854775808s"})
    @DisplayName("should report error when milliseconds property overflows")
    void shouldReportErrorWhenMillisecondsPropertyOverflows(final String value) {
        // Given: system property set to time value that overflows when converted to milliseconds
        System.setProperty("test.property", value);
        // When: milliseconds property is retrieved
        final long result = ConfigParser.getMillisecondsProperty("test.property", 0L);
        // Then: should return default and report overflow error
        assertEquals(0L, result, "should return default value 0");
        assertFalse(ConfigParser.isInitializationOK(), "should report initialization error");
        assertEquals(1, ConfigParser.initializationErrors.size(), "should have one error");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Time value overflow"), "should report overflow error");
    }

    @Test
    @DisplayName("should return default value when milliseconds property is empty")
    void shouldReturnDefaultWhenMillisecondsPropertyEmpty() {
        // Given: system property set to empty string
        System.setProperty("test.property", "");
        // When: milliseconds property is retrieved
        final long result = ConfigParser.getMillisecondsProperty("test.property", 0L);
        // Then: should return default value
        assertEquals(0L, result, "should return default value 0");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }

    @Test
    @DisplayName("should return default value when milliseconds property not found")
    void shouldReturnDefaultWhenMillisecondsPropertyNotFound() {
        // Given: property not set
        // When: milliseconds property is retrieved with default
        final long result = ConfigParser.getMillisecondsProperty("nonexistent.property", 0L);
        // Then: should return default value
        assertEquals(0L, result, "should return default value 0");
        assertTrue(ConfigParser.isInitializationOK(), "should have no initialization errors");
    }
}
