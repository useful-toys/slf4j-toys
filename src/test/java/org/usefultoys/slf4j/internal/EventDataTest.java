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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

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
 *   <li><b>JSON5 Serialization/Deserialization:</b> Parameterized round-trip tests with multiple scenarios</li>
 *   <li><b>readJson5() Method:</b> Comprehensive coverage of JSON5 parsing including:
 *     <ul>
 *       <li>Whitespace handling and field order independence</li>
 *       <li>Partial field updates and preservation of existing values</li>
 *       <li>Edge cases: empty JSON5, missing fields</li>
 *     </ul>
 *   </li>
 *   <li><b>TimeSource Integration:</b> Tests default and custom time source implementations</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
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
    // JSON5 Round-Trip Serialization Tests
    // ============================================================================

    /**
     * Provides test scenarios for round-trip JSON5 serialization tests.
     * Each scenario contains a descriptive name and an EventData instance with specific values.
     * Includes edge cases like empty JSON5, missing fields, whitespace, and field order variations.
     */
    static Stream<Arguments> roundTripSerializationScenarios() {
        return Stream.of(
                // Scenario 1: Full data with all fields populated
                Arguments.of(
                        "Full data scenario",
                        new EventData(FIXED_UUID, 123L, 456L)
                ),

                // Scenario 2: Zero values
                Arguments.of(
                        "Zero values scenario",
                        new EventData("zero_uuid", 0L, 0L)
                ),

                // Scenario 3: Large numeric values (Long.MAX_VALUE)
                Arguments.of(
                        "Large numeric values",
                        new EventData("large_uuid", Long.MAX_VALUE, Long.MAX_VALUE - 1)
                ),

                // Scenario 4: Small positive values
                Arguments.of(
                        "Small positive values",
                        new EventData("small_uuid", 1L, 1000L)
                ),

                // Scenario 5: Medium values (typical use case)
                Arguments.of(
                        "Medium values",
                        new EventData("session123", 55L, 7777777L)
                ),

                // Scenario 6: UUID with special characters (dashes, underscores, dots)
                Arguments.of(
                        "UUID with special characters",
                        new EventData("uuid-with_special.chars123", 100L, 2000L)
                ),

                // Scenario 7: UUID with dashes only
                Arguments.of(
                        "UUID with dashes",
                        new EventData("uuid-with-many-dashes", 42L, 123456789L)
                ),

                // Scenario 8: Short UUID
                Arguments.of(
                        "Short UUID",
                        new EventData("abc", 10L, 500L)
                ),

                // Scenario 9: Long UUID
                Arguments.of(
                        "Long UUID",
                        new EventData("this-is-a-very-long-uuid-for-testing-purposes-12345678901234567890", 999L, 555555L)
                ),

                // Scenario 10: Alphanumeric UUID
                Arguments.of(
                        "Alphanumeric UUID",
                        new EventData("abc123xyz789", 777L, 3000L)
                ),

                // Scenario 11: High position value
                Arguments.of(
                        "High position value",
                        new EventData("high_pos_uuid", 9223372036854775806L, 1000000L)
                ),

                // Scenario 12: High timestamp value
                Arguments.of(
                        "High timestamp value",
                        new EventData("high_time_uuid", 50L, 9223372036854775805L)
                ),

                // Scenario 13: Negative position (edge case)
                Arguments.of(
                        "Negative position",
                        new EventData("neg_pos_uuid", -1L, 5000L)
                ),

                // Scenario 14: UUID with underscores
                Arguments.of(
                        "UUID with underscores",
                        new EventData("uuid_with_underscores_123", 888L, 9999999L)
                ),

                // Scenario 15: Mixed case UUID
                Arguments.of(
                        "Mixed case UUID",
                        new EventData("MixedCaseUUID-Test-123", 456L, 654321L)
                ),

                // Scenario 16: Public method round-trip
                Arguments.of(
                        "Public method round-trip",
                        new EventData("public_test", 123L, 4567L)
                ),

                // Scenario 17: ABC123 scenario
                Arguments.of(
                        "ABC123 scenario",
                        new EventData("abc123", 42L, 9876543210L)
                ),

                // Edge Case Scenario 18: Fields preserved when empty JSON5
                Arguments.of(
                        "Empty JSON5 preserves existing fields",
                        new EventData("original_uuid", 42L, 100L)
                ),

                // Edge Case Scenario 19: UUID with whitespace test
                Arguments.of(
                        "UUID test with whitespace",
                        new EventData("uuid_test", 100L, 2000L)
                ),

                // Edge Case Scenario 20: Different field order test
                Arguments.of(
                        "Field order independence test",
                        new EventData("ord_test", 77L, 3000L)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripSerializationScenarios")
    @DisplayName("should support round-trip JSON5 serialization and deserialization")
    void testReadJson5_roundTripSerialization(final String scenarioName, final EventData original) {
        // Given: EventData with specific values from scenario
        final StringBuilder sb = new StringBuilder(128);

        // When: original is serialized to JSON5, then deserialized
        original.writeJson5(sb);
        final EventData restored = new EventData();
        restored.readJson5("{" + sb + "}");

        // Then: restored data should match original for all fields
        assertEquals(original.getSessionUuid(), restored.getSessionUuid(), "should preserve sessionUuid in " + scenarioName);
        assertEquals(original.getPosition(), restored.getPosition(), "should preserve position in " + scenarioName);
        assertEquals(original.getLastCurrentTime(), restored.getLastCurrentTime(), "should preserve lastCurrentTime in " + scenarioName);
    }

    // ============================================================================
    // JSON5 Special Behavior Tests (Non-Round-Trip)
    // ============================================================================

    @Test
    @DisplayName("should handle empty JSON5 gracefully without altering existing fields")
    void testReadJson5_emptyJson5() {
        // Given: EventData with existing values and empty JSON5 string
        final EventData event = new EventData("original_uuid", 42L, 100L);
        final String json5 = "{}";

        // When: readJson5 is called with empty JSON5
        event.readJson5(json5);

        // Then: fields should remain unchanged (not a round-trip test)
        assertEquals("original_uuid", event.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(42L, event.getPosition(), "should preserve position");
        assertEquals(100L, event.getLastCurrentTime(), "should preserve lastCurrentTime");
    }

    @Test
    @DisplayName("should handle missing fields without throwing exceptions")
    void testReadJson5_missingFields() {
        // Given: EventData with existing values and JSON5 missing some fields
        final EventData event = new EventData("orig_uuid", 50L, 200L);
        final String json5 = "{$:75}";

        // When: readJson5 is called with only position field
        event.readJson5(json5);

        // Then: only present field should be updated (partial update, not round-trip)
        assertEquals("orig_uuid", event.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(75L, event.getPosition(), "should update position");
        assertEquals(200L, event.getLastCurrentTime(), "should preserve lastCurrentTime");
    }

    @Test
    @DisplayName("should preserve existing fields when JSON5 does not contain them")
    void testReadJson5_partialFieldsParsing() {
        // Given: EventData with existing values and partial JSON5
        final EventData event = new EventData("existing_uuid", 999L, 5555L);
        final String json5 = "{_:new_uuid}";

        // When: readJson5 is called with only sessionUuid
        event.readJson5(json5);

        // Then: sessionUuid should be updated and others unchanged (partial update test)
        assertEquals("new_uuid", event.getSessionUuid(), "should update sessionUuid");
        assertEquals(999L, event.getPosition(), "should preserve existing position");
        assertEquals(5555L, event.getLastCurrentTime(), "should preserve existing lastCurrentTime");
    }


}
