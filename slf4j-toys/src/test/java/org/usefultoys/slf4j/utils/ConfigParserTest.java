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
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("test.property");
    }

    @Test
    void testGetPropertyString() {
        System.setProperty("test.property", "value");
        assertEquals("value", ConfigParser.getProperty("test.property", "default"));
        assertEquals("default", ConfigParser.getProperty("nonexistent.property", "default"));
    }

    @Test
    void testGetPropertyBoolean() {
        System.setProperty("test.property", "true");
        assertTrue(ConfigParser.getProperty("test.property", false));
        assertFalse(ConfigParser.getProperty("nonexistent.property", false));
    }

    @Test
    void testGetPropertyInt() {
        System.setProperty("test.property", "42");
        assertEquals(42, ConfigParser.getProperty("test.property", 0));
        assertEquals(0, ConfigParser.getProperty("nonexistent.property", 0));
        System.setProperty("test.property", "invalid");
        assertEquals(0, ConfigParser.getProperty("test.property", 0));
    }

    @Test
    void testGetPropertyLong() {
        System.setProperty("test.property", "123456789");
        assertEquals(123456789L, ConfigParser.getProperty("test.property", 0L));
        System.setProperty("test.property", "invalid");
        assertEquals(0L, ConfigParser.getProperty("test.property", 0L));
    }

    @Test
    void testGetPropertyLongNotFound() {
        assertEquals(0L, ConfigParser.getProperty("nonexistent.property", 0L));
    }

    @Test
    void testGetMillisecondsProperty() {
        System.setProperty("test.property", "10");
        assertEquals(10L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "10ms");
        assertEquals(10L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "10s");
        assertEquals(10000L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "5min");
        assertEquals(300000L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "5m");
        assertEquals(300000L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "1h");
        assertEquals(3600000L, ConfigParser.getMillisecondsProperty("test.property", 0L));
        System.setProperty("test.property", "invalid");
        assertEquals(0L, ConfigParser.getMillisecondsProperty("test.property", 0L));
    }

    @Test
    void testGetMillisecondsPropertyNotFound() {
        assertEquals(0L, ConfigParser.getMillisecondsProperty("nonexistent.property", 0L));
    }
}
