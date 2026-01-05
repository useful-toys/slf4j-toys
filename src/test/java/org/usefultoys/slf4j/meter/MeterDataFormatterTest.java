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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MeterDataFormatter}.
 * <p>
 * Tests validate that MeterDataFormatter correctly formats MeterData into human-readable strings,
 * handling various states, timing information, context data, and system metrics with proper locale-specific formatting.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Status Formatting:</b> Verifies formatting of all meter states (SCHEDULED, STARTED, PROGRESS, OK, REJECT, FAIL, Slow)</li>
 *   <li><b>Identification Formatting:</b> Tests category, operation, and position combinations</li>
 *   <li><b>Path Formatting:</b> Tests ok, reject, and fail paths with optional failure messages</li>
 *   <li><b>Iteration Formatting:</b> Tests current and expected iteration counts</li>
 *   <li><b>Timing Formatting:</b> Tests waiting time, execution time, and iterations per second</li>
 *   <li><b>Metadata Formatting:</b> Tests description and context key-value pairs</li>
 *   <li><b>System Info Formatting:</b> Tests memory usage, system load, and UUID</li>
 *   <li><b>Configuration Sensitivity:</b> Tests formatting with various MeterConfig settings</li>
 *   <li><b>Locale Handling:</b> Ensures consistent formatting across different data scenarios</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("MeterDataFormatter")
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
class MeterDataFormatterTest {

    private static final String TEST_CATEGORY = "org.example.TestCategory";
    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_PARENT = "testParent";
    private static final String TEST_DESCRIPTION = "test description";
    private static final String TEST_UUID = UUID.randomUUID().toString();

    private static Stream<Arguments> provideMeterDataForReadableStringBuilder() {
        // Fixed timestamp for deterministic testing
        final long now = 1000000000000L; // Fixed timestamp in nanoseconds
        
        return Stream.of(
                // === SCHEDULED state (not started) ===
                Arguments.of(
                        "SCHEDULED with minimal data",
                        createMeterData(now, null, now, 0, 0, 0, 0, null, null, null, null, null, null, null, 0.0),
                        "SCHEDULED: 0ns",
                        "SCHEDULED: TestCategory 0ns"
                ),
                Arguments.of(
                        "SCHEDULED with operation",
                        createMeterData(now, null, now, 0, 0, 0, 0, TEST_OPERATION, null, null, null, null, null, null, 0.0),
                        "SCHEDULED: testOperation 0ns",
                        "SCHEDULED: TestCategory/testOperation 0ns"
                ),
                Arguments.of(
                        "SCHEDULED with waiting time",
                        createMeterData(now, null, now - 100_000_000L, 0, 0, 0, 0, null, null, null, null, null, null, null, 0.0),
                        "SCHEDULED: 100.0ms",
                        "SCHEDULED: TestCategory 100.0ms"
                ),
                Arguments.of(
                        "SCHEDULED with description",
                        createMeterData(now, null, now, 0, 0, 0, 0, null, null, null, null, TEST_DESCRIPTION, null, null, 0.0),
                        "SCHEDULED: 0ns; 'test description'",
                        "SCHEDULED: TestCategory 0ns; 'test description'"
                ),

                // === STARTED state (no iterations yet) ===
                Arguments.of(
                        "STARTED with minimal data",
                        createMeterData(now, null, now, now, 0, 0, 0, null, null, null, null, null, null, null, 0.0),
                        "STARTED: ",
                        "STARTED: TestCategory "
                ),
                Arguments.of(
                        "STARTED with operation",
                        createMeterData(now, null, now, now, 0, 0, 0, TEST_OPERATION, null, null, null, null, null, null, 0.0),
                        "STARTED: testOperation ",
                        "STARTED: TestCategory/testOperation "
                ),
                Arguments.of(
                        "STARTED with description and context",
                        createMeterData(now, null, now, now, 0, 0, 0, null, null, null, null, TEST_DESCRIPTION, createContext("key1", "value1"), null, 0.0),
                        "STARTED: 'test description'; key1=value1",
                        "STARTED: TestCategory 'test description'; key1=value1"
                ),

                // === PROGRESS state (with iterations, not slow) ===
                Arguments.of(
                        "PROGRESS with iterations below threshold",
                        createMeterData(now, null, now, now - 500_000_000L, 0, 5, 10, null, null, null, null, null, null, null, 0.0),
                        "PROGRESS: 5/10; 500.0ms; 10.0/s 100.0ms",
                        "PROGRESS: TestCategory 5/10; 500.0ms; 10.0/s 100.0ms"
                ),
                Arguments.of(
                        "PROGRESS with iterations above threshold",
                        createMeterData(now, null, now, now - 5_000_000_000L, 0L, 5L, 10L, null, null, null, null, null, null, null, 0.0),
                        "PROGRESS: 5/10; 5.0s; 1.0/s 1000.0ms",
                        "PROGRESS: TestCategory 5/10; 5.0s; 1.0/s 1000.0ms"
                ),

                // === PROGRESS (Slow) state ===
                Arguments.of(
                        "PROGRESS (Slow) with iterations",
                        createMeterData(now, null, now, now - 15_000_000_000L, 0, 5, 10, null, null, null, null, null, null, null, 0.0),
                        "PROGRESS (Slow): 5/10; 15.0s; 0.3/s 3.0s",
                        "PROGRESS (Slow): TestCategory 5/10; 15.0s; 0.3/s 3.0s"
                ),

                // === OK state (stopped successfully) ===
                Arguments.of(
                        "OK with minimal timing",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, null, null, null, 0.0),
                        "OK: [okPath] 100.0ms",
                        "OK: TestCategory[okPath] 100.0ms"
                ),
                Arguments.of(
                        "OK with operation",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, TEST_OPERATION, "okPath", null, null, null, null, null, 0.0),
                        "OK: testOperation[okPath] 100.0ms",
                        "OK: TestCategory/testOperation[okPath] 100.0ms"
                ),
                Arguments.of(
                        "OK with iterations",
                        createMeterData(now, null, now, now - 2_000_000_000L, now, 100, 100, null, "okPath", null, null, null, null, null, 0.0),
                        "OK: [okPath] 100/100; 2.0s; 50.0/s 20.0ms",
                        "OK: TestCategory[okPath] 100/100; 2.0s; 50.0/s 20.0ms"
                ),
                Arguments.of(
                        "OK with operation and parent",
                        createMeterData(now, TEST_PARENT, now, now - 1_000_000_000L, now, 0, 0, TEST_OPERATION, "okPath", null, null, null, null, null, 0.0),
                        "OK: testParent/testOperation[okPath] 1000.0ms",
                        "OK: TestCategory/testParent/testOperation[okPath] 1000.0ms"
                ),

                // === OK (Slow) state ===
                Arguments.of(
                        "OK (Slow) with timing",
                        createMeterData(now, null, now, now - 15_000_000_000L, now, 0, 0, null, "okPath", null, null, null, null, null, 0.0),
                        "OK (Slow): [okPath] 15.0s",
                        "OK (Slow): TestCategory[okPath] 15.0s"
                ),
                Arguments.of(
                        "OK (Slow) with iterations",
                        createMeterData(now, null, now, now - 12_000_000_000L, now, 10, 10, null, "okPath", null, null, null, null, null, 0.0),
                        "OK (Slow): [okPath] 10/10; 12.0s; 0.8/s 1.2s",
                        "OK (Slow): TestCategory[okPath] 10/10; 12.0s; 0.8/s 1.2s"
                ),

                // === REJECT state ===
                Arguments.of(
                        "REJECT with minimal data",
                        createMeterData(now, null, now, now - 500_000_000L, now, 0, 0, null, null, "rejectPath", null, null, null, null, 0.0),
                        "REJECT: [rejectPath] 500.0ms",
                        "REJECT: TestCategory[rejectPath] 500.0ms"
                ),
                Arguments.of(
                        "REJECT with operation",
                        createMeterData(now, null, now, now - 500_000_000L, now, 0, 0, TEST_OPERATION, null, "rejectPath", null, null, null, null, 0.0),
                        "REJECT: testOperation[rejectPath] 500.0ms",
                        "REJECT: TestCategory/testOperation[rejectPath] 500.0ms"
                ),
                Arguments.of(
                        "REJECT with description",
                        createMeterData(now, null, now, now - 200_000_000L, now, 0, 0, null, null, "rejectPath", null, TEST_DESCRIPTION, null, null, 0.0),
                        "REJECT: [rejectPath] 200.0ms; 'test description'",
                        "REJECT: TestCategory[rejectPath] 200.0ms; 'test description'"
                ),

                // === FAIL state ===
                Arguments.of(
                        "FAIL with minimal data",
                        createMeterData(now, null, now, now - 300_000_000L, now, 0, 0, null, null, null, "failPath", null, null, null, 0.0),
                        "FAIL: [failPath] 300.0ms",
                        "FAIL: TestCategory[failPath] 300.0ms"
                ),
                Arguments.of(
                        "FAIL with message",
                        new MeterData(null, 1, now, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 
                                TEST_CATEGORY, null, null, null, now, now - 400_000_000L, now, 10_000_000_000L, 0, 0, null, null, "failPath", "Error occurred", null),
                        "FAIL: [failPath; Error occurred] 400.0ms",
                        "FAIL: TestCategory[failPath; Error occurred] 400.0ms"
                ),
                Arguments.of(
                        "FAIL with iterations and message",
                        new MeterData(null, 1, now, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                                TEST_CATEGORY, null, null, null, now, now - 2_000_000_000L, now, 10_000_000_000L, 5, 10, null, null, "failPath", "Interrupted", null),
                        "FAIL: [failPath; Interrupted] 5/10; 2.0s; 2.5/s 400.0ms",
                        "FAIL: TestCategory[failPath; Interrupted] 5/10; 2.0s; 2.5/s 400.0ms"
                ),

                // === Context and metadata combinations ===
                Arguments.of(
                        "OK with single context entry",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, null, createContext("key1", "value1"), null, 0.0),
                        "OK: [okPath] 100.0ms; key1=value1",
                        "OK: TestCategory[okPath] 100.0ms; key1=value1"
                ),
                Arguments.of(
                        "OK with multiple context entries",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, null, createContext("key1", "value1", "key2", "value2"), null, 0.0),
                        "OK: [okPath] 100.0ms; key1=value1; key2=value2",
                        "OK: TestCategory[okPath] 100.0ms; key1=value1; key2=value2"
                ),
                Arguments.of(
                        "OK with context entry without value",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, null, createContext("flag", null), null, 0.0),
                        "OK: [okPath] 100.0ms; flag",
                        "OK: TestCategory[okPath] 100.0ms; flag"
                ),
                Arguments.of(
                        "OK with description and context",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, TEST_DESCRIPTION, createContext("key1", "value1"), null, 0.0),
                        "OK: [okPath] 100.0ms; 'test description'; key1=value1",
                        "OK: TestCategory[okPath] 100.0ms; 'test description'; key1=value1"
                ),

                // === System info combinations (with MeterConfig) ===
                Arguments.of(
                        "OK with UUID",
                        createMeterData(now, null, now, now - 100_000_000L, now, 0, 0, null, "okPath", null, null, null, null, TEST_UUID, 0.0),
                        "OK: [okPath] 100.0ms; " + TEST_UUID,
                        "OK: TestCategory[okPath] 100.0ms; " + TEST_UUID
                ),

                // === Edge cases ===
                Arguments.of(
                        "OK with very small timing (microseconds)",
                        createMeterData(now, null, now, now - 1100L, now, 0, 0, null, "okPath", null, null, null, null, null, 0.0),
                        "OK: [okPath] 1.1us",
                        "OK: TestCategory[okPath] 1.1us"
                ),
                Arguments.of(
                        "OK with large iteration count",
                        createMeterData(now, null, now, now - 1_000_000_000L, now, 1_000_000, 1_000_000, null, "okPath", null, null, null, null, null, 0.0),
                        "OK: [okPath] 1000.0k/1000.0k; 1000.0ms; 1000.0k/s 1000.0ns",
                        "OK: TestCategory[okPath] 1000.0k/1000.0k; 1000.0ms; 1000.0k/s 1000.0ns"
                ),
                Arguments.of(
                        "STARTED with expected iterations but no current",
                        createMeterData(now, null, now, now - 5_000_000_000L, 0, 0, 100, null, null, null, null, null, null, null, 0.0),
                        "STARTED: 5.0s",
                        "STARTED: TestCategory 5.0s"
                ),
                Arguments.of(
                        "OK with only current iterations (no expected)",
                        createMeterData(now, null, now, now - 1_000_000_000L, now, 50, 0, null, "okPath", null, null, null, null, null, 0.0),
                        "OK: [okPath] 50; 1000.0ms; 50.0/s 20.0ms",
                        "OK: TestCategory[okPath] 50; 1000.0ms; 50.0/s 20.0ms"
                ),

                // === Empty/minimal state ===
                Arguments.of(
                        "Empty data with all zeros",
                        new MeterData(null, 1, now, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, TEST_CATEGORY, null, null, null, now, 0, 0, 0, 0, 0, null, null, null, null, null),
                        "SCHEDULED: 0ns",
                        "SCHEDULED: TestCategory 0ns"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideMeterDataForReadableStringBuilder")
    @DisplayName("should format meter data to readable string using default config")
    void testReadableStringBuilderWithDefaultConfig(final String testName, final MeterData data, final String expectedDefault, final String expectedWithCategory) {
        // Given: MeterData with various combinations of status, timing, context, and system info
        final StringBuilder sb = new StringBuilder(256);

        // When: readableStringBuilder is called
        MeterDataFormatter.readableStringBuilder(data, sb);

        // Then: should produce the expected formatted string with proper locale-specific formatting
        assertEquals(expectedDefault, sb.toString(), "should format meter data correctly for: " + testName);
    }

    @ParameterizedTest
    @MethodSource("provideMeterDataForReadableStringBuilder")
    @DisplayName("should format meter data to readable string using printCategory=true")
    void testReadableStringBuilderWithPrintCategory(final String testName, final MeterData data, final String expectedDefault, final String expectedWithCategory) {
        // Given: MeterData with various combinations of status, timing, context, and system info
        MeterConfig.printCategory = true;
        final StringBuilder sb = new StringBuilder(256);

        // When: readableStringBuilder is called
        MeterDataFormatter.readableStringBuilder(data, sb);

        // Then: should produce the expected formatted string with proper locale-specific formatting
        assertEquals(expectedWithCategory, sb.toString(), "should format meter data correctly for: " + testName);
    }

    /**
     * Creates a MeterData instance with specified parameters for testing.
     * Uses default values for system metrics and TEST_CATEGORY.
     */
    private static MeterData createMeterData(
            final long currentTime,
            final String parent,
            final long createTime,
            final long startTime,
            final long stopTime,
            final long currentIteration,
            final long expectedIterations,
            final String operation,
            final String okPath,
            final String rejectPath,
            final String failPath,
            final String description,
            final Map<String, String> context,
            final String sessionUuid,
            final double systemLoad) {
        return createMeterData(currentTime, parent, createTime, startTime, stopTime, currentIteration, expectedIterations,
                operation, okPath, rejectPath, failPath, description, context, sessionUuid, systemLoad, 0, 0);
    }

    /**
     * Creates a MeterData instance with specified parameters including memory info.
     */
    private static MeterData createMeterData(
            final long currentTime,
            final String parent,
            final long createTime,
            final long startTime,
            final long stopTime,
            final long currentIteration,
            final long expectedIterations,
            final String operation,
            final String okPath,
            final String rejectPath,
            final String failPath,
            final String description,
            final Map<String, String> context,
            final String sessionUuid,
            final double systemLoad,
            final long usedMemory,
            final long maxMemory) {
        // Build full operation path if parent is specified
        final String fullOperation;
        if (parent != null && operation != null) {
            fullOperation = parent + "/" + operation;
        } else if (parent != null) {
            fullOperation = parent;
        } else {
            fullOperation = operation;
        }

        return new MeterData(
                sessionUuid, // sessionUuid
                1, // position
                currentTime, // time
                0, // heap_committed
                0, // heap_max
                0, // heap_used
                0, // nonHeap_committed
                0, // nonHeap_max
                0, // nonHeap_used
                0, // objectPendingFinalizationCount
                0, // classLoading_loaded
                0, // classLoading_total
                0, // classLoading_unloaded
                0, // compilationTime
                0, // garbageCollector_count
                0, // garbageCollector_time
                usedMemory, // runtime_usedMemory
                maxMemory, // runtime_maxMemory
                0, // runtime_totalMemory
                systemLoad, // systemLoad
                TEST_CATEGORY, // category
                fullOperation, // operation
                null, // parent (not used in display)
                description, // description
                createTime, // createTime
                startTime, // startTime
                stopTime, // stopTime
                10_000_000_000L, // timeLimit (10 seconds in nanoseconds for slow detection)
                currentIteration, // currentIteration
                expectedIterations, // expectedIterations
                okPath, // okPath
                rejectPath, // rejectPath
                failPath, // failPath
                null, // failMessage - passed via separate test cases
                context // context
        );
    }

    /**
     * Creates a context map with the given key-value pairs.
     */
    private static Map<String, String> createContext(final String... keyValues) {
        final Map<String, String> context = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            context.put(keyValues[i], keyValues[i + 1]);
        }
        return context;
    }
}
