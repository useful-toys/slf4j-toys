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
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetSessionConfigExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({ResetSessionConfigExtension.class, CharsetConsistencyExtension.class})
class SessionTest {

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
        assertEquals(SessionConfig.uuidSize, Session.shortSessionUuid().length(), "shortSessionUuid() should return a string of length " + SessionConfig.uuidSize);
    }

    @Test
    void testShortSessionUuidWithNonDefaultSize() {
        SessionConfig.uuidSize = 10;
        assertNotNull(Session.shortSessionUuid(), "shortSessionUuid() should not return null");
        assertTrue(Session.uuid.endsWith(Session.shortSessionUuid()));
        assertEquals(10, Session.shortSessionUuid().length(), "shortSessionUuid() should return a string of length " + SessionConfig.uuidSize);
    }
}
