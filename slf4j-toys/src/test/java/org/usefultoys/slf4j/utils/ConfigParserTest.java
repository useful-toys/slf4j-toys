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

package org.usefultoys.slf4j.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
        System.clearProperty("test.property");
        System.clearProperty("nonexistent.property");
    }

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("test.property");
        System.clearProperty("nonexistent.property");
    }

    @ParameterizedTest
    @ValueSource(strings = {"value", " value", "value ", " value "})
    void testGetPropertyString(String value) {
        System.setProperty("test.property", value);
        assertEquals("value", ConfigParser.getProperty("test.property", "default"));
    }

    @Test
    void testGetPropertyStringNotFound() {
        assertEquals("default", ConfigParser.getProperty("nonexistent.property", "default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", " true", "true ", " true "})
    void testGetPropertyBoolean(String value) {
        System.setProperty("test.property", value);
        assertTrue(ConfigParser.getProperty("test.property", false));
    }

    @Test
    void testGetPropertyBooleanNotFound() {
        assertFalse(ConfigParser.getProperty("nonexistent.property", false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"42", " 42", "42 ", " 42 "})
    void testGetPropertyInt(String value) {
        System.setProperty("test.property", value);
        assertEquals(42, ConfigParser.getProperty("test.property", 0));
    }

    @Test
    void testGetPropertyIntInvalid() {
        System.setProperty("test.property", "invalid");
        assertEquals(0, ConfigParser.getProperty("test.property", 0));
        assertEquals(0, ConfigParser.getProperty("nonexistent.property", 0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", " 10", "10 ", " 10 "})
    void testGetRangeProperty(String value) {
        System.setProperty("test.property", value);
        assertEquals(10, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
    }

    @Test
    void testGetRangePropertyOutOfBounds() {
        System.setProperty("test.property", "4");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        System.setProperty("test.property", "16");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
    }

    @Test
    void testGetRangePropertyInvalid() {
        System.setProperty("test.property", "invalid");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        assertEquals(0, ConfigParser.getRangeProperty("nonexistent.property", 0, 5, 15));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", " 123456789", "123456789 ", " 123456789 "})
    void testGetPropertyLong(String value) {
        System.setProperty("test.property", value);
        assertEquals(123456789L, ConfigParser.getProperty("test.property", 0L));
    }

    @Test
    void testGetPropertyLongInvalid() {
        System.setProperty("test.property", "invalid");
        assertEquals(0L, ConfigParser.getProperty("test.property", 0L));
        assertEquals(0L, ConfigParser.getProperty("nonexistent.property", 0L));
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
    void testGetMillisecondsProperty(String input, long expected) {
        System.setProperty("test.property", input);
        assertEquals(expected, ConfigParser.getMillisecondsProperty("test.property", 0L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "s", "1.5s", ""})
    void testGetMillisecondsPropertyInvalid(String value) {
        System.setProperty("test.property", value);
        assertEquals(0L, ConfigParser.getMillisecondsProperty("test.property", 0L));
    }

    @Test
    void testGetMillisecondsPropertyNotFound() {
        assertEquals(0L, ConfigParser.getMillisecondsProperty("nonexistent.property", 0L));
    }
}
