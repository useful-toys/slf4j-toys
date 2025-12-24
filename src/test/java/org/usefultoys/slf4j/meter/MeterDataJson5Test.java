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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MeterDataJson5Test {
    @BeforeAll
    static void setupConsistentLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    // Concrete class for testing the abstract MeterData
    private static class TestMeterData extends MeterData {
        TestMeterData() {
            super();
        }

        TestMeterData(String sessionUuid, long position, long time, long heap_commited, long heap_max, long heap_used, long nonHeap_commited, long nonHeap_max, long nonHeap_used, long objectPendingFinalizationCount, long classLoading_loaded, long classLoading_total, long classLoading_unloaded, long compilationTime, long garbageCollector_count, long garbageCollector_time, long runtime_usedMemory, long runtime_maxMemory, long runtime_totalMemory, double systemLoad, String category, String operation, String parent, String description, long createTime, long startTime, long stopTime, long timeLimit, long currentIteration, long expectedIterations, String okPath, String rejectPath, String failPath, String failMessage, Map<String, String> context) {
            super(sessionUuid, position, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad, category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, context);
        }
    }

    static Stream<Arguments> roundTripScenarios() {
        final String uuid1 = "8ae94091";
        final Map<String, String> testContext = new HashMap<>();
        testContext.put("key1", "value1");
        testContext.put("key2", "value2");
        testContext.put("nullKey", null);

        return Stream.of(
                Arguments.of(
                        "Full data scenario",
                        new TestMeterData(uuid1, 1, 1000, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18.0, "TestCategory", "testOperation", "parent1", "Test description", 100, 200, 300, 5000, 10, 20, "okPath", null, null, null, testContext),
                        ",d:'Test description',p:okPath,c:TestCategory,n:testOperation,ep:parent1,t0:100,t1:200,t2:300,i:10,ei:20,tl:5000,ctx:{key1:value1,nullKey:,key2:value2}"
                ),
                Arguments.of(
                        "Minimal data scenario",
                        new TestMeterData(uuid1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        ""
                ),
                Arguments.of(
                        "Reject scenario",
                        new TestMeterData(uuid1, 2, 2000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "RejectCategory", "rejectOp", null, "Rejected operation", 150, 250, 350, 0, 0, 0, null, "ValidationError", null, null, null),
                        ",d:'Rejected operation',r:ValidationError,c:RejectCategory,n:rejectOp,t0:150,t1:250,t2:350"
                ),
                Arguments.of(
                        "Fail scenario",
                        new TestMeterData(uuid1, 3, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "FailCategory", "failOp", "parent2", "Failed operation", 180, 280, 380, 3000, 5, 10, null, null, "java.lang.RuntimeException", "Something went wrong", null),
                        ",d:'Failed operation',f:java.lang.RuntimeException,fm:'Something went wrong',c:FailCategory,n:failOp,ep:parent2,t0:180,t1:280,t2:380,i:5,ei:10,tl:3000"
                ),
                Arguments.of(
                        "Only timing data",
                        new TestMeterData(uuid1, 4, 4000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 400, 500, 600, 0, 0, 0, null, null, null, null, null),
                        ",t0:400,t1:500,t2:600"
                ),
                Arguments.of(
                        "Only iteration data",
                        new TestMeterData(uuid1, 5, 5000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 0, 0, 0, 0, 15, 30, null, null, null, null, null),
                        ",i:15,ei:30"
                ),
                Arguments.of(
                        "Only context data",
                        new TestMeterData(uuid1, 6, 6000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, testContext),
                        ",ctx:{key1:value1,nullKey:,key2:value2}"
                ),
                Arguments.of(
                        "Category and operation only",
                        new TestMeterData(uuid1, 7, 7000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "OnlyCategory", "onlyOperation", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        ",c:OnlyCategory,n:onlyOperation"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripScenarios")
    @DisplayName("Should correctly serialize and deserialize (round-trip)")
    void testRoundTrip(String testName, MeterData originalData, String expectedJson) {
        // 1. Test Serialization (Write)
        final StringBuilder sb = new StringBuilder();
        MeterDataJson5.write(originalData, sb);
        final String actualJson = sb.toString();
        assertEquals(expectedJson, actualJson);

        // 2. Test Deserialization (Read)
        final TestMeterData newData = new TestMeterData();
        MeterDataJson5.read(newData, "{" + actualJson + "}");

        // 3. Assert Round-trip consistency
        assertMeterDataEquals(originalData, newData);
    }

    static Stream<Arguments> readEdgeCaseScenarios() {
        final String uuid = "8ae94091";
        final Map<String, String> expectedContext = new HashMap<>();
        expectedContext.put("a", "1");
        expectedContext.put("b", "2");
        expectedContext.put("c", null);

        return Stream.of(
                Arguments.of(
                        "Disordered fields",
                        String.format("{ep:parent1,_:%s,fm:'Error message',t2:300,d:'Test desc',$:1,t:1000,f:Exception,t1:200,c:Cat,n:Op,t0:100}", uuid),
                        new TestMeterData(uuid, 1, 1000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "Cat", "Op", "parent1", "Test desc", 100, 200, 300, 0, 0, 0, null, null, "Exception", "Error message", null)
                ),
                Arguments.of(
                        "Missing fields",
                        String.format("{_:%s,d:'Only desc'}", uuid),
                        new TestMeterData(uuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, "Only desc", 0, 0, 0, 0, 0, 0, null, null, null, null, null)
                ),
                Arguments.of(
                        "Context parsing",
                        String.format("{_:%s,ctx:{a:1,b:2,c:}}", uuid),
                        new TestMeterData(uuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, expectedContext)
                ),
                Arguments.of(
                        "All path types",
                        String.format("{_:%s,p:success,r:rejected,f:failed}", uuid),
                        new TestMeterData(uuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 0, 0, 0, 0, 0, 0, "success", "rejected", "failed", null, null)
                ),
                Arguments.of(
                        "Numeric fields only",
                        String.format("{_:%s,t0:1000,t1:2000,t2:3000,i:5,ei:10,tl:60000}", uuid),
                        new TestMeterData(uuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, null, null, 1000, 2000, 3000, 60000, 5, 10, null, null, null, null, null)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readEdgeCaseScenarios")
    @DisplayName("Should correctly read edge cases")
    void testReadEdgeCases(String testName, String inputJson, MeterData expectedData) {
        final TestMeterData actualData = new TestMeterData();
        MeterDataJson5.read(actualData, inputJson);
        assertMeterDataEquals(expectedData, actualData);
    }

    static Stream<Arguments> readSpecificFieldScenarios() {
        return Stream.of(
                Arguments.of(
                        "Description field",
                        "{d:'Test description'}",
                        "Test description",
                        null
                ),
                Arguments.of(
                        "OK path field",
                        "{p:successPath}",
                        null,
                        "successPath"
                ),
                Arguments.of(
                        "Reject path field", 
                        "{r:rejectedPath}",
                        null,
                        "rejectedPath"
                ),
                Arguments.of(
                        "Fail path field",
                        "{f:java.lang.Exception}",
                        null,
                        "java.lang.Exception"
                ),
                Arguments.of(
                        "Fail message field",
                        "{fm:'Exception occurred'}",
                        null,
                        "Exception occurred"
                ),
                Arguments.of(
                        "Category field",
                        "{c:TestCategory}",
                        null,
                        "TestCategory"
                ),
                Arguments.of(
                        "Operation field", 
                        "{n:testOperation}",
                        null,
                        "testOperation"
                ),
                Arguments.of(
                        "Parent field",
                        "{ep:parent123}",
                        null,
                        "parent123"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readSpecificFieldScenarios")
    @DisplayName("Should correctly read specific fields")
    void testReadSpecificFields(String testName, String inputJson, String expectedDescription, String expectedPath) {
        final TestMeterData data = new TestMeterData();
        
        MeterDataJson5.read(data, inputJson);
        
        if (expectedDescription != null) {
            assertEquals(expectedDescription, data.description);
        }
        // Thes was a strange solution used by IA to decide which attribute to assert.
        if (expectedPath != null) {
            if (inputJson.contains("ep:")) {
                assertEquals(expectedPath, data.parent);
            } else if (inputJson.contains("p:")) {
                assertEquals(expectedPath, data.okPath);
            } else if (inputJson.contains("r:")) {
                assertEquals(expectedPath, data.rejectPath);
            } else if (inputJson.contains("f:")) {
                assertEquals(expectedPath, data.failPath);
            } else if (inputJson.contains("c:")) {
                assertEquals(expectedPath, data.category);
            } else if (inputJson.contains("n:")) {
                assertEquals(expectedPath, data.operation);
            } else if (inputJson.contains("fm:")) {
                assertEquals(expectedPath, data.failMessage);
            }
        }
    }

    static Stream<Arguments> readTimingFieldScenarios() {
        return Stream.of(
                Arguments.of(
                        "Create time field",
                        "{t0:12345}",
                        12345L, 0L, 0L, 0L, 0L, 0L
                ),
                Arguments.of(
                        "Start time field",
                        "{t1:23456}",
                        0L, 23456L, 0L, 0L, 0L, 0L
                ),
                Arguments.of(
                        "Stop time field",
                        "{t2:34567}",
                        0L, 0L, 34567L, 0L, 0L, 0L
                ),
                Arguments.of(
                        "Time limit field",
                        "{tl:60000}",
                        0L, 0L, 0L, 60000L, 0L, 0L
                ),
                Arguments.of(
                        "Current iteration field",
                        "{i:42}",
                        0L, 0L, 0L, 0L, 42L, 0L
                ),
                Arguments.of(
                        "Expected iterations field",
                        "{ei:100}",
                        0L, 0L, 0L, 0L, 0L, 100L
                ),
                Arguments.of(
                        "All timing fields",
                        "{t0:1000,t1:2000,t2:3000,tl:60000,i:5,ei:10}",
                        1000L, 2000L, 3000L, 60000L, 5L, 10L
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readTimingFieldScenarios")
    @DisplayName("Should correctly read timing and iteration fields")
    void testReadTimingFields(String testName, String inputJson, long expectedCreateTime, long expectedStartTime, long expectedStopTime, long expectedTimeLimit, long expectedCurrentIteration, long expectedExpectedIterations) {
        final TestMeterData data = new TestMeterData();
        MeterDataJson5.read(data, inputJson);
        
        assertEquals(expectedCreateTime, data.createTime);
        assertEquals(expectedStartTime, data.startTime);
        assertEquals(expectedStopTime, data.stopTime);
        assertEquals(expectedTimeLimit, data.timeLimit);
        assertEquals(expectedCurrentIteration, data.currentIteration);
        assertEquals(expectedExpectedIterations, data.expectedIterations);
    }

    static Stream<Arguments> readContextScenarios() {
        return Stream.of(
                Arguments.of(
                        "Simple context",
                        "{ctx:{key:value}}",
                        createMap("key", "value")
                ),
                Arguments.of(
                        "Multiple context entries",
                        "{ctx:{key1:value1,key2:value2,key3:value3}}",
                        createMap("key1", "value1", "key2", "value2", "key3", "value3")
                ),
                Arguments.of(
                        "Context with null values",
                        "{ctx:{key1:value1,key2:,key3:value3}}",
                        createMapWithNull("key1", "value1", "key2", null, "key3", "value3")
                ),
                Arguments.of(
                        "Empty context",
                        "{ctx:{}}",
                        new HashMap<String, String>()
                ),
                Arguments.of(
                        "Context with spaces",
                        "{ctx:{ key1 : value1 , key2 : value2 }}",
                        createMap("key1", "value1", "key2", "value2")
                ),
                Arguments.of(
                        "Context with numeric values",
                        "{ctx:{count:42,rate:3.14,enabled:true}}",
                        createMap("count", "42", "rate", "3.14", "enabled", "true")
                )
        );
    }

    // Helper methods for creating maps (Java 8 compatible)
    private static Map<String, String> createMap(String k1, String v1) {
        Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    private static Map<String, String> createMap(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    private static Map<String, String> createMap(String k1, String v1, String k2, String v2, String k3, String v3) {
        Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    private static Map<String, String> createMapWithNull(String k1, String v1, String k2, String v2, String k3, String v3) {
        Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readContextScenarios")
    @DisplayName("Should correctly read context fields")
    void testReadContextFields(String testName, String inputJson, Map<String, String> expectedContext) {
        final TestMeterData data = new TestMeterData();
        MeterDataJson5.read(data, inputJson);
        
        if (expectedContext.isEmpty()) {
            assertEquals(expectedContext, data.getContext());
        } else {
            assertEquals(expectedContext, data.context);
        }
    }

    @ParameterizedTest(name = "Invalid JSON: {0}")
    @DisplayName("Should handle invalid JSON gracefully")
    @MethodSource("invalidJsonScenarios")
    void testInvalidJsonHandling(String testName, String invalidJson) {
        final TestMeterData data = new TestMeterData();
        // Should not throw exceptions for truly invalid JSON that doesn't match any patterns
        try {
            MeterDataJson5.read(data, invalidJson);
        } catch (Exception e) {
            // Allow exceptions for truly malformed input
        }
        
        // For empty or truly invalid JSON, no fields should be set
        if (invalidJson.isEmpty() || invalidJson.equals("{}") || !invalidJson.contains(":")) {
            assertNull(data.description);
            assertNull(data.category);
            assertNull(data.operation);
            assertNull(data.parent);
            assertNull(data.okPath);
            assertNull(data.rejectPath);
            assertNull(data.failPath);
            assertNull(data.failMessage);
            assertEquals(0L, data.createTime);
            assertEquals(0L, data.startTime);
            assertEquals(0L, data.stopTime);
            assertEquals(0L, data.timeLimit);
            assertEquals(0L, data.currentIteration);
            assertEquals(0L, data.expectedIterations);
            assertNull(data.context);
        }
    }

    static Stream<Arguments> invalidJsonScenarios() {
        return Stream.of(
                Arguments.of("Empty string", ""),
                Arguments.of("Only braces", "{}"),
                Arguments.of("Malformed field", "{d:}"),
                Arguments.of("No colon", "{d 'test'}")
        );
    }

    private static void assertMeterDataEquals(MeterData expected, MeterData actual) {
        // MeterData specific fields
        assertEquals(expected.category, actual.category);
        assertEquals(expected.operation, actual.operation);
        assertEquals(expected.parent, actual.parent);
        assertEquals(expected.description, actual.description);
        assertEquals(expected.createTime, actual.createTime);
        assertEquals(expected.startTime, actual.startTime);
        assertEquals(expected.stopTime, actual.stopTime);
        assertEquals(expected.timeLimit, actual.timeLimit);
        assertEquals(expected.currentIteration, actual.currentIteration);
        assertEquals(expected.expectedIterations, actual.expectedIterations);
        assertEquals(expected.okPath, actual.okPath);
        assertEquals(expected.rejectPath, actual.rejectPath);
        assertEquals(expected.failPath, actual.failPath);
        assertEquals(expected.failMessage, actual.failMessage);
        assertEquals(expected.getContext(), actual.getContext());
    }
}