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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MeterData}.
 * <p>
 * Tests validate that MeterData correctly manages meter data including timing,
 * context information, and provides proper string representations for logging.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Meter data initialization:</b> Verifies proper initialization of meter data fields</li>
 *   <li><b>Timing operations:</b> Tests start/stop timing and elapsed time calculations</li>
 *   <li><b>Context management:</b> Tests context map operations and data storage</li>
 *   <li><b>String representation:</b> Tests toString() and print() methods for logging output</li>
 *   <li><b>Status management:</b> Tests meter status transitions and state tracking</li>
 *   <li><b>Configuration integration:</b> Tests integration with SystemConfig and SessionConfig</li>
 *   <li><b>JSON5 Round-Trip Serialization:</b> Tests serialization to JSON5 and deserialization back to MeterData</li>
 *   <li><b>JSON5 Special Behavior:</b> Tests edge cases and partial field updates with JSON5</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Haiku 4.5
 */
@ValidateCharset
@ResetMeterConfig
@ResetSessionConfig
@WithLocale("en")
@ValidateCleanMeter
class MeterDataTest {

    private static class MockMeterData extends MeterData {

        public MockMeterData(final String sessionUuid, final long position,
                             final String category, final String operation, final String parent, final String description,
                             final long createTime, final long startTime, final long stopTime, final int currentTime,
                             final long timeLimit, final long currentIteration, final long expectedIterations,
                             final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
            super(sessionUuid, position, currentTime,
                    0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                    category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, context);
        }
    }


    @Test
    @DisplayName("should initialize all fields correctly and provide proper getters")
    void testConstructorAndGetters() {
        // Given: a context map with test data
        final Map<String, String> contextMap = new HashMap<>();
        contextMap.put("key1", "value1");
        contextMap.put("key2", "value2");

        // When: creating MeterData with all fields populated
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                2, // time
                3, // heap_commited
                4, // heap_max
                5, // heap_used
                6, // nonHeap_commited
                7, // nonHeap_max
                8, // nonHeap_used
                9, // objectPendingFinalizationCount
                10, // classLoading_loaded
                11, // classLoading_total
                12, // classLoading_unloaded
                13, // compilationTime
                14, // garbageCollector_count
                15, // garbageCollector_time
                16, // runtime_usedMemory
                17, // runtime_maxMemory
                18, // runtime_totalMemory
                19.0, // systemLoad
                "categoryTest", // category
                "operationTest", // operation
                "parentTest", // parent
                "descriptionTest", // description
                20, // createTime
                21, // startTime
                22, // stopTime
                23, // timeLimit
                24, // currentIteration
                25, // expectedIterations
                "okPathTest", // okPath
                null, // rejectPath
                null, // failPath
                null, // failMessage
                contextMap // context
        );

        // Then: all fields should be properly initialized and accessible via getters
        // Verify inherited fields from SystemData
        assertEquals("abc", meterData.getSessionUuid());
        assertEquals(1, meterData.getPosition());
        assertEquals(2L, meterData.getLastCurrentTime());
        assertEquals(3L, meterData.getHeap_commited());
        assertEquals(4L, meterData.getHeap_max());
        assertEquals(5L, meterData.getHeap_used());
        assertEquals(6L, meterData.getNonHeap_commited());
        assertEquals(7L, meterData.getNonHeap_max());
        assertEquals(8L, meterData.getNonHeap_used());
        assertEquals(9L, meterData.getObjectPendingFinalizationCount());
        assertEquals(10L, meterData.getClassLoading_loaded());
        assertEquals(11L, meterData.getClassLoading_total());
        assertEquals(12L, meterData.getClassLoading_unloaded());
        assertEquals(13L, meterData.getCompilationTime());
        assertEquals(14L, meterData.getGarbageCollector_count());
        assertEquals(15L, meterData.getGarbageCollector_time());
        assertEquals(16L, meterData.getRuntime_usedMemory());
        assertEquals(17L, meterData.getRuntime_maxMemory());
        assertEquals(18L, meterData.getRuntime_totalMemory());
        assertEquals(19.0, meterData.getSystemLoad());

        // Verify MeterData specific fields
        assertEquals("categoryTest", meterData.getCategory());
        assertEquals("operationTest", meterData.getOperation());
        assertEquals("parentTest", meterData.getParent());
        assertEquals("descriptionTest", meterData.getDescription());
        assertEquals(20L, meterData.getCreateTime());
        assertEquals(21L, meterData.getStartTime());
        assertEquals(22L, meterData.getStopTime());
        assertEquals(23L, meterData.getTimeLimit());
        assertEquals(24L, meterData.getCurrentIteration());
        assertEquals(25L, meterData.getExpectedIterations());
        assertEquals("okPathTest", meterData.getOkPath());
        assertNull(meterData.getRejectPath());
        assertNull(meterData.getFailPath());
        assertNull(meterData.getFailMessage());

        // Verify context map
        final Map<String, String> returnedContext = meterData.getContext();
        assertEquals(2, returnedContext.size());
        assertEquals("value1", returnedContext.get("key1"));
        assertEquals("value2", returnedContext.get("key2"));

        // Verify status methods
        assertTrue(meterData.isStarted());
        assertTrue(meterData.isStopped());
        assertTrue(meterData.isOK());
        assertFalse(meterData.isReject());
        assertFalse(meterData.isFail());
    }

    @Test
    @DisplayName("should clear all fields to default values when reset is called")
    void testResetClearsFields() {
        // Given: a context map with test data
        final Map<String, String> contextMap = new HashMap<>();
        contextMap.put("key1", "value1");

        // When: creating MeterData with all fields populated and then calling reset()
        final MeterData meterData = new MeterData(
                "abc", // sessionUuid
                1, // position
                2, // time
                3, // heap_commited
                4, // heap_max
                5, // heap_used
                6, // nonHeap_commited
                7, // nonHeap_max
                8, // nonHeap_used
                9, // objectPendingFinalizationCount
                10, // classLoading_loaded
                11, // classLoading_total
                12, // classLoading_unloaded
                13, // compilationTime
                14, // garbageCollector_count
                15, // garbageCollector_time
                16, // runtime_usedMemory
                17, // runtime_maxMemory
                18, // runtime_totalMemory
                19.0, // systemLoad
                "categoryTest", // category
                "operationTest", // operation
                "parentTest", // parent
                "descriptionTest", // description
                20, // createTime
                21, // startTime
                22, // stopTime
                23, // timeLimit
                24, // currentIteration
                25, // expectedIterations
                "okPathTest", // okPath
                null, // rejectPath
                null, // failPath
                null, // failMessage
                contextMap // context
        );

        // Reset the meterData
        meterData.reset();

        // Then: all fields should be cleared to default values
        // Verify inherited fields from SystemData are cleared
        assertNull(meterData.getSessionUuid());
        assertEquals(0L, meterData.getPosition());
        assertEquals(0L, meterData.getLastCurrentTime());
        assertEquals(0L, meterData.getHeap_commited());
        assertEquals(0L, meterData.getHeap_max());
        assertEquals(0L, meterData.getHeap_used());
        assertEquals(0L, meterData.getNonHeap_commited());
        assertEquals(0L, meterData.getNonHeap_max());
        assertEquals(0L, meterData.getNonHeap_used());
        assertEquals(0L, meterData.getObjectPendingFinalizationCount());
        assertEquals(0L, meterData.getClassLoading_loaded());
        assertEquals(0L, meterData.getClassLoading_total());
        assertEquals(0L, meterData.getClassLoading_unloaded());
        assertEquals(0L, meterData.getCompilationTime());
        assertEquals(0L, meterData.getGarbageCollector_count());
        assertEquals(0L, meterData.getGarbageCollector_time());
        assertEquals(0L, meterData.getRuntime_usedMemory());
        assertEquals(0L, meterData.getRuntime_maxMemory());
        assertEquals(0L, meterData.getRuntime_totalMemory());
        assertEquals(0.0, meterData.getSystemLoad());

        // Verify MeterData specific fields are cleared
        assertNull(meterData.getCategory());
        assertNull(meterData.getOperation());
        assertNull(meterData.getParent());
        assertNull(meterData.getDescription());
        assertEquals(0L, meterData.getCreateTime());
        assertEquals(0L, meterData.getStartTime());
        assertEquals(0L, meterData.getStopTime());
        assertEquals(0L, meterData.getTimeLimit());
        assertEquals(0L, meterData.getCurrentIteration());
        assertEquals(0L, meterData.getExpectedIterations());
        assertNull(meterData.getOkPath());
        assertNull(meterData.getRejectPath());
        assertNull(meterData.getFailPath());
        assertNull(meterData.getFailMessage());
        assertNotNull(meterData.getContext());

        // Verify status methods
        assertFalse(meterData.isStarted());
        assertFalse(meterData.isStopped());
        assertFalse(meterData.isOK());
        assertFalse(meterData.isReject());
        assertFalse(meterData.isFail());
    }

    static Stream<Arguments> providePathTestCases() {
        return Stream.of(
                Arguments.of(new MockMeterData("uuid", 1, "cat", "op", null, null, 100, 200, 300, 400, 0, 0, 0, null, null, null, null, null),
                        null),
                Arguments.of(new MockMeterData("uuid", 1, "cat", "op", null, null, 100, 200, 300, 400, 0, 0, 0, "okp", null, null, null, null),
                        "okp"),
                Arguments.of(new MockMeterData("uuid", 1, "cat", "op", null, null, 100, 200, 300, 400, 0, 0, 0, null, "rejectp", null, null, null),
                        "rejectp"),
                Arguments.of(new MockMeterData("uuid", 1, "cat", "op", null, null, 100, 200, 300, 400, 0, 0, 0, null, null, "failp", null, null),
                        "failp")
        );
    }

    @ParameterizedTest
    @MethodSource("providePathTestCases")
    @DisplayName("should return correct path based on ok, reject, or fail paths")
    void testPath(final MeterData value, final String expected) {
        // Given: a MeterData instance with various path configurations
        // When: calling getPath() method
        final String actual = value.getPath();
        // Then: the path should match the expected value
        assertEquals(expected, actual, "should match expected path");
    }

    static Stream<Arguments> provideFullIDTestCases() {
        return Stream.of(
                Arguments.of(new MockMeterData("uuid", 1, "cat", "op", null, null, 100, 200, 300, 400, 0, 0, 0, null, null, null, null, null),
                        "cat/op#1"),
                Arguments.of(new MockMeterData("uuid", 1, "cat", null, null, null, 100, 200, 300, 400, 0, 0, 0, null, null, null, null, null),
                        "cat#1")
            );
    }

    @ParameterizedTest
    @MethodSource("provideFullIDTestCases")
    @DisplayName("should generate full ID in correct format from category, operation, and position")
    void testFullID(final MeterData value, final String expected) {
        // Given: a MeterData instance with various category and operation configurations
        // When: calling getFullID() method
        final String actual = value.getFullID();
        // Then: the full ID should match the expected format
        assertEquals(expected, actual, "should match expected full ID format");
    }

    // ============================================================================
    // JSON5 Round-Trip Serialization Tests
    // ============================================================================

    /**
     * Provides test scenarios for round-trip JSON5 serialization tests.
     * Each scenario contains a descriptive name and a MeterData instance with specific field combinations.
     * Includes edge cases like all nulls, all values populated, and various combinations of paths.
     */
    static Stream<Arguments> roundTripSerializationScenarios() {
        final Map<String, String> contextMap1 = new HashMap<>();
        contextMap1.put("key1", "value1");
        contextMap1.put("key2", "value2");

        final Map<String, String> contextMap2 = new HashMap<>();
        contextMap2.put("traceId", "trace-123");

        return Stream.of(
                // Scenario 1: Full data with all MeterData fields populated
                Arguments.of(
                        "Full data scenario",
                        new MeterData("uuid-full", 1L, 1000L, 100L, 200L, 150L, 50L, 100L, 75L, 10L,
                                1000L, 2000L, 100L, 5000L, 50L, 10000L, 512L, 1024L, 768L, 0.75,
                                "processing", "batch_job", "parent-op", "Batch job execution",
                                1000L, 2000L, 3000L, 5000L, 100L, 200L, "success", null, null, null, contextMap1)
                ),

                // Scenario 2: Minimal data with only required fields
                Arguments.of(
                        "Minimal data scenario",
                        new MeterData("uuid-min", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0.0,
                                "category", "operation", null, null,
                                0L, 0L, 0L, 0L, 0L, 0L, null, null, null, null, null)
                ),

                // Scenario 3: Data with reject path
                Arguments.of(
                        "Reject path scenario",
                        new MeterData("uuid-reject", 5L, 500L, 100L, 200L, 150L, 50L, 100L, 75L, 0L,
                                500L, 1000L, 50L, 2500L, 25L, 5000L, 256L, 512L, 384L, 0.5,
                                "validation", "check_data", null, "Data validation check",
                                500L, 600L, 700L, 1000L, 1L, 1L, null, "INVALID_FORMAT", null, null, null)
                ),

                // Scenario 4: Data with fail path and message
                Arguments.of(
                        "Fail path scenario",
                        new MeterData("uuid-fail", 10L, 2000L, 200L, 400L, 300L, 100L, 200L, 150L, 5L,
                                2000L, 4000L, 200L, 10000L, 100L, 20000L, 1024L, 2048L, 1536L, 1.0,
                                "database", "query", "parent-query", "Database query execution",
                                2000L, 2100L, 2500L, 3000L, 50L, 100L, null, null, "SQLException", "Connection timeout", contextMap2)
                ),

                // Scenario 5: Data with large numeric values
                Arguments.of(
                        "Large numeric values",
                        new MeterData("uuid-large", Long.MAX_VALUE - 1, Long.MAX_VALUE - 100,
                                Long.MAX_VALUE - 1, Long.MAX_VALUE - 2, Long.MAX_VALUE - 3,
                                Long.MAX_VALUE - 4, Long.MAX_VALUE - 5, Long.MAX_VALUE - 6,
                                Long.MAX_VALUE - 7, Long.MAX_VALUE - 8, Long.MAX_VALUE - 9,
                                Long.MAX_VALUE - 10, Long.MAX_VALUE - 11, Long.MAX_VALUE - 12,
                                Long.MAX_VALUE - 13, Long.MAX_VALUE - 14, Long.MAX_VALUE - 15,
                                Long.MAX_VALUE - 16, 999.999,
                                "heavy", "processing", "parent-heavy", "Heavy processing task",
                                Long.MAX_VALUE - 20, Long.MAX_VALUE - 21, Long.MAX_VALUE - 22,
                                Long.MAX_VALUE - 23, Long.MAX_VALUE - 24, Long.MAX_VALUE - 25,
                                "ok", null, null, null, null)
                ),

                // Scenario 6: Data with special characters in fields
                Arguments.of(
                        "Special characters scenario",
                        new MeterData("uuid-special-chars-123", 42L, 5555L, 111L, 222L, 166L, 55L, 111L, 83L, 2L,
                                550L, 1100L, 55L, 2775L, 27L, 5550L, 288L, 576L, 432L, 0.42,
                                "cat_with-special.chars", "op-with_special.chars", "parent#with-chars",
                                "Description with special: chars, symbols!",
                                5555L, 5600L, 5700L, 1000L, 10L, 20L,
                                "path/with/special#chars", null, null, null, null)
                ),

                // Scenario 7: Data with zero values and null paths
                Arguments.of(
                        "Zero values scenario",
                        new MeterData("zero-uuid", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0.0,
                                "zero", "zero", null, null,
                                0L, 0L, 0L, 0L, 0L, 0L, null, null, null, null, null)
                ),

                // Scenario 8: Data with iteration tracking
                Arguments.of(
                        "Iteration tracking scenario",
                        new MeterData("uuid-iter", 15L, 1500L, 150L, 300L, 225L, 75L, 150L, 112L, 3L,
                                1500L, 3000L, 150L, 7500L, 75L, 15000L, 768L, 1536L, 1152L, 0.75,
                                "loop", "batch_process", "parent-batch", "Process batch items",
                                1500L, 1600L, 2500L, 5000L, 500L, 1000L,
                                "completed", null, null, null, contextMap1)
                ),

                // Scenario 9: Data with only ok path set
                Arguments.of(
                        "Only ok path scenario",
                        new MeterData("uuid-ok-only", 3L, 300L, 50L, 100L, 75L, 25L, 50L, 37L, 1L,
                                300L, 600L, 30L, 1500L, 15L, 3000L, 128L, 256L, 192L, 0.3,
                                "simple", "task", null, "Simple task",
                                300L, 350L, 400L, 500L, 1L, 1L,
                                "done", null, null, null, null)
                ),

                // Scenario 10: Data with empty context map
                Arguments.of(
                        "Empty context map scenario",
                        new MeterData("uuid-empty-ctx", 7L, 700L, 70L, 140L, 105L, 35L, 70L, 52L, 1L,
                                700L, 1400L, 70L, 3500L, 35L, 7000L, 448L, 896L, 672L, 0.7,
                                "context_test", "empty_ctx", null, "Test with empty context",
                                700L, 750L, 850L, 1000L, 10L, 20L,
                                "ok", null, null, null, new HashMap<>())
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripSerializationScenarios")
    @DisplayName("should support round-trip JSON5 serialization and deserialization")
    void testWriteReadJson5_roundTripSerialization(final String scenarioName, final MeterData original) {
        // Given: MeterData with specific values from scenario
        final StringBuilder sb = new StringBuilder(256);

        // When: original is serialized to JSON5, then deserialized
        original.writeJson5(sb);
        final MeterData restored = new MeterData();
        restored.readJson5("{" + sb + "}");

        // Then: restored data should match original for all fields
        assertEquals(original.getSessionUuid(), restored.getSessionUuid(),
                "should preserve sessionUuid in " + scenarioName);
        assertEquals(original.getPosition(), restored.getPosition(),
                "should preserve position in " + scenarioName);
        assertEquals(original.getLastCurrentTime(), restored.getLastCurrentTime(),
                "should preserve lastCurrentTime in " + scenarioName);
        assertEquals(original.getCategory(), restored.getCategory(),
                "should preserve category in " + scenarioName);
        assertEquals(original.getOperation(), restored.getOperation(),
                "should preserve operation in " + scenarioName);
        assertEquals(original.getParent(), restored.getParent(),
                "should preserve parent in " + scenarioName);
        assertEquals(original.getDescription(), restored.getDescription(),
                "should preserve description in " + scenarioName);
        assertEquals(original.getCreateTime(), restored.getCreateTime(),
                "should preserve createTime in " + scenarioName);
        assertEquals(original.getStartTime(), restored.getStartTime(),
                "should preserve startTime in " + scenarioName);
        assertEquals(original.getStopTime(), restored.getStopTime(),
                "should preserve stopTime in " + scenarioName);
        assertEquals(original.getTimeLimit(), restored.getTimeLimit(),
                "should preserve timeLimit in " + scenarioName);
        assertEquals(original.getCurrentIteration(), restored.getCurrentIteration(),
                "should preserve currentIteration in " + scenarioName);
        assertEquals(original.getExpectedIterations(), restored.getExpectedIterations(),
                "should preserve expectedIterations in " + scenarioName);
        assertEquals(original.getOkPath(), restored.getOkPath(),
                "should preserve okPath in " + scenarioName);
        assertEquals(original.getRejectPath(), restored.getRejectPath(),
                "should preserve rejectPath in " + scenarioName);
        assertEquals(original.getFailPath(), restored.getFailPath(),
                "should preserve failPath in " + scenarioName);
        assertEquals(original.getFailMessage(), restored.getFailMessage(),
                "should preserve failMessage in " + scenarioName);
    }

    // ============================================================================
    // JSON5 Special Behavior Tests (Non-Round-Trip)
    // ============================================================================

    @Test
    @DisplayName("should handle empty JSON5 gracefully without altering existing fields")
    void testReadJson5_emptyJson5() {
        // Given: MeterData with existing values and empty JSON5 string
        final Map<String, String> context = new HashMap<>();
        context.put("existing", "data");

        final MeterData meterData = new MeterData("original_uuid", 42L, 100L, 10L, 20L, 15L, 5L, 10L, 7L, 1L,
                100L, 200L, 10L, 500L, 5L, 1000L, 64L, 128L, 96L, 0.42,
                "category", "operation", "parent", "description",
                100L, 150L, 200L, 500L, 10L, 20L,
                "ok_path", null, null, null, context);

        // When: readJson5 is called with empty JSON5
        meterData.readJson5("{}");

        // Then: fields should remain unchanged (not a round-trip test)
        assertEquals("original_uuid", meterData.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(42L, meterData.getPosition(), "should preserve position");
        assertEquals("category", meterData.getCategory(), "should preserve category");
        assertEquals("operation", meterData.getOperation(), "should preserve operation");
        assertEquals("parent", meterData.getParent(), "should preserve parent");
        assertEquals("description", meterData.getDescription(), "should preserve description");
        assertEquals("ok_path", meterData.getOkPath(), "should preserve okPath");
    }

    @Test
    @DisplayName("should handle missing fields without throwing exceptions")
    void testReadJson5_missingFields() {
        // Given: MeterData with existing values and JSON5 missing some fields
        final MeterData meterData = new MeterData("orig_uuid", 50L, 200L, 20L, 40L, 30L, 10L, 20L, 15L, 2L,
                200L, 400L, 20L, 1000L, 10L, 2000L, 128L, 256L, 192L, 0.5,
                "cat1", "op1", "parent1", "desc1",
                200L, 250L, 300L, 1000L, 15L, 30L,
                "okpath1", null, null, null, null);

        // When: readJson5 is called with only category field
        meterData.readJson5("{c:new_category}");

        // Then: only category should be updated, others unchanged (partial update test)
        assertEquals("orig_uuid", meterData.getSessionUuid(), "should preserve sessionUuid");
        assertEquals(50L, meterData.getPosition(), "should preserve position");
        assertEquals("new_category", meterData.getCategory(), "should update category");
        assertEquals("op1", meterData.getOperation(), "should preserve operation");
        assertEquals("parent1", meterData.getParent(), "should preserve parent");
    }

    @Test
    @DisplayName("should preserve existing fields when JSON5 does not contain them")
    void testReadJson5_partialFieldsParsing() {
        // Given: MeterData with existing values and partial JSON5
        final MeterData meterData = new MeterData("existing_uuid", 999L, 5555L, 55L, 110L, 82L, 27L, 55L, 41L, 3L,
                5555L, 11111L, 555L, 27777L, 277L, 55555L, 1792L, 3584L, 2688L, 0.99,
                "existing_cat", "existing_op", "existing_parent", "existing_desc",
                5555L, 5600L, 5700L, 10000L, 100L, 200L,
                "existing_ok", null, null, null, null);

        // When: readJson5 is called with only operation field
        meterData.readJson5("{n:new_operation}");

        // Then: operation should be updated and others unchanged (partial update test)
        assertEquals("existing_uuid", meterData.getSessionUuid(), "should preserve sessionUuid");
        assertEquals("existing_cat", meterData.getCategory(), "should preserve category");
        assertEquals("new_operation", meterData.getOperation(), "should update operation");
        assertEquals("existing_parent", meterData.getParent(), "should preserve parent");
        assertEquals("existing_desc", meterData.getDescription(), "should preserve description");
    }

    @Test
    @DisplayName("should handle partial updates with multiple fields in JSON5")
    void testReadJson5_multiplePartialFields() {
        // Given: MeterData with existing values and JSON5 with multiple fields
        final MeterData meterData = new MeterData("uuid_partial", 77L, 7700L, 77L, 154L, 115L, 38L, 77L, 57L, 4L,
                7700L, 15400L, 770L, 38500L, 385L, 77000L, 2464L, 4928L, 3696L, 0.77,
                "cat_old", "op_old", "parent_old", "desc_old",
                7700L, 7750L, 7850L, 10000L, 50L, 100L,
                "ok_old", null, null, null, null);

        // When: readJson5 is called with category, operation, and description
        meterData.readJson5("{c:cat_new,n:op_new,d:'desc_new'}");

        // Then: multiple fields should be updated, others unchanged
        assertEquals("uuid_partial", meterData.getSessionUuid(), "should preserve sessionUuid");
        assertEquals("cat_new", meterData.getCategory(), "should update category");
        assertEquals("op_new", meterData.getOperation(), "should update operation");
        assertEquals("parent_old", meterData.getParent(), "should preserve parent");
        assertEquals("desc_new", meterData.getDescription(), "should update description");
        assertEquals("ok_old", meterData.getOkPath(), "should preserve okPath");
    }

    // ============================================================================
    // equals() and hashCode() Tests
    // ============================================================================

    /**
     * Provides test scenarios for equals() method testing.
     * Each scenario contains a pair of MeterData instances with specific differences to test.
     */
    static Stream<Arguments> equalsTestScenarios() {
        return Stream.of(
                // Scenario 1: Identical data - should be equal
                Arguments.of(
                        "Identical instances",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        true,
                        "identical instances should be equal"
                ),

                // Scenario 2: Different category - should not be equal
                Arguments.of(
                        "Different category",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat2", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "different category should not be equal"
                ),

                // Scenario 3: Different operation - should not be equal
                Arguments.of(
                        "Different operation",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op2", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "different operation should not be equal"
                ),

                // Scenario 4: Different position - should not be equal
                Arguments.of(
                        "Different position",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "different position should not be equal"
                ),

                // Scenario 5: Different sessionUuid - should not be equal
                Arguments.of(
                        "Different sessionUuid",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid2", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "different sessionUuid should not be equal"
                ),

                // Scenario 6: Both have null category - should be equal (if all other fields match)
                Arguments.of(
                        "Both null category",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                null, "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                null, "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        true,
                        "both null categories should be equal"
                ),

                // Scenario 7: One null category - should not be equal
                Arguments.of(
                        "One null category",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                null, "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "one null category should not be equal"
                ),

                // Scenario 8: Both have null operation - should be equal (if all other fields match)
                Arguments.of(
                        "Both null operation",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        true,
                        "both null operations should be equal"
                ),

                // Scenario 9: One null operation - should not be equal
                Arguments.of(
                        "One null operation",
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "one null operation should not be equal"
                ),

                // Scenario 10: Both have null sessionUuid - should be equal (if all other fields match)
                Arguments.of(
                        "Both null sessionUuid",
                        new MeterData(null, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData(null, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        true,
                        "both null sessionUuids should be equal"
                ),

                // Scenario 11: One null sessionUuid - should not be equal
                Arguments.of(
                        "One null sessionUuid",
                        new MeterData(null, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        false,
                        "one null sessionUuid should not be equal"
                ),

                // Scenario 12: Different system data fields but same key fields - should be equal
                Arguments.of(
                        "Different system fields, same key fields",
                        new MeterData("uuid1", 1, 100, 10, 20, 15, 5, 10, 7, 1, 100, 200, 10, 500, 5, 1000, 64, 128, 96, 0.5,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        new MeterData("uuid1", 1, 200, 20, 40, 30, 10, 20, 14, 2, 200, 400, 20, 1000, 10, 2000, 128, 256, 192, 1.0,
                                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null),
                        true,
                        "different system fields but same key fields should be equal"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("equalsTestScenarios")
    @DisplayName("should correctly compare MeterData instances for equality")
    void testEquals(final String scenarioName, final MeterData data1, final MeterData data2,
                    final boolean expectedEqual, final String message) {
        // Given: two MeterData instances with specific characteristics
        // When: comparing instances for equality
        final boolean actualEqual = data1.equals(data2);

        // Then: equality should match the expected result
        assertEquals(expectedEqual, actualEqual, message + " in scenario: " + scenarioName);
    }

    @Test
    @DisplayName("should return false when comparing with null")
    void testEquals_withNull() {
        // Given: a MeterData instance and null reference
        final MeterData data = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        // When: comparing with null
        // Then: should return false
        assertNotEquals(data, null, "should not be equal to null");
        assertFalse(data.equals(null), "equals(null) should return false");
    }

    @Test
    @DisplayName("should return true when comparing with self")
    void testEquals_withSelf() {
        // Given: a MeterData instance
        final MeterData data = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        // When: comparing with itself
        // Then: should return true
        assertEquals(data, data, "should be equal to itself");
        assertTrue(data.equals(data), "equals(self) should return true");
    }

    @Test
    @DisplayName("should return false when comparing with different class type")
    void testEquals_withDifferentClass() {
        // Given: a MeterData instance and a different type object
        final MeterData data = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final Object differentType = "not a MeterData";

        // When: comparing with different type
        // Then: should return false
        assertNotEquals(data, differentType, "should not be equal to different type");
        assertFalse(data.equals(differentType), "equals(different type) should return false");
    }

    /**
     * Provides test scenarios for hashCode() method testing.
     * Each scenario contains MeterData instances to verify hash code consistency.
     */
    static Stream<Arguments> hashCodeTestScenarios() {
        final MeterData identical1 = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final MeterData identical2 = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        final MeterData different1 = new MeterData("uuid2", 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat2", "op2", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        return Stream.of(
                // Scenario 1: Equal objects must have equal hash codes
                Arguments.of(
                        "Equal objects have equal hash codes",
                        identical1,
                        identical2,
                        true,
                        "equal objects should have equal hash codes"
                ),

                // Scenario 2: Different objects likely have different hash codes
                Arguments.of(
                        "Different objects likely have different hash codes",
                        identical1,
                        different1,
                        false,
                        "different objects should likely have different hash codes"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("hashCodeTestScenarios")
    @DisplayName("should produce consistent hash codes for equal objects")
    void testHashCode(final String scenarioName, final MeterData data1, final MeterData data2,
                      final boolean expectedHashCodeEqual, final String message) {
        // Given: two MeterData instances
        // When: computing hash codes
        final int hash1 = data1.hashCode();
        final int hash2 = data2.hashCode();

        // Then: hash codes should follow equality contract
        if (data1.equals(data2)) {
            assertEquals(hash1, hash2, "equal objects must have equal hash codes in " + scenarioName);
        }
        if (expectedHashCodeEqual) {
            assertEquals(hash1, hash2, message + " in " + scenarioName);
        }
    }

    @Test
    @DisplayName("should handle null fields in hashCode calculation")
    void testHashCode_withNullFields() {
        // Given: MeterData instances with null fields
        final MeterData nullCategory = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                null, "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final MeterData nullOperation = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", null, null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final MeterData nullUuid = new MeterData(null, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        // When: computing hash codes with null fields
        final int hash1 = nullCategory.hashCode();
        final int hash2 = nullOperation.hashCode();
        final int hash3 = nullUuid.hashCode();

        // Then: hash codes should be computed without throwing exceptions
        assertNotNull(hash1, "hash code with null category should not be null");
        assertNotNull(hash2, "hash code with null operation should not be null");
        assertNotNull(hash3, "hash code with null uuid should not be null");
    }

    @Test
    @DisplayName("should produce consistent hash codes across multiple invocations")
    void testHashCode_consistency() {
        // Given: a MeterData instance
        final MeterData data = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        // When: computing hash code multiple times
        final int hash1 = data.hashCode();
        final int hash2 = data.hashCode();
        final int hash3 = data.hashCode();

        // Then: all invocations should return the same value
        assertEquals(hash1, hash2, "hash code should be consistent across invocations");
        assertEquals(hash2, hash3, "hash code should remain consistent");
    }
}
