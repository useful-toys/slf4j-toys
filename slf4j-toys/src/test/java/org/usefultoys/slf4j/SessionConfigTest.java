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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class SessionConfigTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        // Reinitialize WatcherConfig to ensure clean configuration before each test
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void tearDown() {
        // Reinitialize WatcherConfig to ensure clean configuration for further tests
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @Test
    void testDefaultValues() {
        SessionConfig.init();
        assertEquals(5, SessionConfig.uuidSize);
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset);
    }

    @Test
    void testResetValues() {
        SessionConfig.reset();
        assertEquals(5, SessionConfig.uuidSize);
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset);
    }

    @Test
    void testUuidSizeProperty() {
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "10");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals(10, SessionConfig.uuidSize, "uuidSize should reflect the system property value");
    }

    @Test
    public void testCharsetProperty() {
        System.setProperty(SessionConfig.PROP_PRINT_CHARSET, "ISO-8859-1");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals("ISO-8859-1", SessionConfig.charset, "charset should reflect the system property value");
    }
}
