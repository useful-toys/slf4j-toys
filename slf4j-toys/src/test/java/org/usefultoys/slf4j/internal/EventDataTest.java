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
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.*;

@WithLocale("en")
@ExtendWith(CharsetConsistency.class)
class EventDataTest {

    private static final String FIXED_UUID = "12345"; // Fixed UUID as requested

    @Test
    @DisplayName("Constructor should initialize fields correctly with default values")
    void testConstructorAndGetters0() {
        final EventData event = new EventData();
        assertNull(event.getSessionUuid(), "Session UUID should be null by default");
        assertEquals(0L, event.getPosition(), "Position should be 0 by default");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default");
    }

    @Test
    @DisplayName("Constructor should initialize sessionUuid correctly")
    void testConstructorAndGetters1() {
        final EventData event = new EventData("abc");
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(0L, event.getPosition(), "Position should be 0 by default when only UUID is provided");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default when only UUID is provided");
    }

    @Test
    @DisplayName("Constructor should initialize sessionUuid and position correctly")
    void testConstructorAndGetters2() {
        final EventData event = new EventData("abc", 5L);
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(5L, event.getPosition(), "Position should be initialized correctly");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default when UUID and position are provided");
    }

    @Test
    @DisplayName("Constructor should initialize all fields correctly")
    void testConstructorAndGetters3() {
        final EventData event = new EventData("abc", 5L, 10L);
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(5L, event.getPosition(), "Position should be initialized correctly");
        assertEquals(10L, event.getLastCurrentTime(), "Last current time should be initialized correctly");
    }

    @Test
    @DisplayName("collectCurrentTime should update lastCurrentTime with current nano time")
    void testCollectCurrentTime() {
        final EventData event = new EventData();
        long startTime = System.nanoTime();
        event.collectCurrentTime();
        long endTime = System.nanoTime();
        assertTrue(event.getLastCurrentTime() >= startTime, "Last current time should be greater than or equal to start time");
        assertTrue(event.getLastCurrentTime() <= endTime, "Last current time should be less than or equal to end time");
    }

    @Test
    @DisplayName("reset should clear all fields to their default values")
    void testResetClearsFields() {
        final EventData event = new EventData("abc", 5L, 10L);
        event.reset();
        assertNull(event.getSessionUuid(), "Session UUID should be null after reset");
        assertEquals(0L, event.getPosition(), "Position should be 0 after reset");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 after reset");
    }

    @Test
    @DisplayName("nextPosition should increment position normally")
    void testNextPosition_incrementNormal() {
        final EventData event = new EventData("test", 10L);
        event.nextPosition();
        assertEquals(11L, event.getPosition(), "Position should increment by 1");
    }

    @Test
    @DisplayName("nextPosition should reset position to 0 when Long.MAX_VALUE is reached")
    void testNextPosition_overflowResetsToZero() {
        final EventData event = new EventData("test", Long.MAX_VALUE);
        event.nextPosition();
        assertEquals(0L, event.getPosition(), "Position should reset to 0 after reaching Long.MAX_VALUE");
    }

    @Test
    @DisplayName("read/write json5 should preserve event data")
    void testReadWriteJson5() {
        final EventData event = new EventData(FIXED_UUID, 123L, 456L);
        final StringBuilder sb = new StringBuilder();
        EventDataJson5.write(event, sb);
        final EventData event2 = new EventData();
        EventDataJson5.read(event2, "{" + sb.toString() + "}");
        assertEquals(event.getSessionUuid(), event2.getSessionUuid(), "Session UUID should be preserved after JSON5 write/read");
        assertEquals(event.getPosition(), event2.getPosition(), "Position should be preserved after JSON5 write/read");
        assertEquals(event.getLastCurrentTime(), event2.getLastCurrentTime(), "Last current time should be preserved after JSON5 write/read");
    }
}
