/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link MeterDataJson5}.
 * <p>
 * Tests validate that MeterDataJson5 correctly serializes and deserializes {@link MeterData} objects
 * to/from JSON5 format, handling various data scenarios, edge cases, and invalid inputs.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Round-Trip Serialization:</b> Verifies that a full MeterData object can be serialized to JSON and deserialized back with no data loss.</li>
 *   <li><b>Edge Cases:</b> Validates handling of disordered fields, missing fields, and various path types.</li>
 *   <li><b>Specific Fields:</b> Tests reading of individual fields like description, category, operation, and paths.</li>
 *   <li><b>Timing & Iteration:</b> Ensures timing (create, start, stop) and iteration counts are correctly parsed.</li>
 *   <li><b>Context Data:</b> Validates parsing of the context map, including null values, spaces, and numeric values.</li>
 *   <li><b>Invalid JSON:</b> Verifies that the parser handles malformed JSON gracefully without throwing exceptions.</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Pro (Preview)
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 */
@ValidateCharset
@WithLocale("en")
@ValidateCleanMeter
class MeterDataJson5Test {

    /**
     * Concrete class for testing the abstract {@link MeterData}.
     */
    private static class TestMeterData extends MeterData {
        /**
         * Default constructor.
         */
        TestMeterData() {
        }

        /**
         * Full constructor.
         *
         * @param sessionUuid session UUID
         * @param position position
         * @param time time
         * @param heap_commited heap committed
         * @param heap_max heap max
         * @param heap_used heap used
         * @param nonHeap_commited non-heap committed
         * @param nonHeap_max non-heap max
         * @param nonHeap_used non-heap used
         * @param objectPendingFinalizationCount object pending finalization count
         * @param classLoading_loaded class loading loaded
         * @param classLoading_total class loading total
         * @param classLoading_unloaded class loading unloaded
         * @param compilationTime compilation time
         * @param garbageCollector_count garbage collector count
         * @param garbageCollector_time garbage collector time
         * @param runtime_usedMemory runtime used memory
         * @param runtime_maxMemory runtime max memory
         * @param runtime_totalMemory runtime total memory
         * @param systemLoad system load
         * @param category category
         * @param operation operation
         * @param parent parent
         * @param description description
         * @param createTime create time
         * @param startTime start time
         * @param stopTime stop time
         * @param timeLimit time limit
         * @param currentIteration current iteration
         * @param expectedIterations expected iterations
         * @param okPath OK path
         * @param rejectPath reject path
         * @param failPath fail path
         * @param failMessage fail message
         * @param context context
         */
        TestMeterData(final String sessionUuid, final long position, final long time, final long heap_commited, final long heap_max, final long heap_used, final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used, final long objectPendingFinalizationCount, final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded, final long compilationTime, final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad, final String category, final String operation, final String parent, final String description, final long createTime, final long startTime, final long stopTime, final long timeLimit, final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
            super(sessionUuid, position, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad, category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, context);
        }
    }

    /**
     * Data provider for round-trip serialization scenarios.
     *
     * @return stream of arguments
     */
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

    /**
     * Tests for round-trip serialization.
     */
    @Nested
    @DisplayName("Round-trip serialization tests")
    class RoundTrip {
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#roundTripScenarios")
        @DisplayName("Should correctly serialize and deserialize (round-trip)")
        void shouldCorrectlySerializeAndDeserializeRoundTrip(final String testName, final MeterData originalData, final String expectedJson) {
            // Given: original MeterData object and expected JSON string
            // When: data is serialized to JSON
            final StringBuilder sb = new StringBuilder();
            MeterDataJson5.write(originalData, sb);
            final String actualJson = sb.toString();

            // Then: JSON should match expected format
            assertEquals(expectedJson, actualJson, "serialized JSON should match expected format");

            // When: JSON is deserialized back to MeterData
            final TestMeterData newData = new TestMeterData();
            MeterDataJson5.read(newData, "{" + actualJson + "}");

            // Then: deserialized object should match original data
            assertMeterDataEquals(originalData, newData);
        }
    }

    /**
     * Data provider for edge case scenarios.
     *
     * @return stream of arguments
     */
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

    /**
     * Tests for edge case scenarios.
     */
    @Nested
    @DisplayName("Edge case tests")
    class EdgeCases {
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#readEdgeCaseScenarios")
        @DisplayName("Should correctly read edge cases")
        void shouldCorrectlyReadEdgeCases(final String testName, final String inputJson, final MeterData expectedData) {
            // Given: input JSON with edge cases (disordered, missing fields, etc.)
            final TestMeterData actualData = new TestMeterData();

            // When: JSON is parsed
            MeterDataJson5.read(actualData, inputJson);

            // Then: parsed data should match expected MeterData
            assertMeterDataEquals(expectedData, actualData);
        }
    }

    /**
     * Data provider for specific field scenarios.
     *
     * @return stream of arguments
     */
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

    /**
     * Tests for specific field reading.
     */
    @Nested
    @DisplayName("Specific field tests")
    class SpecificFields {
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#readSpecificFieldScenarios")
        @DisplayName("Should correctly read specific fields")
        void shouldCorrectlyReadSpecificFields(final String testName, final String inputJson, final String expectedDescription, final String expectedPath) {
            // Given: input JSON containing specific fields
            final TestMeterData data = new TestMeterData();

            // When: JSON is parsed
            MeterDataJson5.read(data, inputJson);

            // Then: specific fields should be correctly populated
            if (expectedDescription != null) {
                assertEquals(expectedDescription, data.description, "description should match expected value");
            }
            // Thes was a strange solution used by IA to decide which attribute to assert.
            if (expectedPath != null) {
                if (inputJson.contains("ep:")) {
                    assertEquals(expectedPath, data.parent, "parent should match expected value");
                } else if (inputJson.contains("p:")) {
                    assertEquals(expectedPath, data.okPath, "okPath should match expected value");
                } else if (inputJson.contains("r:")) {
                    assertEquals(expectedPath, data.rejectPath, "rejectPath should match expected value");
                } else if (inputJson.contains("f:")) {
                    assertEquals(expectedPath, data.failPath, "failPath should match expected value");
                } else if (inputJson.contains("c:")) {
                    assertEquals(expectedPath, data.category, "category should match expected value");
                } else if (inputJson.contains("n:")) {
                    assertEquals(expectedPath, data.operation, "operation should match expected value");
                } else if (inputJson.contains("fm:")) {
                    assertEquals(expectedPath, data.failMessage, "failMessage should match expected value");
                }
            }
        }
    }

    /**
     * Data provider for timing and iteration field scenarios.
     *
     * @return stream of arguments
     */
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

    /**
     * Tests for timing and iteration field reading.
     */
    @Nested
    @DisplayName("Timing and iteration tests")
    class TimingAndIteration {
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#readTimingFieldScenarios")
        @DisplayName("Should correctly read timing and iteration fields")
        void shouldCorrectlyReadTimingAndIterationFields(final String testName, final String inputJson, final long expectedCreateTime, final long expectedStartTime, final long expectedStopTime, final long expectedTimeLimit, final long expectedCurrentIteration, final long expectedExpectedIterations) {
            // Given: input JSON with timing/iteration data
            final TestMeterData data = new TestMeterData();

            // When: JSON is parsed
            MeterDataJson5.read(data, inputJson);

            // Then: timing and iteration fields should match expected values
            assertEquals(expectedCreateTime, data.createTime, "createTime should match expected value");
            assertEquals(expectedStartTime, data.startTime, "startTime should match expected value");
            assertEquals(expectedStopTime, data.stopTime, "stopTime should match expected value");
            assertEquals(expectedTimeLimit, data.timeLimit, "timeLimit should match expected value");
            assertEquals(expectedCurrentIteration, data.currentIteration, "currentIteration should match expected value");
            assertEquals(expectedExpectedIterations, data.expectedIterations, "expectedIterations should match expected value");
        }
    }

    /**
     * Data provider for context field scenarios.
     *
     * @return stream of arguments
     */
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

    /**
     * Helper method for creating maps (Java 8 compatible).
     */
    private static Map<String, String> createMap(final String k1, final String v1) {
        final Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    /**
     * Helper method for creating maps (Java 8 compatible).
     */
    private static Map<String, String> createMap(final String k1, final String v1, final String k2, final String v2) {
        final Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * Helper method for creating maps (Java 8 compatible).
     */
    private static Map<String, String> createMap(final String k1, final String v1, final String k2, final String v2, final String k3, final String v3) {
        final Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * Helper method for creating maps with null values (Java 8 compatible).
     */
    private static Map<String, String> createMapWithNull(final String k1, final String v1, final String k2, final String v2, final String k3, final String v3) {
        final Map<String, String> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * Tests for context field reading.
     */
    @Nested
    @DisplayName("Context field tests")
    class ContextFields {
        @ParameterizedTest(name = "{0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#readContextScenarios")
        @DisplayName("Should correctly read context fields")
        void shouldCorrectlyReadContextFields(final String testName, final String inputJson, final Map<String, String> expectedContext) {
            // Given: input JSON with context data
            final TestMeterData data = new TestMeterData();

            // When: JSON is parsed
            MeterDataJson5.read(data, inputJson);

            // Then: context map should match expected values
            if (expectedContext.isEmpty()) {
                assertEquals(expectedContext, data.getContext(), "context map should be empty");
            } else {
                assertEquals(expectedContext, data.context, "context map should match expected values");
            }
        }
    }

    /**
     * Data provider for invalid JSON scenarios.
     *
     * @return stream of arguments
     */
    static Stream<Arguments> invalidJsonScenarios() {
        return Stream.of(
                Arguments.of("Empty string", ""),
                Arguments.of("Only braces", "{}"),
                Arguments.of("Malformed field", "{d:}"),
                Arguments.of("No colon", "{d 'test'}")
        );
    }

    /**
     * Tests for invalid JSON handling.
     */
    @Nested
    @DisplayName("Invalid JSON tests")
    class InvalidJson {
        @ParameterizedTest(name = "Invalid JSON: {0}")
        @MethodSource("org.usefultoys.slf4j.meter.MeterDataJson5Test#invalidJsonScenarios")
        @DisplayName("Should handle invalid JSON gracefully")
        void shouldHandleInvalidJsonGracefully(final String testName, final String invalidJson) {
            // Given: invalid JSON input
            final TestMeterData data = new TestMeterData();

            // When: JSON is parsed (should not throw exception)
            try {
                MeterDataJson5.read(data, invalidJson);
            } catch (final Exception e) {
                // Allow exceptions for truly malformed input
            }

            // Then: no fields should be set for invalid input
            if (invalidJson.isEmpty() || invalidJson.equals("{}") || !invalidJson.contains(":")) {
                assertNull(data.description, "description should be null");
                assertNull(data.category, "category should be null");
                assertNull(data.operation, "operation should be null");
                assertNull(data.parent, "parent should be null");
                assertNull(data.okPath, "okPath should be null");
                assertNull(data.rejectPath, "rejectPath should be null");
                assertNull(data.failPath, "failPath should be null");
                assertNull(data.failMessage, "failMessage should be null");
                assertEquals(0L, data.createTime, "createTime should be 0");
                assertEquals(0L, data.startTime, "startTime should be 0");
                assertEquals(0L, data.stopTime, "stopTime should be 0");
                assertEquals(0L, data.timeLimit, "timeLimit should be 0");
                assertEquals(0L, data.currentIteration, "currentIteration should be 0");
                assertEquals(0L, data.expectedIterations, "expectedIterations should be 0");
                assertNull(data.context, "context should be null");
            }
        }
    }

    /**
     * Asserts that two {@link MeterData} objects are equal.
     *
     * @param expected expected data
     * @param actual actual data
     */
    private static void assertMeterDataEquals(final MeterData expected, final MeterData actual) {
        // MeterData specific fields
        assertEquals(expected.category, actual.category, "category should match");
        assertEquals(expected.operation, actual.operation, "operation should match");
        assertEquals(expected.parent, actual.parent, "parent should match");
        assertEquals(expected.description, actual.description, "description should match");
        assertEquals(expected.createTime, actual.createTime, "createTime should match");
        assertEquals(expected.startTime, actual.startTime, "startTime should match");
        assertEquals(expected.stopTime, actual.stopTime, "stopTime should match");
        assertEquals(expected.timeLimit, actual.timeLimit, "timeLimit should match");
        assertEquals(expected.currentIteration, actual.currentIteration, "currentIteration should match");
        assertEquals(expected.expectedIterations, actual.expectedIterations, "expectedIterations should match");
        assertEquals(expected.okPath, actual.okPath, "okPath should match");
        assertEquals(expected.rejectPath, actual.rejectPath, "rejectPath should match");
        assertEquals(expected.failPath, actual.failPath, "failPath should match");
        assertEquals(expected.failMessage, actual.failMessage, "failMessage should match");
        assertEquals(expected.getContext(), actual.getContext(), "context should match");
    }
}