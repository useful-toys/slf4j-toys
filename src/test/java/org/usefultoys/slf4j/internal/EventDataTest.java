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
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EventData}.
 * <p>
 * Tests verify that EventData correctly initializes, manages, and resets event information
 * including session UUID, position, and timing data.
 */
@DisplayName("EventData")
@ValidateCharset
@WithLocale("en")
class EventDataTest {

    private static final String FIXED_UUID = "12345";

    @Test
    @DisplayName("should initialize with default values when no-arg constructor is used")
    void testConstructorAndGetters0() {
        // Given: EventData with no-arg constructor
        // When: object is created
        final EventData event = new EventData();

        // Then: all fields should have default values
        assertNull(event.getSessionUuid(), "Session UUID should be null by default");
        assertEquals(0L, event.getPosition(), "Position should be 0 by default");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default");
    }

    @Test
    @DisplayName("should initialize sessionUuid correctly with single-arg constructor")
    void testConstructorAndGetters1() {
        // Given: EventData with UUID
        // When: created with "abc" UUID
        final EventData event = new EventData("abc");

        // Then: UUID should be set and other fields default
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(0L, event.getPosition(), "Position should be 0 by default");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default");
    }

    @Test
    @DisplayName("should initialize sessionUuid and position correctly with two-arg constructor")
    void testConstructorAndGetters2() {
        // Given: EventData with UUID and position
        // When: created with "abc" and 5L
        final EventData event = new EventData("abc", 5L);

        // Then: UUID and position should be set
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(5L, event.getPosition(), "Position should be initialized correctly");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 by default");
    }

    @Test
    @DisplayName("should initialize all fields correctly with three-arg constructor")
    void testConstructorAndGetters3() {
        // Given: EventData with UUID, position, and time
        // When: created with "abc", 5L, and 10L
        final EventData event = new EventData("abc", 5L, 10L);

        // Then: all fields should be initialized correctly
        assertEquals("abc", event.getSessionUuid(), "Session UUID should be initialized correctly");
        assertEquals(5L, event.getPosition(), "Position should be initialized correctly");
        assertEquals(10L, event.getLastCurrentTime(), "Last current time should be initialized correctly");
    }

    @Test
    @DisplayName("should update lastCurrentTime with current nano time")
    void testCollectCurrentTime() {
        // Given: EventData initialized
        final EventData event = new EventData();
        final long startTime = System.nanoTime();

        // When: collectCurrentTime is called
        event.collectCurrentTime();
        final long endTime = System.nanoTime();

        // Then: lastCurrentTime should be within the measured time window
        assertTrue(event.getLastCurrentTime() >= startTime, "Last current time should be >= start time");
        assertTrue(event.getLastCurrentTime() <= endTime, "Last current time should be <= end time");
    }

    @Test
    @DisplayName("should reset all fields to default values")
    void testResetClearsFields() {
        // Given: EventData with values set
        final EventData event = new EventData("abc", 5L, 10L);

        // When: reset is called
        event.reset();

        // Then: all fields should return to default values
        assertNull(event.getSessionUuid(), "Session UUID should be null after reset");
        assertEquals(0L, event.getPosition(), "Position should be 0 after reset");
        assertEquals(0L, event.getLastCurrentTime(), "Last current time should be 0 after reset");
    }

    @Test
    @DisplayName("should increment position normally")
    void testNextPosition_incrementNormal() {
        // Given: EventData with position 10L
        final EventData event = new EventData("test", 10L);

        // When: nextPosition is called
        event.nextPosition();

        // Then: position should increment by 1
        assertEquals(11L, event.getPosition(), "Position should increment by 1");
    }

    @Test
    @DisplayName("should reset position to 0 when Long.MAX_VALUE is reached")
    void testNextPosition_overflowResetsToZero() {
        // Given: EventData with position at Long.MAX_VALUE
        final EventData event = new EventData("test", Long.MAX_VALUE);

        // When: nextPosition is called (overflow condition)
        event.nextPosition();

        // Then: position should reset to 0
        assertEquals(0L, event.getPosition(), "Position should reset to 0 after reaching Long.MAX_VALUE");
    }

    @Test
    @DisplayName("should preserve event data during JSON5 write/read round-trip")
    void testReadWriteJson5() {
        // Given: EventData with all fields set
        final EventData event = new EventData(FIXED_UUID, 123L, 456L);
        final StringBuilder sb = new StringBuilder();

        // When: data is serialized to JSON5 and deserialized
        EventDataJson5.write(event, sb);
        final EventData event2 = new EventData();
        EventDataJson5.read(event2, "{" + sb + "}");

        // Then: deserialized data should match original
        assertEquals(event.getSessionUuid(), event2.getSessionUuid(), "Session UUID should be preserved");
        assertEquals(event.getPosition(), event2.getPosition(), "Position should be preserved");
        assertEquals(event.getLastCurrentTime(), event2.getLastCurrentTime(), "Last current time should be preserved");
    }
}
