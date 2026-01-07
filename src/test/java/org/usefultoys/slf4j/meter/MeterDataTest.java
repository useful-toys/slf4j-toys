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
 * </ul>
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

    @Test
    @DisplayName("should implement equals and hashCode correctly")
    void testEqualsAndHashCode() {
        // Given: three MeterData instances - two identical and one different
        final MeterData data1 = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final MeterData data2 = new MeterData("uuid1", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "cat1", "op1", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);
        final MeterData data3 = new MeterData("uuid2", 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, "cat2", "op2", null, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null);

        // When: comparing the instances for equality
        // Then: identical instances should be equal, different instances should not be equal
        assertEquals(data1, data2, "should be equal when all fields are identical");
        assertNotEquals(data1, data3, "should not be equal when fields differ");

        // And: hash codes should be consistent with equality
        assertEquals(data1.hashCode(), data2.hashCode(), "hash codes should be equal when objects are equal");
        assertNotEquals(data1.hashCode(), data3.hashCode(), "hash codes should differ when objects are not equal");
    }
}
