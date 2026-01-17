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
 * including session UUID, position, and timing data. Tests also validate JSON5 serialization
 * and deserialization functionality.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Constructor Initialization:</b> Tests initialization with no-arg, single-arg, and multi-arg constructors</li>
 *   <li><b>Field Management:</b> Verifies correct setting and getting of sessionUuid, position, and timing fields</li>
 *   <li><b>Time Collection:</b> Ensures collectCurrentTime updates lastCurrentTime with current nano time</li>
 *   <li><b>Reset Functionality:</b> Tests that reset clears all fields to default values</li>
 *   <li><b>Position Increment:</b> Tests normal increment and overflow handling at Long.MAX_VALUE</li>
 *   <li><b>JSON5 Serialization/Deserialization:</b> Tests round-trip conversion and individual field parsing</li>
 *   <li><b>readJson5() Method:</b> Comprehensive coverage of JSON5 parsing including:
 *     <ul>
 *       <li>Individual field parsing (sessionUuid, position, lastCurrentTime)</li>
 *       <li>Complete JSON5 string parsing with all fields</li>
 *       <li>Whitespace handling and field order independence</li>
 *       <li>Partial field updates and preservation of existing values</li>
 *       <li>Edge cases: zero values, large numeric values, empty JSON5, missing fields</li>
 *       <li>Special characters in UUID and round-trip serialization</li>
 *     </ul>
 *   </li>
 *   <li><b>TimeSource Integration:</b> Tests default and custom time source implementations</li>
 * </ul>
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

    @Test
    @DisplayName("should use default SystemTimeSource by default")
    void testDefaultTimeSource() {
        // Given: EventData with default time source
        final EventData event = new EventData();
        final long startTime = System.nanoTime();

        // When: collectCurrentTime is called
        final long collectedTime = event.collectCurrentTime();
        final long endTime = System.nanoTime();

        // Then: collected time should be within system time window
        assertTrue(collectedTime >= startTime, "Collected time should be >= start time");
        assertTrue(collectedTime <= endTime, "Collected time should be <= end time");
        assertEquals(collectedTime, event.getLastCurrentTime(), "Last current time should match collected time");
    }

    @Test
    @DisplayName("should use custom TimeSource when set via withTimeSource")
    void testCustomTimeSource() {
        // Given: EventData with custom time source
        final TestTimeSource testTimeSource = new TestTimeSource();
        testTimeSource.setNanoTime(1000L);
        final EventData event = new EventData();

        // When: custom time source is set and collectCurrentTime is called
        event.setTimeSource(testTimeSource);
        final long collectedTime = event.collectCurrentTime();

        // Then: collected time should match custom time source
        assertEquals(1000L, collectedTime, "Collected time should match custom time source value");
        assertEquals(1000L, event.getLastCurrentTime(), "Last current time should match custom time source value");
    }

    @Test
    @DisplayName("should collect deterministic times with controllable time source")
    void testDeterministicTimeCollection() {
        // Given: EventData with controllable time source
        final TestTimeSource testTimeSource = new TestTimeSource();
        final EventData event = new EventData();
        event.setTimeSource(testTimeSource);

        // When: time is advanced in controlled steps
        testTimeSource.setNanoTime(100L);
        event.collectCurrentTime();
        final long time1 = event.getLastCurrentTime();

        testTimeSource.setNanoTime(200L);
        event.collectCurrentTime();
        final long time2 = event.getLastCurrentTime();

        testTimeSource.advanceMiliseconds(150L);
        event.collectCurrentTime();
        final long time3 = event.getLastCurrentTime();

        // Then: collected times should match controlled progression
        assertEquals(100L, time1, "First collected time should be 100L");
        assertEquals(200L, time2, "Second collected time should be 200L");
        assertEquals(150000200L, time3, "Third collected time should be 350L");
    }

    // ============================================================================
    // readJson5() method tests
    // ============================================================================

    @Test
    @DisplayName("should parse sessionUuid from JSON5 string")
    void testReadJson5_parseSessionUuid() {
        // Given: EventData instance and JSON5 string with sessionUuid
        final EventData event = new EventData();
        final String json5 = "{_:abc123}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: sessionUuid should be parsed correctly
        assertEquals("abc123", event.getSessionUuid(), "should parse sessionUuid from JSON5");
    }

    @Test
    @DisplayName("should parse position from JSON5 string")
    void testReadJson5_parsePosition() {
        // Given: EventData instance and JSON5 string with position
        final EventData event = new EventData();
        final String json5 = "{$:42}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: position should be parsed correctly
        assertEquals(42L, event.getPosition(), "should parse position from JSON5");
    }

    @Test
    @DisplayName("should parse lastCurrentTime from JSON5 string")
    void testReadJson5_parseLastCurrentTime() {
        // Given: EventData instance and JSON5 string with time
        final EventData event = new EventData();
        final String json5 = "{t:9876543210}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: lastCurrentTime should be parsed correctly
        assertEquals(9876543210L, event.getLastCurrentTime(), "should parse lastCurrentTime from JSON5");
    }

    @Test
    @DisplayName("should parse all fields from complete JSON5 string")
    void testReadJson5_parseAllFields() {
        // Given: EventData instance and complete JSON5 string
        final EventData event = new EventData();
        final String json5 = "{_:session123,$:55,t:7777777}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: all fields should be parsed correctly
        assertEquals("session123", event.getSessionUuid(), "should parse sessionUuid");
        assertEquals(55L, event.getPosition(), "should parse position");
        assertEquals(7777777L, event.getLastCurrentTime(), "should parse lastCurrentTime");
    }

    @Test
    @DisplayName("should handle JSON5 with extra whitespace and line breaks")
    void testReadJson5_whitespaceHandling() {
        // Given: EventData and JSON5 with extra whitespace
        final EventData event = new EventData();
        final String json5 = "{ _  :  uuid_test , $ : 100 , t : 2000 }";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: all fields should be parsed correctly ignoring whitespace
        assertEquals("uuid_test", event.getSessionUuid(), "should parse sessionUuid with whitespace");
        assertEquals(100L, event.getPosition(), "should parse position with whitespace");
        assertEquals(2000L, event.getLastCurrentTime(), "should parse lastCurrentTime with whitespace");
    }

    @Test
    @DisplayName("should handle JSON5 with comma-separated fields in any order")
    void testReadJson5_differentFieldOrder() {
        // Given: EventData and JSON5 with fields in different order
        final EventData event = new EventData();
        final String json5 = "{t:3000,$:77,_:ord_test}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: all fields should be parsed correctly regardless of order
        assertEquals("ord_test", event.getSessionUuid(), "should parse sessionUuid in different order");
        assertEquals(77L, event.getPosition(), "should parse position in different order");
        assertEquals(3000L, event.getLastCurrentTime(), "should parse lastCurrentTime in different order");
    }

    @Test
    @DisplayName("should preserve existing fields when JSON5 does not contain them")
    void testReadJson5_partialFieldsParsing() {
        // Given: EventData with existing values and partial JSON5
        final EventData event = new EventData("existing_uuid", 999L, 5555L);
        final String json5 = "{_:new_uuid}";

        // When: readJson5 is called with only sessionUuid
        event.readJson5(json5);

        // Then: sessionUuid should be updated and others unchanged
        assertEquals("new_uuid", event.getSessionUuid(), "should update sessionUuid");
        assertEquals(999L, event.getPosition(), "should preserve existing position");
        assertEquals(5555L, event.getLastCurrentTime(), "should preserve existing lastCurrentTime");
    }

    @Test
    @DisplayName("should handle zero values correctly")
    void testReadJson5_zeroValues() {
        // Given: EventData and JSON5 with zero values
        final EventData event = new EventData();
        final String json5 = "{_:zero_uuid,$:0,t:0}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: zero values should be parsed correctly
        assertEquals("zero_uuid", event.getSessionUuid(), "should parse sessionUuid");
        assertEquals(0L, event.getPosition(), "should parse zero position");
        assertEquals(0L, event.getLastCurrentTime(), "should parse zero lastCurrentTime");
    }

    @Test
    @DisplayName("should handle large numeric values correctly")
    void testReadJson5_largeNumericValues() {
        // Given: EventData and JSON5 with large numeric values
        final EventData event = new EventData();
        final String json5 = "{_:large_uuid,$:9223372036854775807,t:9223372036854775806}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: large values should be parsed correctly
        assertEquals("large_uuid", event.getSessionUuid(), "should parse sessionUuid");
        assertEquals(Long.MAX_VALUE, event.getPosition(), "should parse Long.MAX_VALUE");
        assertEquals(Long.MAX_VALUE - 1, event.getLastCurrentTime(), "should parse large lastCurrentTime");
    }

    @Test
    @DisplayName("should handle empty JSON5 gracefully without errors")
    void testReadJson5_emptyJson5() {
        // Given: EventData and empty JSON5 string
        final EventData event = new EventData("original_uuid", 42L, 100L);
        final String json5 = "{}";

        // When: readJson5 is called with empty JSON5
        event.readJson5(json5);

        // Then: fields should remain unchanged
        assertEquals("original_uuid", event.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(42L, event.getPosition(), "should preserve position");
        assertEquals(100L, event.getLastCurrentTime(), "should preserve lastCurrentTime");
    }

    @Test
    @DisplayName("should handle missing fields without throwing exceptions")
    void testReadJson5_missingFields() {
        // Given: EventData and JSON5 missing some fields
        final EventData event = new EventData("orig_uuid", 50L, 200L);
        final String json5 = "{$:75}";

        // When: readJson5 is called with only position field
        event.readJson5(json5);

        // Then: only present fields should be updated
        assertEquals("orig_uuid", event.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(75L, event.getPosition(), "should update position");
        assertEquals(200L, event.getLastCurrentTime(), "should preserve lastCurrentTime");
    }

    @Test
    @DisplayName("should delegate to readJson5 public method correctly")
    void testReadJson5_publicMethod() {
        // Given: EventData and complete JSON5 data
        final EventData event = new EventData();
        final String json5 = "{_:public_test,$:123,t:4567}";

        // When: public readJson5 method is called
        event.readJson5(json5);

        // Then: all fields should be populated correctly
        assertEquals("public_test", event.getSessionUuid(), "should parse sessionUuid via public method");
        assertEquals(123L, event.getPosition(), "should parse position via public method");
        assertEquals(4567L, event.getLastCurrentTime(), "should parse lastCurrentTime via public method");
    }

    @Test
    @DisplayName("should handle special characters in sessionUuid")
    void testReadJson5_specialCharactersInUuid() {
        // Given: EventData and JSON5 with special characters in UUID
        final EventData event = new EventData();
        final String json5 = "{_:uuid-with_special.chars123}";

        // When: readJson5 is called
        event.readJson5(json5);

        // Then: UUID with special characters should be parsed correctly
        assertEquals("uuid-with_special.chars123", event.getSessionUuid(), "should parse UUID with special characters");
    }

    @Test
    @DisplayName("should support round-trip JSON5 serialization via public method")
    void testReadJson5_roundTripWithPublicMethod() {
        // Given: EventData with known values
        final EventData original = new EventData("roundtrip_uuid", 999L, 555555L);
        final StringBuilder sb = new StringBuilder();

        // When: original is serialized, then deserialized via public method
        EventDataJson5.write(original, sb);
        final EventData restored = new EventData();
        restored.readJson5("{" + sb + "}");

        // Then: restored data should match original
        assertEquals(original.getSessionUuid(), restored.getSessionUuid(), "should preserve sessionUuid in round-trip");
        assertEquals(original.getPosition(), restored.getPosition(), "should preserve position in round-trip");
        assertEquals(original.getLastCurrentTime(), restored.getLastCurrentTime(), "should preserve lastCurrentTime in round-trip");
    }

}
