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


class SessionTest {

    @BeforeAll
    static void validateConsistentCharset() {
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
    void testUuidIsNotNull() {
        assertNotNull(Session.uuid, "Session UUID should not be null");
    }

    @Test
    void testUuidIsImmutable() {
        final String firstUuid = Session.uuid;
        final String secondUuid = Session.uuid;
        assertEquals(firstUuid, secondUuid, "Session UUID should remain constant");
    }

    @Test
    void testUuidFormat() {
        assertTrue(Session.uuid.matches("^[a-f0-9]{32}$"), "Session UUID should be a 32-character hexadecimal string");
    }

    @Test
    void testShortSessionUudi() {
        assertNotNull(Session.shortSessionUuid(), "shortSessionUuid() should not return null");
        assertTrue(Session.uuid.endsWith(Session.shortSessionUuid()));
    }
}
