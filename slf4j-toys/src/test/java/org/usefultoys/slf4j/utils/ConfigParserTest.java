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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.nio.charset.Charset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import org.usefultoys.slf4j.SessionConfig;


class ConfigParserTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        ConfigParser.clearInitializationErrors();
        System.clearProperty("test.property");
    }

    @AfterEach
    void tearDown() {
        ConfigParser.clearInitializationErrors();
        System.clearProperty("test.property");
    }

    @ParameterizedTest
    @ValueSource(strings = {"value", " value", "value ", " value "})
    void testGetPropertyString(String value) {
        System.setProperty("test.property", value);
        assertEquals("value", ConfigParser.getProperty("test.property", "default"));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetPropertyStringNotFound() {
        assertEquals("default", ConfigParser.getProperty("nonexistent.property", "default"));
        assertTrue(ConfigParser.isInitializationOK());
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
    void testGetPropertyBooleanValid(String input, boolean expected) {
        System.setProperty("test.property", input);
        assertEquals(expected, ConfigParser.getProperty("test.property", !expected));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetPropertyBooleanInvalidFormat() {
        System.setProperty("test.property", "abc");
        assertTrue(ConfigParser.getProperty("test.property", true));
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value"));
    }

    @Test
    void testGetPropertyBooleanNotFound() {
        assertFalse(ConfigParser.getProperty("nonexistent.property", false));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @ParameterizedTest
    @ValueSource(strings = {"42", " 42", "42 ", " 42 "})
    void testGetPropertyInt(String value) {
        System.setProperty("test.property", value);
        assertEquals(42, ConfigParser.getProperty("test.property", 0));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetPropertyIntInvalidFormat() {
        System.setProperty("test.property", "invalid");
        assertEquals(0, ConfigParser.getProperty("test.property", 0));
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid integer value"));
    }

    @Test
    void testGetPropertyIntNotFound() {
        assertEquals(0, ConfigParser.getProperty("nonexistent.property", 0));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", " 10", "10 ", " 10 "})
    void testGetRangeProperty(String value) {
        System.setProperty("test.property", value);
        assertEquals(10, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetRangePropertyOutOfBounds() {
        System.setProperty("test.property", "4");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("out of range"));

        ConfigParser.clearInitializationErrors();

        System.setProperty("test.property", "16");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("out of range"));
    }

    @Test
    void testGetRangePropertyInvalidFormat() {
        System.setProperty("test.property", "invalid");
        assertEquals(0, ConfigParser.getRangeProperty("test.property", 0, 5, 15));
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid integer value"));
    }

    @Test
    void testGetRangePropertyNotFound() {
        assertEquals(0, ConfigParser.getRangeProperty("nonexistent.property", 0, 5, 15));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", " 123456789", "123456789 ", " 123456789 "})
    void testGetPropertyLong(String value) {
        System.setProperty("test.property", value);
        assertEquals(123456789L, ConfigParser.getProperty("test.property", 0L));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetPropertyLongInvalidFormat() {
        System.setProperty("test.property", "invalid");
        assertEquals(0L, ConfigParser.getProperty("test.property", 0L));
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid long value"));
    }

    @Test
    void testGetPropertyLongNotFound() {
        assertEquals(0L, ConfigParser.getProperty("nonexistent.property", 0L));
        assertTrue(ConfigParser.isInitializationOK());
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
        assertTrue(ConfigParser.isInitializationOK());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "s", "1.5s"})
    void testGetMillisecondsPropertyInvalidFormat(String value) {
        System.setProperty("test.property", value);
        assertEquals(0L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid time value"));
    }

    @Test
    void testGetMillisecondsPropertyEmpty() {
        System.setProperty("test.property", "");
        assertEquals(0L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testGetMillisecondsPropertyNotFound() {
        assertEquals(0L, ConfigParser.getMillisecondsProperty("nonexistent.property", 0L));
        assertTrue(ConfigParser.isInitializationOK());
    }
}
