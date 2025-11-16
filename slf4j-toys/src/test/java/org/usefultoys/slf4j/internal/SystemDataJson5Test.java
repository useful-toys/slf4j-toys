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

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemDataJson5Test {

    // Concrete class for testing the abstract SystemData
    private static class TestSystemData extends SystemData {
        TestSystemData() {
            super();
        }

        TestSystemData(String sessionUuid, long position, long lastCurrentTime, long heap_commited, long heap_max, long heap_used, long nonHeap_commited, long nonHeap_max, long nonHeap_used, long objectPendingFinalizationCount, long classLoading_loaded, long classLoading_total, long classLoading_unloaded, long compilationTime, long garbageCollector_count, long garbageCollector_time, long runtime_usedMemory, long runtime_maxMemory, long runtime_totalMemory, double systemLoad) {
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
                        "Partial data scenario",
                        new TestSystemData(uuid1, 1, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 17, 0.0),
                        ",m:[15,17,16]"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripScenarios")
    @DisplayName("Should correctly serialize and deserialize (round-trip)")
    void testRoundTrip(String testName, SystemData originalData, String expectedJson) {
        // 1. Test Serialization (Write)
        final StringBuilder sb = new StringBuilder();
        SystemDataJson5.write(originalData, sb);
        final String actualJson = sb.toString();
        assertEquals(expectedJson, actualJson);

        // 2. Test Deserialization (Read)
        final TestSystemData newData = new TestSystemData();
        SystemDataJson5.read(newData, "{"+actualJson+"}");

        // 3. Assert Round-trip consistency
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
    @DisplayName("Should correctly read edge cases")
    void testReadEdgeCases(String testName, String inputJson, SystemData expectedData) {
        final TestSystemData actualData = new TestSystemData();
        SystemDataJson5.read(actualData, inputJson);
        assertSystemDataEquals(expectedData, actualData);
    }

    private static void assertSystemDataEquals(SystemData expected, SystemData actual) {
        // SystemData fields
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
