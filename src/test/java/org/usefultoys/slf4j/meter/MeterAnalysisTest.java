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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MeterAnalysis}.
 * <p>
 * Tests validate that MeterAnalysis correctly analyzes meter states and calculates performance metrics
 * based on timing data, iteration counts, and outcome paths.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Operation State Detection:</b> Verifies isStarted(), isStopped(), isOK(), isReject(), and isFail() methods correctly identify operation states based on start/stop times and paths</li>
 *   <li><b>Outcome Path Resolution:</b> Tests getPath() method returns correct outcome (OK, reject, fail) based on configured paths</li>
 *   <li><b>Performance Metrics:</b> Validates getIterationsPerSecond() calculation using iteration count and elapsed time</li>
 *   <li><b>Timing Calculations:</b> Ensures getExecutionTime() and getWaitingTime() compute correct durations from create/start/stop times</li>
 *   <li><b>Slow Operation Detection:</b> Tests isSlow() identifies operations exceeding time limits</li>
 *   <li><b>Edge Cases:</b> Covers scenarios with no operation, null values, zero iterations, and various timing combinations</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot (Gemini 3 Flash (Preview))
 */
@DisplayName("MeterAnalysis")
@ValidateCharset
@ResetSessionConfig
@WithLocale("en")
@ValidateCleanMeter
class MeterAnalysisLegacyTest {

    // Test scenario class that implements MeterAnalysis
    /**
     * Test scenario class that implements {@link MeterAnalysis} for parameterized testing.
     * <p>
     * Provides predefined meter states and expected outcomes for comprehensive validation
     * of MeterAnalysis interface methods.
     */
    @Getter
    @AllArgsConstructor
    static class MeterAnalysisScenario implements MeterAnalysis {
        /** The current time for the last update. */
        private final long lastCurrentTime;
        /** The category of the operation. */
        private final String category;
        /** The name of the operation. */
        private final String operation;
        /** The parent operation ID. */
        private final String parent;
        /** The time when the operation was created. */
        private final long createTime;
        /** The time when the operation started. */
        private final long startTime;
        /** The time when the operation stopped. */
        private final long stopTime;
        /** The time limit for the operation. */
        private final long timeLimit;
        /** The current iteration count. */
        private final long currentIteration;
        /** The path for successful completion. */
        private final String okPath;
        /** The path for rejection. */
        private final String rejectPath;
        /** The path for failure. */
        private final String failPath;

        // Expected fields for assertions
        /** The expected full ID of the operation. */
        private final String expectedFullID;
        /** Whether the operation is expected to be started. */
        private final boolean expectedIsStarted;
        /** Whether the operation is expected to be stopped. */
        private final boolean expectedIsStopped;
        /** Whether the operation is expected to be OK. */
        private final boolean expectedIsOK;
        /** Whether the operation is expected to be rejected. */
        private final boolean expectedIsReject;
        /** Whether the operation is expected to be failed. */
        private final boolean expectedIsFail;
        /** The expected outcome path. */
        private final String expectedPath;
        /** The expected iterations per second. */
        private final double expectedIterationsPerSecond;
        /** The expected execution time. */
        private final long expectedExecutionTime;
        /** The expected waiting time. */
        private final long expectedWaitingTime;
        /** Whether the operation is expected to be slow. */
        private final boolean expectedIsSlow;

        @Override
        public String toString() {
            return String.format("Scenario(category='%s', operation='%s')", category, operation);
        }
    }

    /**
     * Provides a stream of test scenarios for {@link MeterAnalysis} validation.
     * <p>
     * Each scenario represents different meter states (started, stopped, OK, reject, fail)
     * with expected outcomes for all MeterAnalysis methods.
     *
     * @return stream of MeterAnalysisScenario instances for parameterized tests
     */
    static Stream<MeterAnalysisScenario> provideMeterAnalysisScenarios() {
        return Stream.of(
                // Scenario 1: Operation in progress, no failure/rejection
                new MeterAnalysisScenario(
                        1000L,
                        "cat1", "op1", null,
                        100L, 500L, 0L, 0L,
                        10L, "ok", null, null,
                        "cat1/op1#1", true, false,
                        false, false, false,
                        "ok", 10.0 / (1000.0 - 500.0) * 1_000_000_000, 500L,
                        400L, false
                ),
                // Scenario 2: Operation completed successfully
                new MeterAnalysisScenario(
                        1500L,
                        "cat1", "op1", null,
                        100L, 500L, 1200L, 0L,
                        20L, "ok", null, null,
                        "cat1/op1#2", true, true,
                        true, false, false,
                        "ok", 20.0 / (1200.0 - 500.0) * 1_000_000_000, 700L,
                        400L, false
                ),
                // Scenario 3: Operation rejected
                new MeterAnalysisScenario(
                        2000L,
                        "cat2", "op2", null,
                        200L, 600L, 1800L, 0L,
                        5L, null, "rejected", null,
                        "cat2/op2#3", true, true,
                        false, true, false,
                        "rejected", 5.0 / (1800.0 - 600.0) * 1_000_000_000, 1200L,
                        400L, false
                ),
                // Scenario 4: Operation failed
                new MeterAnalysisScenario(
                        2500L,
                        "cat3", "op3", null,
                        300L, 700L, 2200L, 0L,
                        1L, null, null, "failed",
                        "cat3/op3#4", true, true,
                        false, false, true,
                        "failed", 1.0 / (2200.0 - 700.0) * 1_000_000_000, 1500L,
                        400L, false
                ),
                // Scenario 5: Operation not started
                new MeterAnalysisScenario(
                        500L,
                        "cat4", "op4", null,
                        100L, 0L, 0L, 0L,
                        0L, null, null, null,
                        "cat4/op4#5", false, false,
                        false, false, false,
                        null, 0.0, 0L,
                        400L, false
                ),
                // Scenario 6: Slow operation
                new MeterAnalysisScenario(
                        3000L,
                        "cat5", "op5", null,
                        400L, 800L, 2800L, 1000L, // timeLimit = 1000, executionTime = 2000
                        10L, "ok", null, null,
                        "cat5/op5#6", true, true,
                        true, false, false,
                        "ok", 10.0 / (2800.0 - 800.0) * 1_000_000_000, 2000L,
                        400L, true
                ),
                // Scenario 7: Fast operation (not slow)
                new MeterAnalysisScenario(
                        3500L,
                        "cat6", "op6", null,
                        500L, 900L, 1200L, 1000L, // timeLimit = 1000, executionTime = 300
                        5L, "ok", null, null,
                        "cat6/op6#7", true, true,
                        true, false, false,
                        "ok", 5.0 / (1200.0 - 900.0) * 1_000_000_000, 300L,
                        400L, false
                ),
                // Scenario 8: getFullID without operation
                new MeterAnalysisScenario(
                        3500L,
                        "cat7", null, null,
                        500L, 900L, 1200L, 1000L,
                        5L, "ok", null, null,
                        "cat7#8", true, true,
                        true, false, false,
                        "ok", 5.0 / (1200.0 - 900.0) * 1_000_000_000, 300L,
                        400L, false
                ),
                // Scenario 9: Zero iterations - getIterationsPerSecond should return 0.0
                new MeterAnalysisScenario(
                        2500L,
                        "cat8", "op8", null,
                        100L, 500L, 2000L, 0L,
                        0L, "ok", null, null, // Zero iterations
                        "cat8/op8#9", true, true,
                        true, false, false,
                        "ok", 0.0, 1500L,
                        400L, false
                ),
                // Scenario 10: Zero duration when stopped (auto-corrected) - getIterationsPerSecond should return 0.0
                new MeterAnalysisScenario(
                        1500L,
                        "cat9", "op9", null,
                        500L, 1500L, 1500L, 0L, // startTime == stopTime (zero duration)
                        10L, "ok", null, null,
                        "cat9/op9#10", true, true,
                        true, false, false,
                        "ok", 0.0, 0L, // Zero duration means zero iterations per second
                        1000L, false
                ),
                // Scenario 11: Started but not stopped - execution time based on lastCurrentTime
                new MeterAnalysisScenario(
                        5000L,
                        "cat10", "op10", null,
                        100L, 1000L, 0L, 0L, // Not stopped
                        15L, "ok", null, null,
                        "cat10/op10#11", true, false,
                        false, false, false,
                        null, 15.0 / (5000.0 - 1000.0) * 1_000_000_000, 4000L,
                        900L, false
                ),
                // Scenario 12: Zero iterations and zero duration - both getIterationsPerSecond and getExecutionTime are zero
                new MeterAnalysisScenario(
                        2000L,
                        "cat11", "op11", null,
                        500L, 2000L, 2000L, 0L, // startTime == stopTime
                        0L, "ok", null, null, // Zero iterations
                        "cat11/op11#12", true, true,
                        true, false, false,
                        "ok", 0.0, 0L,
                        1500L, false
                ),
                // Scenario 13: Operation with reject path but still running - no execution time yet
                new MeterAnalysisScenario(
                        1200L,
                        "cat12", "op12", null,
                        100L, 500L, 0L, 0L, // Not stopped yet
                        8L, null, "rejected", null,
                        "cat12/op12#13", true, false,
                        false, false, false,
                        "rejected", 8.0 / (1200.0 - 500.0) * 1_000_000_000, 700L,
                        400L, false
                ),
                // Scenario 14: Large iteration count with very small duration
                new MeterAnalysisScenario(
                        1100L,
                        "cat13", "op13", null,
                        100L, 1000L, 1100L, 0L, // 100 nanosecond duration
                        1000L, "ok", null, null,
                        "cat13/op13#14", true, true,
                        true, false, false,
                        "ok", 1000.0 / (1100.0 - 1000.0) * 1_000_000_000, 100L,
                        900L, false
                ),
                // Scenario 15: Operation with fail path and slow timing
                new MeterAnalysisScenario(
                        4500L,
                        "cat14", "op14", null,
                        500L, 1000L, 4000L, 1000L, // executionTime = 3000, timeLimit = 1000 (slow)
                        20L, null, null, "failed",
                        "cat14/op14#15", true, true,
                        false, false, true,
                        "failed", 20.0 / (4000.0 - 1000.0) * 1_000_000_000, 3000L,
                        500L, true
                )
        );
    }

    /**
     * Tests that {@link MeterAnalysis#isStarted()} correctly indicates if the operation has started.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStarted should correctly indicate if the operation has started")
    void shouldCorrectlyIndicateIfOperationHasStarted(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation has started
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsStarted(), scenario.isStarted(),
                "isStarted should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isStopped()} correctly indicates if the operation has stopped.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStopped should correctly indicate if the operation has stopped")
    void shouldCorrectlyIndicateIfOperationHasStopped(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation has stopped
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsStopped(), scenario.isStopped(),
                "isStopped should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isOK()} correctly indicates if the operation completed successfully.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isOK should correctly indicate if the operation completed successfully")
    void shouldCorrectlyIndicateIfOperationCompletedSuccessfully(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation completed successfully
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsOK(), scenario.isOK(),
                "isOK should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isReject()} correctly indicates if the operation was rejected.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isReject should correctly indicate if the operation was rejected")
    void shouldCorrectlyIndicateIfOperationWasRejected(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation was rejected
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsReject(), scenario.isReject(),
                "isReject should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isFail()} correctly indicates if the operation failed.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isFail should correctly indicate if the operation failed")
    void shouldCorrectlyIndicateIfOperationFailed(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation failed
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsFail(), scenario.isFail(),
                "isFail should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getPath()} returns the correct outcome path.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getPath should return the correct outcome path")
    void shouldReturnCorrectOutcomePath(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: retrieving the outcome path
        // Then: it should match the expected value
        assertEquals(scenario.getExpectedPath(), scenario.getPath(),
                "Path should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getIterationsPerSecond()} calculates the correct iterations per second.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getIterationsPerSecond should calculate the correct iterations per second")
    void shouldCalculateCorrectIterationsPerSecond(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: calculating iterations per second
        // Then: it should match the expected value
        assertEquals(scenario.getExpectedIterationsPerSecond(), scenario.getIterationsPerSecond(), 0.000000001, // Delta for double comparison
                "Iterations per second should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getExecutionTime()} calculates the correct execution time.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getExecutionTime should calculate the correct execution time")
    void shouldCalculateCorrectExecutionTime(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: calculating execution time
        // Then: it should match the expected value
        assertEquals(scenario.getExpectedExecutionTime(), scenario.getExecutionTime(),
                "Execution time should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getWaitingTime()} calculates the correct waiting time.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getWaitingTime should calculate the correct waiting time")
    void shouldCalculateCorrectWaitingTime(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: calculating waiting time
        // Then: it should match the expected value
        assertEquals(scenario.getExpectedWaitingTime(), scenario.getWaitingTime(),
                "Waiting time should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isSlow()} correctly indicates if the operation is slow.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isSlow should correctly indicate if the operation is slow")
    void shouldCorrectlyIndicateIfOperationIsSlow(final MeterAnalysisScenario scenario) {
        // Given: a meter analysis scenario
        // When: checking if the operation is slow
        // Then: it should match the expected value
        assertEquals(scenario.isExpectedIsSlow(), scenario.isSlow(),
                "isSlow should match the expected value for scenario: " + scenario);
    }
}
