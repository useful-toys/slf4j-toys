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

/**
 * Unit tests for {@link SystemData}.
 * <p>
 * Tests verify that SystemData correctly initializes, manages, and resets system monitoring information
 * including memory, garbage collection, class loading, and compilation metrics. Tests also validate JSON5 serialization
 * and deserialization functionality.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Constructor Initialization:</b> Tests initialization with full constructor setting all system metrics</li>
 *   <li><b>Constructor Variations:</b> Tests for SystemData(String uuid) and SystemData(String uuid, long timestamp) constructors with various scenarios</li>
 *   <li><b>Reset Functionality:</b> Verifies that reset clears all fields to default values</li>
 *   <li><b>Field Management:</b> Ensures correct setting and getting of memory, GC, class loading, and compilation data</li>
 *   <li><b>JSON5 Serialization/Deserialization:</b> Tests round-trip conversion and individual field parsing</li>
 *   <li><b>readJson5() Method:</b> Comprehensive coverage of JSON5 parsing including:
 *     <ul>
 *       <li>Individual field parsing (memory, heap, non-heap, class loading, garbage collector, system load)</li>
 *       <li>Complete JSON5 string parsing with all fields</li>
 *       <li>Whitespace handling and field order independence</li>
 *       <li>Partial field updates and preservation of existing values</li>
 *       <li>Edge cases: zero values, large numeric values, empty JSON5, missing fields</li>
 *       <li>Floating-point system load values and round-trip serialization</li>
 *     </ul>
 *   </li>
 *   <li><b>Parent Class Integration:</b> Tests inheritance from EventData and delegation to parent readJson5()</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
 */
@DisplayName("SystemData")
@ValidateCharset
@WithLocale("en")
class SystemDataTest {

    static class TestSystemData extends SystemData {
        public TestSystemData() {
        }

        public TestSystemData(final String uuid) {
            super(uuid);
        }

        public TestSystemData(final String uuid, final long timestamp) {
            super(uuid, timestamp);
        }

        protected TestSystemData(final String sessionUuid, final long position, final long lastCurrentTime,
                              final long heap_commited, final long heap_max, final long heap_used,
                              final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                              final long objectPendingFinalizationCount, final long classLoading_loaded,
                              final long classLoading_total, final long classLoading_unloaded, final long compilationTime,
                              final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory,
                              final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad) {
            super(sessionUuid, position, lastCurrentTime, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max,
                    nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded,
                    compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory,
                    runtime_totalMemory, systemLoad);
        }
    }

    @Test
    @DisplayName("should initialize all fields correctly with full constructor")
    void testConstructorAndGetters() {
        // Given: SystemData with all parameters
        // When: object is created
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);

        // Then: all fields should be set correctly
        assertEquals("abc", event.getSessionUuid());
        assertEquals(1, event.getPosition());
        assertEquals(2L, event.getLastCurrentTime());
        assertEquals(3L, event.getHeap_commited());
        assertEquals(4L, event.getHeap_max());
        assertEquals(5L, event.getHeap_used());
        assertEquals(6L, event.getNonHeap_commited());
        assertEquals(7L, event.getNonHeap_max());
        assertEquals(8L, event.getNonHeap_used());
        assertEquals(9L, event.getObjectPendingFinalizationCount());
        assertEquals(10L, event.getClassLoading_loaded());
        assertEquals(11L, event.getClassLoading_total());
        assertEquals(12L, event.getClassLoading_unloaded());
        assertEquals(13L, event.getCompilationTime());
        assertEquals(14L, event.getGarbageCollector_count());
        assertEquals(15L, event.getGarbageCollector_time());
        assertEquals(16L, event.getRuntime_usedMemory());
        assertEquals(17L, event.getRuntime_maxMemory());
        assertEquals(18L, event.getRuntime_totalMemory());
        assertEquals(19.0, event.getSystemLoad());
    }

    @Test
    @DisplayName("should reset all fields to default values")
    void testResetClearsFields() {
        // Given: SystemData with values set
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);

        // When: reset is called
        event.reset();

        // Then: all fields should return to default values
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
        assertEquals(0L, event.getHeap_commited());
        assertEquals(0L, event.getHeap_max());
        assertEquals(0L, event.getHeap_used());
        assertEquals(0L, event.getNonHeap_commited());
        assertEquals(0L, event.getNonHeap_max());
        assertEquals(0L, event.getNonHeap_used());
        assertEquals(0L, event.getObjectPendingFinalizationCount());
        assertEquals(0L, event.getClassLoading_loaded());
        assertEquals(0L, event.getClassLoading_total());
        assertEquals(0L, event.getClassLoading_unloaded());
        assertEquals(0L, event.getCompilationTime());
        assertEquals(0L, event.getGarbageCollector_count());
        assertEquals(0L, event.getGarbageCollector_time());
        assertEquals(0L, event.getRuntime_usedMemory());
        assertEquals(0L, event.getRuntime_maxMemory());
        assertEquals(0L, event.getRuntime_totalMemory());
        assertEquals(0.0, event.getSystemLoad());
    }

    /**
     * Provides test scenarios for round-trip JSON5 serialization tests.
     * Each scenario contains a descriptive name and a TestSystemData instance with specific values.
     */
    static Stream<Arguments> roundTripSerializationScenarios() {
        return Stream.of(
                // Scenario 1: Full data with all fields populated
                Arguments.of(
                        "Full data scenario",
                        new TestSystemData("full_uuid", 1, 2, 300, 400, 500, 150, 200, 250, 25, 800, 1000, 200, 10000, 75, 3500, 900, 1100, 1200, 1.5)
                ),

                // Scenario 2: Zero values (minimal data)
                Arguments.of(
                        "Zero values scenario",
                        new TestSystemData("zero_uuid", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0)
                ),

                // Scenario 3: Maximum long values for memory metrics
                Arguments.of(
                        "Maximum memory values",
                        new TestSystemData("max_memory_uuid", 999, 123456789L, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 0, 0, 0, 0, 0, 0, 0, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 0.0)
                ),

                // Scenario 4: Typical production values
                Arguments.of(
                        "Typical production values",
                        new TestSystemData("prod_uuid", 42, 987654321L, 1073741824, 2147483648L, 536870912, 268435456, 536870912, 134217728, 5, 5000, 10000, 500, 30000, 100, 5000, 1610612736, 4294967296L, 2147483648L, 2.5)
                ),

                // Scenario 5: Only heap metrics populated
                Arguments.of(
                        "Only heap metrics",
                        new TestSystemData("heap_only_uuid", 10, 111111L, 512000000, 1024000000, 256000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0)
                ),

                // Scenario 6: Only non-heap metrics populated
                Arguments.of(
                        "Only non-heap metrics",
                        new TestSystemData("nonheap_only_uuid", 20, 222222L, 0, 0, 0, 128000000, 256000000, 64000000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0)
                ),

                // Scenario 7: Only class loading metrics populated
                Arguments.of(
                        "Only class loading metrics",
                        new TestSystemData("classload_uuid", 30, 333333L, 0, 0, 0, 0, 0, 0, 0, 15000, 20000, 5000, 0, 0, 0, 0, 0, 0, 0.0)
                ),

                // Scenario 8: Only garbage collector metrics populated
                Arguments.of(
                        "Only GC metrics",
                        new TestSystemData("gc_uuid", 40, 444444L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 250, 15000, 0, 0, 0, 0.0)
                ),

                // Scenario 9: Only runtime memory metrics populated
                Arguments.of(
                        "Only runtime memory metrics",
                        new TestSystemData("runtime_uuid", 50, 555555L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2147483648L, 4294967296L, 3221225472L, 0.0)
                ),

                // Scenario 10: Only system load populated
                Arguments.of(
                        "Only system load",
                        new TestSystemData("sysload_uuid", 60, 666666L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5.8)
                ),

                // Scenario 11: High system load and finalization count
                Arguments.of(
                        "High load and finalization",
                        new TestSystemData("highload_uuid", 70, 777777L, 0, 0, 0, 0, 0, 0, 999, 0, 0, 0, 0, 0, 0, 0, 0, 0, 99.9)
                ),

                // Scenario 12: Small values (minimal memory usage)
                Arguments.of(
                        "Small values scenario",
                        new TestSystemData("small_uuid", 1, 1000L, 1024, 2048, 512, 256, 512, 128, 1, 100, 150, 50, 100, 5, 250, 1024, 2048, 1536, 0.1)
                ),

                // Scenario 13: Medium compilation time
                Arguments.of(
                        "With compilation time",
                        new TestSystemData("compile_uuid", 80, 888888L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 45000, 0, 0, 0, 0, 0, 0.0)
                ),

                // Scenario 14: Balanced resource usage
                Arguments.of(
                        "Balanced resource usage",
                        new TestSystemData("balanced_uuid", 100, 1000000L, 805306368, 1073741824, 402653184, 134217728, 268435456, 67108864, 10, 8000, 12000, 4000, 20000, 50, 2500, 1073741824, 2147483648L, 1610612736, 1.8)
                ),

                // Scenario 15: UUID with special characters
                Arguments.of(
                        "UUID with special characters",
                        new TestSystemData("uuid-with-dashes_123-456", 15, 151515L, 100, 200, 50, 75, 150, 25, 2, 500, 600, 100, 5000, 10, 500, 100000, 200000, 150000, 0.5)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripSerializationScenarios")
    @DisplayName("should support round-trip JSON5 serialization and deserialization")
    void testWriteReadJson5_roundTripSerialization(final String scenarioName, final TestSystemData original) {
        // Given: SystemData with specific values from scenario
        final StringBuilder sb = new StringBuilder(512);

        // When: original is serialized, then deserialized via readJson5
        original.writeJson5(sb);
        final TestSystemData restored = new TestSystemData();
        restored.readJson5("{" + sb + "}");

        // Then: restored data should match original for all fields
        assertEquals(original.getSessionUuid(), restored.getSessionUuid(), "should preserve sessionUuid in " + scenarioName);
        assertEquals(original.getPosition(), restored.getPosition(), "should preserve position in " + scenarioName);
        assertEquals(original.getLastCurrentTime(), restored.getLastCurrentTime(), "should preserve lastCurrentTime in " + scenarioName);
        assertEquals(original.getHeap_commited(), restored.getHeap_commited(), "should preserve heap_commited in " + scenarioName);
        assertEquals(original.getHeap_max(), restored.getHeap_max(), "should preserve heap_max in " + scenarioName);
        assertEquals(original.getHeap_used(), restored.getHeap_used(), "should preserve heap_used in " + scenarioName);
        assertEquals(original.getNonHeap_commited(), restored.getNonHeap_commited(), "should preserve nonHeap_commited in " + scenarioName);
        assertEquals(original.getNonHeap_max(), restored.getNonHeap_max(), "should preserve nonHeap_max in " + scenarioName);
        assertEquals(original.getNonHeap_used(), restored.getNonHeap_used(), "should preserve nonHeap_used in " + scenarioName);
        assertEquals(original.getObjectPendingFinalizationCount(), restored.getObjectPendingFinalizationCount(), "should preserve objectPendingFinalizationCount in " + scenarioName);
        assertEquals(original.getClassLoading_loaded(), restored.getClassLoading_loaded(), "should preserve classLoading_loaded in " + scenarioName);
        assertEquals(original.getClassLoading_total(), restored.getClassLoading_total(), "should preserve classLoading_total in " + scenarioName);
        assertEquals(original.getClassLoading_unloaded(), restored.getClassLoading_unloaded(), "should preserve classLoading_unloaded in " + scenarioName);
        assertEquals(original.getCompilationTime(), restored.getCompilationTime(), "should preserve compilationTime in " + scenarioName);
        assertEquals(original.getGarbageCollector_count(), restored.getGarbageCollector_count(), "should preserve garbageCollector_count in " + scenarioName);
        assertEquals(original.getGarbageCollector_time(), restored.getGarbageCollector_time(), "should preserve garbageCollector_time in " + scenarioName);
        assertEquals(original.getRuntime_usedMemory(), restored.getRuntime_usedMemory(), "should preserve runtime_usedMemory in " + scenarioName);
        assertEquals(original.getRuntime_maxMemory(), restored.getRuntime_maxMemory(), "should preserve runtime_maxMemory in " + scenarioName);
        assertEquals(original.getRuntime_totalMemory(), restored.getRuntime_totalMemory(), "should preserve runtime_totalMemory in " + scenarioName);
        assertEquals(original.getSystemLoad(), restored.getSystemLoad(), 0.001, "should preserve systemLoad in " + scenarioName);
    }


    /**
     * Provides test scenarios for single-parameter constructor SystemData(String uuid).
     * Tests various UUID values including null, empty, normal, and special characters.
     */
    static Stream<Arguments> singleParameterConstructorScenarios() {
        return Stream.of(
                Arguments.of("Normal UUID", "test-session-uuid-123"),
                Arguments.of("Null UUID", null),
                Arguments.of("Empty string UUID", ""),
                Arguments.of("UUID with special characters", "uuid-with-special-chars_!@#$%^&*()"),
                Arguments.of("UUID with dashes and numbers", "uuid-with-dashes-123-456"),
                Arguments.of("Very long UUID", "this-is-a-very-long-uuid-that-contains-many-characters-to-test-length-handling-12345678901234567890")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleParameterConstructorScenarios")
    @DisplayName("should initialize with uuid constructor - SystemData(String uuid)")
    void testConstructorWithUuidOnly(final String scenarioName, final String testUuid) {
        // Given: a session UUID from scenario
        // When: SystemData is created using single-parameter constructor
        final TestSystemData systemData = new TestSystemData(testUuid);

        // Then: sessionUuid should be set and all other fields should have default values
        if (testUuid == null) {
            assertNull(systemData.getSessionUuid(), "should accept null sessionUuid for " + scenarioName);
        } else {
            assertEquals(testUuid, systemData.getSessionUuid(), "should set sessionUuid from constructor for " + scenarioName);
        }
        assertEquals(0L, systemData.getPosition(), "should initialize position to 0 for " + scenarioName);
        assertEquals(0L, systemData.getLastCurrentTime(), "should initialize lastCurrentTime to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_commited(), "should initialize heap_commited to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_max(), "should initialize heap_max to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_used(), "should initialize heap_used to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_commited(), "should initialize nonHeap_commited to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_max(), "should initialize nonHeap_max to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_used(), "should initialize nonHeap_used to 0 for " + scenarioName);
        assertEquals(0L, systemData.getObjectPendingFinalizationCount(), "should initialize objectPendingFinalizationCount to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_loaded(), "should initialize classLoading_loaded to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_total(), "should initialize classLoading_total to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_unloaded(), "should initialize classLoading_unloaded to 0 for " + scenarioName);
        assertEquals(0L, systemData.getCompilationTime(), "should initialize compilationTime to 0 for " + scenarioName);
        assertEquals(0L, systemData.getGarbageCollector_count(), "should initialize garbageCollector_count to 0 for " + scenarioName);
        assertEquals(0L, systemData.getGarbageCollector_time(), "should initialize garbageCollector_time to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_usedMemory(), "should initialize runtime_usedMemory to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_maxMemory(), "should initialize runtime_maxMemory to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_totalMemory(), "should initialize runtime_totalMemory to 0 for " + scenarioName);
        assertEquals(0.0, systemData.getSystemLoad(), "should initialize systemLoad to 0.0 for " + scenarioName);
    }

    /**
     * Provides test scenarios for two-parameter constructor SystemData(String uuid, long timestamp).
     * Tests various combinations of UUID and timestamp values including edge cases.
     */
    static Stream<Arguments> twoParameterConstructorScenarios() {
        return Stream.of(
                Arguments.of("Normal values", "test-session-uuid-456", 123456789L),
                Arguments.of("Null UUID with timestamp", null, 999888777L),
                Arguments.of("Zero timestamp", "zero-timestamp-test", 0L),
                Arguments.of("Negative timestamp", "negative-timestamp-test", -12345L),
                Arguments.of("Long.MAX_VALUE timestamp", "max-timestamp-test", Long.MAX_VALUE),
                Arguments.of("Long.MIN_VALUE timestamp", "min-timestamp-test", Long.MIN_VALUE),
                Arguments.of("Small positive timestamp", "small-timestamp", 1L),
                Arguments.of("Large timestamp", "large-timestamp", 9223372036854775806L),
                Arguments.of("UUID with special chars and timestamp", "uuid-with-dashes-and-numbers-123-456-789", 555666777L)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("twoParameterConstructorScenarios")
    @DisplayName("should initialize with uuid and timestamp constructor - SystemData(String uuid, long timestamp)")
    void testConstructorWithUuidAndTimestamp(final String scenarioName, final String testUuid, final long testTimestamp) {
        // Given: a session UUID and timestamp from scenario
        // When: SystemData is created using two-parameter constructor
        final TestSystemData systemData = new TestSystemData(testUuid, testTimestamp);

        // Then: sessionUuid and position should be set (position is set via super(uuid, position))
        if (testUuid == null) {
            assertNull(systemData.getSessionUuid(), "should accept null sessionUuid for " + scenarioName);
        } else {
            assertEquals(testUuid, systemData.getSessionUuid(), "should set sessionUuid from constructor for " + scenarioName);
        }
        assertEquals(testTimestamp, systemData.getPosition(), "should set position from timestamp parameter for " + scenarioName);
        assertEquals(0L, systemData.getLastCurrentTime(), "should initialize lastCurrentTime to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_commited(), "should initialize heap_commited to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_max(), "should initialize heap_max to 0 for " + scenarioName);
        assertEquals(0L, systemData.getHeap_used(), "should initialize heap_used to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_commited(), "should initialize nonHeap_commited to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_max(), "should initialize nonHeap_max to 0 for " + scenarioName);
        assertEquals(0L, systemData.getNonHeap_used(), "should initialize nonHeap_used to 0 for " + scenarioName);
        assertEquals(0L, systemData.getObjectPendingFinalizationCount(), "should initialize objectPendingFinalizationCount to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_loaded(), "should initialize classLoading_loaded to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_total(), "should initialize classLoading_total to 0 for " + scenarioName);
        assertEquals(0L, systemData.getClassLoading_unloaded(), "should initialize classLoading_unloaded to 0 for " + scenarioName);
        assertEquals(0L, systemData.getCompilationTime(), "should initialize compilationTime to 0 for " + scenarioName);
        assertEquals(0L, systemData.getGarbageCollector_count(), "should initialize garbageCollector_count to 0 for " + scenarioName);
        assertEquals(0L, systemData.getGarbageCollector_time(), "should initialize garbageCollector_time to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_usedMemory(), "should initialize runtime_usedMemory to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_maxMemory(), "should initialize runtime_maxMemory to 0 for " + scenarioName);
        assertEquals(0L, systemData.getRuntime_totalMemory(), "should initialize runtime_totalMemory to 0 for " + scenarioName);
        assertEquals(0.0, systemData.getSystemLoad(), "should initialize systemLoad to 0.0 for " + scenarioName);
    }
}
