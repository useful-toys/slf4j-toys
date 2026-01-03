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

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;
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
 */
@DisplayName("MeterAnalysis")
@ValidateCharset
@ResetSessionConfig
@WithLocale("en")
class MeterAnalysisTest {

    // Test scenario class that implements MeterAnalysis
    /**
     * Test scenario class that implements {@link MeterAnalysis} for parameterized testing.
     * <p>
     * Provides predefined meter states and expected outcomes for comprehensive validation
     * of MeterAnalysis interface methods.
     */
    @Getter
    static class MeterAnalysisScenario implements MeterAnalysis {
        long lastCurrentTime;
        String category;
        String operation;
        String parent;
        long createTime;
        long startTime;
        long stopTime;
        long timeLimit;
        long currentIteration;
        String okPath;
        String rejectPath;
        String failPath;

        // Expected fields for assertions
        String expectedFullID;
        boolean expectedIsStarted;
        boolean expectedIsStopped;
        boolean expectedIsOK;
        boolean expectedIsReject;
        boolean expectedIsFail;
        String expectedPath;
        double expectedIterationsPerSecond;
        long expectedExecutionTime;
        long expectedWaitingTime;
        boolean expectedIsSlow;

        // Complete constructor to facilitate scenario creation
        public MeterAnalysisScenario(
                final long lastCurrentTime,
                final String category, final String operation, final String parent,
                final long createTime, final long startTime, final long stopTime, final long timeLimit,
                final long currentIteration, final String okPath, final String rejectPath, final String failPath,
                final String expectedFullID, final boolean expectedIsStarted, final boolean expectedIsStopped,
                final boolean expectedIsOK, final boolean expectedIsReject, final boolean expectedIsFail,
                final String expectedPath, final double expectedIterationsPerSecond, final long expectedExecutionTime,
                final long expectedWaitingTime, final boolean expectedIsSlow) {
            this.lastCurrentTime = lastCurrentTime;
            this.category = category;
            this.operation = operation;
            this.parent = parent;
            this.createTime = createTime;
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.timeLimit = timeLimit;
            this.currentIteration = currentIteration;
            this.okPath = okPath;
            this.rejectPath = rejectPath;
            this.failPath = failPath;
            this.expectedFullID = expectedFullID;
            this.expectedIsStarted = expectedIsStarted;
            this.expectedIsStopped = expectedIsStopped;
            this.expectedIsOK = expectedIsOK;
            this.expectedIsReject = expectedIsReject;
            this.expectedIsFail = expectedIsFail;
            this.expectedPath = expectedPath;
            this.expectedIterationsPerSecond = expectedIterationsPerSecond;
            this.expectedExecutionTime = expectedExecutionTime;
            this.expectedWaitingTime = expectedWaitingTime;
            this.expectedIsSlow = expectedIsSlow;
        }

        @Override
        public String toString() {
            return String.format("Scenario(category='%s', operation='%s')", category, operation);
        }

        // --- Implementation of abstract methods from MeterAnalysis ---
        @Override
        public long getLastCurrentTime() {
            return lastCurrentTime;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getOperation() {
            return operation;
        }

        @Override
        public String getParent() {
            return parent;
        }

        @Override
        public long getCreateTime() {
            return createTime;
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        @Override
        public long getStopTime() {
            return stopTime;
        }

        @Override
        public long getTimeLimit() {
            return timeLimit;
        }

        @Override
        public long getCurrentIteration() {
            return currentIteration;
        }

        @Override
        public String getOkPath() {
            return okPath;
        }

        @Override
        public String getRejectPath() {
            return rejectPath;
        }

        @Override
        public String getFailPath() {
            return failPath;
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
                )
        );
    }

    /**
     * Tests that {@link MeterAnalysis#isStarted()} correctly indicates if the operation has started.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStarted should correctly indicate if the operation has started")
    void testIsStarted(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsStarted, scenario.isStarted(),
                "isStarted should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isStopped()} correctly indicates if the operation has stopped.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isStopped should correctly indicate if the operation has stopped")
    void testIsStopped(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsStopped, scenario.isStopped(),
                "isStopped should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isOK()} correctly indicates if the operation completed successfully.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isOK should correctly indicate if the operation completed successfully")
    void testIsOK(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsOK, scenario.isOK(),
                "isOK should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isReject()} correctly indicates if the operation was rejected.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isReject should correctly indicate if the operation was rejected")
    void testIsReject(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsReject, scenario.isReject(),
                "isReject should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isFail()} correctly indicates if the operation failed.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isFail should correctly indicate if the operation failed")
    void testIsFail(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsFail, scenario.isFail(),
                "isFail should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getPath()} returns the correct outcome path.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getPath should return the correct outcome path")
    void testGetPath(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedPath, scenario.getPath(),
                "Path should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getIterationsPerSecond()} calculates the correct iterations per second.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getIterationsPerSecond should calculate the correct iterations per second")
    void testGetIterationsPerSecond(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIterationsPerSecond, scenario.getIterationsPerSecond(), 0.000000001, // Delta for double comparison
                "Iterations per second should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getExecutionTime()} calculates the correct execution time.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getExecutionTime should calculate the correct execution time")
    void testGetExecutionTime(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedExecutionTime, scenario.getExecutionTime(),
                "Execution time should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#getWaitingTime()} calculates the correct waiting time.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("getWaitingTime should calculate the correct waiting time")
    void testGetWaitingTime(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedWaitingTime, scenario.getWaitingTime(),
                "Waiting time should match the expected value for scenario: " + scenario);
    }

    /**
     * Tests that {@link MeterAnalysis#isSlow()} correctly indicates if the operation is slow.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("isSlow should correctly indicate if the operation is slow")
    void testIsSlow(final MeterAnalysisScenario scenario) {
        assertEquals(scenario.expectedIsSlow, scenario.isSlow(),
                "isSlow should match the expected value for scenario: " + scenario);
    }
}
