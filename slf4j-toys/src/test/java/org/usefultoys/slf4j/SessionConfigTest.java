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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetSessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith({ResetSessionConfig.class, CharsetConsistency.class})
class SessionConfigTest {
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
    void testUuidSizePropertyWithNonDefaultValue() {
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "15");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals(15, SessionConfig.uuidSize, "uuidSize should reflect the system property value");
    }

    @Test
    void testUuidSizePropertyWithinBounds() {
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "0");
        SessionConfig.init();
        assertEquals(0, SessionConfig.uuidSize, "uuidSize should be 0");

        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "32");
        SessionConfig.init();
        assertEquals(32, SessionConfig.uuidSize, "uuidSize should be 32");
    }

    @Test
    void testUuidSizePropertyOutOfBounds() {
        // Below lower bound
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "-1");
        SessionConfig.init();
        assertEquals(5, SessionConfig.uuidSize, "uuidSize should fall back to default for values below range");

        // Above upper bound
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "33");
        SessionConfig.init();
        assertEquals(5, SessionConfig.uuidSize, "uuidSize should fall back to default for values above range");
    }

    @Test
    void testUuidSizePropertyInvalid() {
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "invalid");
        SessionConfig.init();
        assertEquals(5, SessionConfig.uuidSize, "uuidSize should fall back to default for invalid values");
    }

    @Test
    public void testCharsetProperty() {
        System.setProperty(SessionConfig.PROP_PRINT_CHARSET, "ISO-8859-1");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals("ISO-8859-1", SessionConfig.charset, "charset should reflect the system property value");
    }
}
