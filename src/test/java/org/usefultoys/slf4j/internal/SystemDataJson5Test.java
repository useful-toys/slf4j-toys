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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SystemDataJson5}.
 * <p>
 * Tests verify that SystemData can be correctly serialized to and deserialized from JSON5 format,
 * including round-trip consistency and edge case handling.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Round-trip Serialization:</b> Tests serialization to JSON5 and deserialization back to SystemData</li>
 *   <li><b>Edge Case Handling:</b> Verifies correct handling of null values, empty strings, and boundary conditions</li>
 *   <li><b>Consistency:</b> Ensures that serialize/deserialize maintains data integrity</li>
 * </ul>
 */
@DisplayName("SystemDataJson5")
@ValidateCharset
@WithLocale("en")
class SystemDataJson5Test {

    // Concrete class for testing the abstract SystemData
    private static class TestSystemData extends SystemData {
        TestSystemData() {
        }

        TestSystemData(final String sessionUuid, final long position, final long lastCurrentTime, final long heap_commited, final long heap_max, final long heap_used, final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used, final long objectPendingFinalizationCount, final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded, final long compilationTime, final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad) {
            super(sessionUuid, position, lastCurrentTime, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad);
        }
    }

    static Stream<Arguments> roundTripScenarios() {
        final String uuid1 = "8ae94091";
        return Stream.of(
                Arguments.of(
                        "Full data scenario",
                        new TestSystemData(uuid1, 1, 1000, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18.0),
                        ",m:[15,17,16],h:[4,2,3],nh:[7,5,6],fc:8,cl:[10,9,11],ct:12,gc:[13,14],sl:18.0"
                ),
                Arguments.of(
                        "Zero data scenario",
                        new TestSystemData(uuid1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0),
                        ""
                ),
                Arguments.of(
                        "Partial data: only garbageCollector_time",
                        new TestSystemData(uuid1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0.0),
                        ",gc:[0,14]"
                ),
                Arguments.of(
                        "Partial data: only systemLoad",
                        new TestSystemData(uuid1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18.0),
                        ",sl:18.0"
                ),
                Arguments.of(
                        "Combination: Runtime Memory + System Load",
                        new TestSystemData(uuid1, 1, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 17, 18.0),
                        ",m:[15,17,16],sl:18.0"
                ),
                Arguments.of(
                        "Combination: Heap + Class Loading",
                        new TestSystemData(uuid1, 1, 1000, 2, 3, 4, 0, 0, 0, 0, 9, 10, 11, 0, 0, 0, 0, 0, 0, 0.0),
                        ",h:[4,2,3],cl:[10,9,11]"
                ),
                Arguments.of(
                        "Combination: Non-Heap + GC",
                        new TestSystemData(uuid1, 1, 1000, 0, 0, 0, 5, 6, 7, 0, 0, 0, 0, 0, 13, 14, 0, 0, 0, 0.0),
                        ",nh:[7,5,6],gc:[13,14]"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripScenarios")
    @DisplayName("should correctly serialize and deserialize (round-trip)")
    void testRoundTrip(final String testName, final SystemData originalData, final String expectedJson) {
        // Given: SystemData with various field combinations
        final StringBuilder sb = new StringBuilder();

        // When: data is serialized to JSON5
        SystemDataJson5.write(originalData, sb);
        final String actualJson = sb.toString();

        // Then: serialized JSON should match expected format
        assertEquals(expectedJson, actualJson, "Serialized JSON should match expected format for: " + testName);

        // When: serialized data is deserialized
        final TestSystemData newData = new TestSystemData();
        SystemDataJson5.read(newData, "{"+actualJson+"}");

        // Then: round-trip should preserve all data
        assertSystemDataEquals(originalData, newData);
    }

    static Stream<Arguments> readEdgeCaseScenarios() {
        final String uuid = "8ae94091";
        return Stream.of(
                Arguments.of(
                        "Disordered fields",
                        String.format("{sl:18.0,m:[15,17,16],_:%s,ct:12,t:1000,h:[4,2,3],$:1,nh:[7,5,6],fc:8,cl:[10,9,11],gc:[13,14]}", uuid),
                        new TestSystemData(uuid, 1, 1000, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18.0)
                ),
                Arguments.of(
                        "Missing fields",
                        String.format("{_:%s,m:[1,2,3]}", uuid),
                        new TestSystemData(uuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 2, 0.0)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readEdgeCaseScenarios")
    @DisplayName("should correctly read edge cases")
    void testReadEdgeCases(final String testName, final String inputJson, final SystemData expectedData) {
        // Given: JSON5 with edge case formatting
        final TestSystemData actualData = new TestSystemData();

        // When: JSON5 is deserialized
        SystemDataJson5.read(actualData, inputJson);

        // Then: should handle edge cases correctly
        assertSystemDataEquals(expectedData, actualData);
    }

    private static void assertSystemDataEquals(final SystemData expected, final SystemData actual) {
        // ...existing assertions...
        assertEquals(expected.getHeap_commited(), actual.getHeap_commited());
        assertEquals(expected.getHeap_max(), actual.getHeap_max());
        assertEquals(expected.getHeap_used(), actual.getHeap_used());
        assertEquals(expected.getNonHeap_commited(), actual.getNonHeap_commited());
        assertEquals(expected.getNonHeap_max(), actual.getNonHeap_max());
        assertEquals(expected.getNonHeap_used(), actual.getNonHeap_used());
        assertEquals(expected.getObjectPendingFinalizationCount(), actual.getObjectPendingFinalizationCount());
        assertEquals(expected.getClassLoading_loaded(), actual.getClassLoading_loaded());
        assertEquals(expected.getClassLoading_total(), actual.getClassLoading_total());
        assertEquals(expected.getClassLoading_unloaded(), actual.getClassLoading_unloaded());
        assertEquals(expected.getCompilationTime(), actual.getCompilationTime());
        assertEquals(expected.getGarbageCollector_count(), actual.getGarbageCollector_count());
        assertEquals(expected.getGarbageCollector_time(), actual.getGarbageCollector_time());
        assertEquals(expected.getRuntime_usedMemory(), actual.getRuntime_usedMemory());
        assertEquals(expected.getRuntime_maxMemory(), actual.getRuntime_maxMemory());
        assertEquals(expected.getRuntime_totalMemory(), actual.getRuntime_totalMemory());
        assertEquals(expected.getSystemLoad(), actual.getSystemLoad());
    }
}
