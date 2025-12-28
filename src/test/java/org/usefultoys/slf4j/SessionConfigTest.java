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

package org.usefultoys.slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SessionConfig}.
 * <p>
 * Tests validate that SessionConfig correctly parses and applies system properties,
 * with proper error handling for invalid values and correct defaults.
 */
@ValidateCharset
@ResetSessionConfig
class SessionConfigTest {

    @Test
    @DisplayName("should have correct default values on init")
    void shouldHaveCorrectDefaultValuesOnInit() {
        // Given: SessionConfig not yet initialized
        // When: init() is called
        SessionConfig.init();
        // Then: should have default values
        assertEquals(5, SessionConfig.uuidSize, "should have default uuidSize of 5");
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "should have default charset");
    }

    @Test
    @DisplayName("should reset to default values")
    void shouldResetToDefaultValues() {
        // Given: SessionConfig with custom values
        // When: reset() is called
        SessionConfig.reset();
        // Then: should return to defaults
        assertEquals(5, SessionConfig.uuidSize, "should reset uuidSize to default 5");
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "should reset charset to default");
    }

    @Test
    @DisplayName("should parse uuidSize property correctly")
    void shouldParseUuidSizePropertyCorrectly() {
        // Given: system property PROP_PRINT_UUID_SIZE set to "10"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "10");
        // When: init() is called
        SessionConfig.init();
        // Then: uuidSize should reflect the system property value
        assertEquals(10, SessionConfig.uuidSize, "should parse uuidSize from system property");
    }

    @Test
    @DisplayName("should parse uuidSize property with non-default value")
    void shouldParseUuidSizePropertyWithNonDefaultValue() {
        // Given: system property PROP_PRINT_UUID_SIZE set to "15"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "15");
        // When: init() is called
        SessionConfig.init();
        // Then: uuidSize should reflect the system property value
        assertEquals(15, SessionConfig.uuidSize, "should parse uuidSize from system property");
    }

    @Test
    @DisplayName("should accept uuidSize within bounds")
    void shouldAcceptUuidSizeWithinBounds() {
        // Given: system property set to lower bound value "0"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "0");
        // When: init() is called
        SessionConfig.init();
        // Then: should accept the value
        assertEquals(0, SessionConfig.uuidSize, "should accept uuidSize of 0");

        // Given: system property set to upper bound value "32"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "32");
        // When: init() is called again
        SessionConfig.init();
        // Then: should accept the value
        assertEquals(32, SessionConfig.uuidSize, "should accept uuidSize of 32");
    }

    @Test
    @DisplayName("should use default when uuidSize is out of bounds")
    void shouldUseDefaultWhenUuidSizeIsOutOfBounds() {
        // Given: system property set below lower bound "-1"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "-1");
        // When: init() is called
        SessionConfig.init();
        // Then: should fall back to default
        assertEquals(5, SessionConfig.uuidSize, "should fall back to default for values below range");

        // Given: system property set above upper bound "33"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "33");
        // When: init() is called again
        SessionConfig.init();
        // Then: should fall back to default
        assertEquals(5, SessionConfig.uuidSize, "should fall back to default for values above range");
    }

    @Test
    @DisplayName("should use default when uuidSize has invalid format")
    void shouldUseDefaultWhenUuidSizeHasInvalidFormat() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "invalid");
        // When: init() is called
        SessionConfig.init();
        // Then: should fall back to default
        assertEquals(5, SessionConfig.uuidSize, "should fall back to default for invalid values");
    }

    @Test
    @DisplayName("should parse charset property correctly")
    void shouldParseCharsetPropertyCorrectly() {
        // Given: system property PROP_PRINT_CHARSET set to "ISO-8859-1"
        System.setProperty(SessionConfig.PROP_PRINT_CHARSET, "ISO-8859-1");
        // When: init() is called
        SessionConfig.init();
        // Then: charset should reflect the system property value
        assertEquals("ISO-8859-1", SessionConfig.charset, "should parse charset from system property");
    }
}
