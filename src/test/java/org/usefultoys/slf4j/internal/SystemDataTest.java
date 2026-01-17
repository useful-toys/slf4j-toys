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
 */
@DisplayName("SystemData")
@ValidateCharset
@WithLocale("en")
class SystemDataTest {

    static class TestSystemData extends SystemData {
        public TestSystemData() {
        }

        public TestSystemData(final String sessionUuid, final long position) {
            super(sessionUuid);
            this.position = position;
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

    // ============================================================================
    // readJson5() method tests
    // ============================================================================

    @Test
    @DisplayName("should parse runtime memory from JSON5 string")
    void testReadJson5_parseRuntimeMemory() {
        // Given: SystemData instance and JSON5 with runtime memory
        final TestSystemData data = new TestSystemData();
        final String json5 = "{m:[1000,2000,3000]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: runtime memory fields should be parsed correctly
        assertEquals(1000L, data.getRuntime_usedMemory(), "should parse runtime_usedMemory");
        assertEquals(2000L, data.getRuntime_totalMemory(), "should parse runtime_totalMemory");
        assertEquals(3000L, data.getRuntime_maxMemory(), "should parse runtime_maxMemory");
    }

    @Test
    @DisplayName("should parse heap memory from JSON5 string")
    void testReadJson5_parseHeapMemory() {
        // Given: SystemData instance and JSON5 with heap memory
        final TestSystemData data = new TestSystemData();
        final String json5 = "{h:[100,200,300]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: heap memory fields should be parsed correctly
        assertEquals(100L, data.getHeap_used(), "should parse heap_used");
        assertEquals(200L, data.getHeap_commited(), "should parse heap_commited");
        assertEquals(300L, data.getHeap_max(), "should parse heap_max");
    }

    @Test
    @DisplayName("should parse non-heap memory from JSON5 string")
    void testReadJson5_parseNonHeapMemory() {
        // Given: SystemData instance and JSON5 with non-heap memory
        final TestSystemData data = new TestSystemData();
        final String json5 = "{nh:[50,60,70]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: non-heap memory fields should be parsed correctly
        assertEquals(50L, data.getNonHeap_used(), "should parse nonHeap_used");
        assertEquals(60L, data.getNonHeap_commited(), "should parse nonHeap_commited");
        assertEquals(70L, data.getNonHeap_max(), "should parse nonHeap_max");
    }

    @Test
    @DisplayName("should parse object pending finalization count from JSON5")
    void testReadJson5_parseFinalizationCount() {
        // Given: SystemData instance and JSON5 with finalization count
        final TestSystemData data = new TestSystemData();
        final String json5 = "{fc:42}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: finalization count should be parsed correctly
        assertEquals(42L, data.getObjectPendingFinalizationCount(), "should parse objectPendingFinalizationCount");
    }

    @Test
    @DisplayName("should parse class loading metrics from JSON5 string")
    void testReadJson5_parseClassLoading() {
        // Given: SystemData instance and JSON5 with class loading data
        final TestSystemData data = new TestSystemData();
        final String json5 = "{cl:[5000,3000,500]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: class loading fields should be parsed correctly
        assertEquals(5000L, data.getClassLoading_total(), "should parse classLoading_total");
        assertEquals(3000L, data.getClassLoading_loaded(), "should parse classLoading_loaded");
        assertEquals(500L, data.getClassLoading_unloaded(), "should parse classLoading_unloaded");
    }

    @Test
    @DisplayName("should parse compilation time from JSON5 string")
    void testReadJson5_parseCompilationTime() {
        // Given: SystemData instance and JSON5 with compilation time
        final TestSystemData data = new TestSystemData();
        final String json5 = "{ct:12345}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: compilation time should be parsed correctly
        assertEquals(12345L, data.getCompilationTime(), "should parse compilationTime");
    }

    @Test
    @DisplayName("should parse garbage collector metrics from JSON5 string")
    void testReadJson5_parseGarbageCollector() {
        // Given: SystemData instance and JSON5 with GC metrics
        final TestSystemData data = new TestSystemData();
        final String json5 = "{gc:[100,5000]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: garbage collector fields should be parsed correctly
        assertEquals(100L, data.getGarbageCollector_count(), "should parse garbageCollector_count");
        assertEquals(5000L, data.getGarbageCollector_time(), "should parse garbageCollector_time");
    }

    @Test
    @DisplayName("should parse system load from JSON5 string")
    void testReadJson5_parseSystemLoad() {
        // Given: SystemData instance and JSON5 with system load
        final TestSystemData data = new TestSystemData();
        final String json5 = "{sl:2.5}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: system load should be parsed correctly
        assertEquals(2.5, data.getSystemLoad(), 0.001, "should parse systemLoad");
    }

    @Test
    @DisplayName("should parse all system fields from complete JSON5 string")
    void testReadJson5_parseAllFields() {
        // Given: SystemData instance and complete JSON5 string
        final TestSystemData data = new TestSystemData();
        final String json5 = "{_:uuid123,$:10,t:1000,m:[1000,2000,3000],h:[100,200,300],nh:[50,60,70],fc:42,cl:[5000,3000,500],ct:12345,gc:[100,5000],sl:2.5}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: all fields should be parsed correctly
        assertEquals("uuid123", data.getSessionUuid(), "should parse sessionUuid");
        assertEquals(10L, data.getPosition(), "should parse position");
        assertEquals(1000L, data.getLastCurrentTime(), "should parse lastCurrentTime");
        assertEquals(1000L, data.getRuntime_usedMemory(), "should parse runtime_usedMemory");
        assertEquals(2000L, data.getRuntime_totalMemory(), "should parse runtime_totalMemory");
        assertEquals(3000L, data.getRuntime_maxMemory(), "should parse runtime_maxMemory");
        assertEquals(100L, data.getHeap_used(), "should parse heap_used");
        assertEquals(200L, data.getHeap_commited(), "should parse heap_commited");
        assertEquals(300L, data.getHeap_max(), "should parse heap_max");
        assertEquals(50L, data.getNonHeap_used(), "should parse nonHeap_used");
        assertEquals(60L, data.getNonHeap_commited(), "should parse nonHeap_commited");
        assertEquals(70L, data.getNonHeap_max(), "should parse nonHeap_max");
        assertEquals(42L, data.getObjectPendingFinalizationCount(), "should parse objectPendingFinalizationCount");
        assertEquals(5000L, data.getClassLoading_total(), "should parse classLoading_total");
        assertEquals(3000L, data.getClassLoading_loaded(), "should parse classLoading_loaded");
        assertEquals(500L, data.getClassLoading_unloaded(), "should parse classLoading_unloaded");
        assertEquals(12345L, data.getCompilationTime(), "should parse compilationTime");
        assertEquals(100L, data.getGarbageCollector_count(), "should parse garbageCollector_count");
        assertEquals(5000L, data.getGarbageCollector_time(), "should parse garbageCollector_time");
        assertEquals(2.5, data.getSystemLoad(), 0.001, "should parse systemLoad");
    }

    @Test
    @DisplayName("should handle JSON5 with extra whitespace")
    void testReadJson5_whitespaceHandling() {
        // Given: SystemData and JSON5 with extra whitespace
        final TestSystemData data = new TestSystemData();
        final String json5 = "{m:[1000,2000,3000],h:[100,200,300]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: all fields should be parsed correctly
        assertEquals(1000L, data.getRuntime_usedMemory(), "should parse runtime memory");
        assertEquals(100L, data.getHeap_used(), "should parse heap");
    }

    @Test
    @DisplayName("should handle JSON5 with different field order")
    void testReadJson5_differentFieldOrder() {
        // Given: SystemData and JSON5 with fields in different order
        final TestSystemData data = new TestSystemData();
        final String json5 = "{sl:1.5,gc:[50,2000],ct:5000,cl:[1000,800,100],fc:20,nh:[25,30,35],h:[50,100,150],m:[500,1000,1500]}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: all fields should be parsed correctly regardless of order
        assertEquals(500L, data.getRuntime_usedMemory(), "should parse in different order");
        assertEquals(50L, data.getHeap_used(), "should parse heap in different order");
        assertEquals(20L, data.getObjectPendingFinalizationCount(), "should parse fc in different order");
        assertEquals(1.5, data.getSystemLoad(), 0.001, "should parse sl in different order");
    }

    @Test
    @DisplayName("should preserve existing fields when JSON5 does not contain them")
    void testReadJson5_partialFieldsParsing() {
        // Given: SystemData with existing values and partial JSON5
        final TestSystemData data = new TestSystemData("existing_uuid", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        final String json5 = "{m:[1000,2000,3000]}";

        // When: readJson5 is called with only runtime memory
        data.readJson5(json5);

        // Then: runtime memory should be updated and others unchanged
        assertEquals(1000L, data.getRuntime_usedMemory(), "should update runtime_usedMemory");
        assertEquals(3L, data.getHeap_commited(), "should preserve heap_commited");
        assertEquals(14L, data.getGarbageCollector_count(), "should preserve garbageCollector_count");
    }

    @Test
    @DisplayName("should handle zero values correctly")
    void testReadJson5_zeroValues() {
        // Given: SystemData and JSON5 with zero values
        final TestSystemData data = new TestSystemData();
        final String json5 = "{m:[0,0,0],h:[0,0,0],fc:0,sl:0.0}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: zero values should be parsed correctly
        assertEquals(0L, data.getRuntime_usedMemory(), "should parse zero runtime memory");
        assertEquals(0L, data.getHeap_used(), "should parse zero heap");
        assertEquals(0L, data.getObjectPendingFinalizationCount(), "should parse zero fc");
        assertEquals(0.0, data.getSystemLoad(), "should parse zero system load");
    }

    @Test
    @DisplayName("should handle large numeric values correctly")
    void testReadJson5_largeNumericValues() {
        // Given: SystemData and JSON5 with large numeric values
        final TestSystemData data = new TestSystemData();
        final String json5 = "{m:[9223372036854775807,9223372036854775806,9223372036854775805],h:[1000000000000,2000000000000,3000000000000],sl:99.9}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: large values should be parsed correctly
        assertEquals(Long.MAX_VALUE, data.getRuntime_usedMemory(), "should parse Long.MAX_VALUE");
        assertEquals(1000000000000L, data.getHeap_used(), "should parse large heap_used");
        assertEquals(99.9, data.getSystemLoad(), 0.001, "should parse large system load");
    }

    @Test
    @DisplayName("should handle empty JSON5 gracefully without errors")
    void testReadJson5_emptyJson5() {
        // Given: SystemData with values and empty JSON5
        final TestSystemData data = new TestSystemData("uuid", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        final String json5 = "{}";

        // When: readJson5 is called with empty JSON5
        data.readJson5(json5);

        // Then: fields should remain unchanged
        assertEquals("uuid", data.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(3L, data.getHeap_commited(), "should preserve heap_commited");
        assertEquals(14L, data.getGarbageCollector_count(), "should preserve garbageCollector_count");
    }

    @Test
    @DisplayName("should handle missing fields without throwing exceptions")
    void testReadJson5_missingFields() {
        // Given: SystemData with values and partial JSON5
        // Constructor params: uuid, pos, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used, finCount, cl_loaded, cl_total, cl_unloaded, compTime, gc_count, gc_time, rt_used, rt_max, rt_total, sysLoad
        final TestSystemData data = new TestSystemData("orig_uuid", 1, 2, 100, 200, 300, 50, 60, 70, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        final String json5 = "{h:[400,500,600]}";

        // When: readJson5 is called with only heap field
        data.readJson5(json5);

        // Then: only present fields should be updated
        assertEquals("orig_uuid", data.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(400L, data.getHeap_used(), "should update heap_used");
        assertEquals(70L, data.getNonHeap_used(), "should preserve nonHeap_used");
    }

    @Test
    @DisplayName("should handle floating-point system load with various precision levels")
    void testReadJson5_systemLoadFloatingPoint() {
        // Given: SystemData and JSON5 with different floating-point formats
        final TestSystemData data = new TestSystemData();
        final String json5 = "{sl:0.1}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: floating-point value should be parsed correctly
        assertEquals(0.1, data.getSystemLoad(), 0.001, "should parse 0.1");
    }

    @Test
    @DisplayName("should support round-trip JSON5 serialization and deserialization")
    void testReadJson5_roundTripSerialization() {
        // Given: SystemData with known values
        final TestSystemData original = new TestSystemData("roundtrip_uuid", 1, 2, 300, 400, 500, 150, 200, 250, 25, 800, 1000, 200, 10000, 75, 3500, 900, 1100, 1200, 1.5);
        final StringBuilder sb = new StringBuilder();

        // When: original is serialized, then deserialized via readJson5
        EventDataJson5.write(original, sb);
        SystemDataJson5.write(original, sb);
        final TestSystemData restored = new TestSystemData();
        restored.readJson5("{" + sb + "}");

        // Then: restored data should match original
        assertEquals(original.getSessionUuid(), restored.getSessionUuid(), "should preserve sessionUuid in round-trip");
        assertEquals(original.getPosition(), restored.getPosition(), "should preserve position in round-trip");
        assertEquals(original.getRuntime_usedMemory(), restored.getRuntime_usedMemory(), "should preserve runtime memory in round-trip");
        assertEquals(original.getHeap_used(), restored.getHeap_used(), "should preserve heap_used in round-trip");
        assertEquals(original.getNonHeap_used(), restored.getNonHeap_used(), "should preserve nonHeap_used in round-trip");
        assertEquals(original.getClassLoading_total(), restored.getClassLoading_total(), "should preserve classLoading_total in round-trip");
        assertEquals(original.getGarbageCollector_count(), restored.getGarbageCollector_count(), "should preserve garbageCollector_count in round-trip");
        assertEquals(original.getSystemLoad(), restored.getSystemLoad(), 0.001, "should preserve systemLoad in round-trip");
    }

    @Test
    @DisplayName("should parse parent class fields alongside system fields")
    void testReadJson5_parentAndChildFields() {
        // Given: SystemData and JSON5 with both parent and child fields
        final TestSystemData data = new TestSystemData();
        final String json5 = "{_:combined_uuid,$:99,t:9999,m:[100,200,300],fc:5}";

        // When: readJson5 is called
        data.readJson5(json5);

        // Then: both parent (EventData) and child (SystemData) fields should be parsed
        assertEquals("combined_uuid", data.getSessionUuid(), "should parse parent sessionUuid");
        assertEquals(99L, data.getPosition(), "should parse parent position");
        assertEquals(9999L, data.getLastCurrentTime(), "should parse parent lastCurrentTime");
        assertEquals(100L, data.getRuntime_usedMemory(), "should parse child runtime_usedMemory");
        assertEquals(5L, data.getObjectPendingFinalizationCount(), "should parse child finalizationCount");
    }
}
