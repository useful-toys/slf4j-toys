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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MeterAnalysis} interface default methods.
 * <p>
 * Tests validate that MeterAnalysis correctly analyzes meter data and provides
 * analytical methods for state checking, timing calculations, and performance metrics.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>State Checks:</b> Tests isStarted(), isStopped(), isOK(), isReject(), isFail()</li>
 *   <li><b>Path Resolution:</b> Tests getPath() for different outcome scenarios</li>
 *   <li><b>Timing Calculations:</b> Tests getExecutionTime() and getWaitingTime()</li>
 *   <li><b>Performance Metrics:</b> Tests getIterationsPerSecond() and isSlow()</li>
 *   <li><b>Edge Cases:</b> Tests auto-correction scenarios and zero-duration cases</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("MeterAnalysis")
@ValidateCharset
@WithLocale("en")
@ValidateCleanMeter
class MeterAnalysisTest {

    /**
     * Test implementation of {@link MeterAnalysis} for parameterized testing.
     * <p>
     * Provides a simple value object that implements MeterAnalysis with configurable
     * values for all required fields.
     */
    @Getter
    @AllArgsConstructor
    static class TestMeterAnalysis implements MeterAnalysis {
        private final long lastCurrentTime;
        private final long createTime;
        private final long startTime;
        private final long stopTime;
        private final long timeLimit;
        private final long currentIteration;
        private final String okPath;
        private final String rejectPath;
        private final String failPath;
    }

    /**
     * Provides test scenarios for MeterAnalysis validation.
     * <p>
     * Each scenario represents a different meter state with expected outcomes
     * for all analysis methods.
     *
     * @return stream of test scenarios with input values and expected results
     */
    static Stream<Arguments> provideMeterAnalysisScenarios() {
        return Stream.of(
                // Scenario 1: Not started yet (waiting)
                Arguments.of(
                        new TestMeterAnalysis(150L, 100L, 0L, 0L, 0L, 0L, null, null, null),
                        false, false, false, false, false,  // isStarted, isStopped, isOK, isReject, isFail
                        null,      // path
                        0.0,       // iterationsPerSecond
                        0L,        // executionTime
                        50L,       // waitingTime (150 - 100)
                        false,     // isSlow
                        "Not started yet (waiting)"
                ),

                // Scenario 2: Started, running (no iterations)
                Arguments.of(
                        new TestMeterAnalysis(300L, 100L, 200L, 0L, 0L, 0L, null, null, null),
                        true, false, false, false, false,
                        null,
                        0.0,       // no iterations
                        100L,      // executionTime (300 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,
                        "Started, running (no iterations)"
                ),

                // Scenario 3: Started, running with iterations
                Arguments.of(
                        new TestMeterAnalysis(400L, 100L, 200L, 0L, 0L, 50L, null, null, null),
                        true, false, false, false, false,
                        null,
                        250000000.0, // 50 iterations / 200ns * 1_000_000_000 = 250M/s
                        200L,        // executionTime (400 - 200)
                        100L,        // waitingTime (200 - 100)
                        false,
                        "Started, running with iterations"
                ),

                // Scenario 4: Completed successfully (OK)
                Arguments.of(
                        new TestMeterAnalysis(400L, 100L, 200L, 350L, 0L, 0L, "success", null, null),
                        true, true, true, false, false,
                        "success",
                        0.0,
                        150L,      // executionTime (350 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,
                        "Completed successfully (OK)"
                ),

                // Scenario 5: Completed with rejection
                Arguments.of(
                        new TestMeterAnalysis(500L, 100L, 200L, 400L, 0L, 0L, null, "rejected", null),
                        true, true, false, true, false,
                        "rejected",
                        0.0,
                        200L,      // executionTime (400 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,
                        "Completed with rejection"
                ),

                // Scenario 6: Completed with failure
                Arguments.of(
                        new TestMeterAnalysis(600L, 100L, 200L, 500L, 0L, 0L, null, null, "error"),
                        true, true, false, false, true,
                        "error",
                        0.0,
                        300L,      // executionTime (500 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,
                        "Completed with failure"
                ),

                // Scenario 7: Completed successfully with iterations
                Arguments.of(
                        new TestMeterAnalysis(500L, 100L, 200L, 400L, 0L, 100L, "success", null, null),
                        true, true, true, false, false,
                        "success",
                        500000000.0, // 100 iterations / 200ns * 1_000_000_000
                        200L,        // executionTime (400 - 200)
                        100L,        // waitingTime (200 - 100)
                        false,
                        "Completed successfully with iterations"
                ),

                // Scenario 8: Slow operation (exceeded time limit)
                Arguments.of(
                        new TestMeterAnalysis(500L, 100L, 200L, 0L, 50L, 0L, null, null, null),
                        true, false, false, false, false,
                        null,
                        0.0,
                        300L,      // executionTime (500 - 200)
                        100L,      // waitingTime (200 - 100)
                        true,      // isSlow (300 > 50 limit)
                        "Slow operation (exceeded time limit)"
                ),

                // Scenario 9: Completed slow operation
                Arguments.of(
                        new TestMeterAnalysis(600L, 100L, 200L, 400L, 50L, 0L, "success", null, null),
                        true, true, true, false, false,
                        "success",
                        0.0,
                        200L,      // executionTime (400 - 200)
                        100L,      // waitingTime (200 - 100)
                        true,      // isSlow (200 > 50 limit)
                        "Completed slow operation"
                ),

                // Scenario 10: Auto-corrected (stopped without start)
                Arguments.of(
                        new TestMeterAnalysis(300L, 100L, 0L, 300L, 0L, 0L, null, null, "error"),
                        false, true, false, false, true,
                        "error",
                        0.0,
                        0L,        // executionTime = 0 (not started)
                        200L,      // waitingTime (300 - 100, using lastCurrentTime since not started)
                        false,
                        "Auto-corrected (stopped without start)"
                ),

                // Scenario 11: Multiple paths (both isReject and isFail are true)
                Arguments.of(
                        new TestMeterAnalysis(400L, 100L, 200L, 300L, 0L, 0L, "okpath", "rejectpath", "failpath"),
                        true, true, false, true, true,
                        "failpath", // getPath: fail takes precedence
                        0.0,
                        100L,
                        100L,
                        false,
                        "Multiple paths (both isReject and isFail are true)"
                ),

                // Scenario 12: Multiple paths (reject takes precedence over ok)
                Arguments.of(
                        new TestMeterAnalysis(400L, 100L, 200L, 300L, 0L, 0L, "okpath", "rejectpath", null),
                        true, true, false, true, false,
                        "rejectpath", // reject takes precedence over ok
                        0.0,
                        100L,
                        100L,
                        false,
                        "Multiple paths (reject takes precedence over ok)"
                ),

                // Scenario 13: Zero-duration execution (instantaneous)
                Arguments.of(
                        new TestMeterAnalysis(200L, 100L, 200L, 200L, 0L, 10L, "instant", null, null),
                        true, true, true, false, false,
                        "instant",
                        0.0,       // 0 duration, cannot calculate rate
                        0L,        // executionTime (200 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,
                        "Zero-duration execution (instantaneous)"
                ),

                // Scenario 14: Time limit set but not exceeded
                Arguments.of(
                        new TestMeterAnalysis(300L, 100L, 200L, 250L, 1000L, 0L, "success", null, null),
                        true, true, true, false, false,
                        "success",
                        0.0,
                        50L,       // executionTime (250 - 200)
                        100L,      // waitingTime (200 - 100)
                        false,     // isSlow (50 < 1000 limit)
                        "Time limit set but not exceeded"
                ),

                // Scenario 15: Running with parent
                Arguments.of(
                        new TestMeterAnalysis(400L, 100L, 200L, 0L, 0L, 25L, null, null, null),
                        true, false, false, false, false,
                        null,
                        125000000.0, // 25 iterations / 200ns * 1_000_000_000
                        200L,        // executionTime (400 - 200)
                        100L,        // waitingTime (200 - 100)
                        false,
                        "Running with parent"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is started")
    void shouldCorrectlyDetermineIfMeterIsStarted(final TestMeterAnalysis meter,
                                                   final boolean expectedIsStarted,
                                                   final boolean expectedIsStopped,
                                                   final boolean expectedIsOK,
                                                   final boolean expectedIsReject,
                                                   final boolean expectedIsFail,
                                                   final String expectedPath,
                                                   final double expectedIterationsPerSecond,
                                                   final long expectedExecutionTime,
                                                   final long expectedWaitingTime,
                                                   final boolean expectedIsSlow,
                                                   final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if started
        final boolean result = meter.isStarted();

        // Then: should match expected result
        assertEquals(expectedIsStarted, result,
                String.format("isStarted() should be %s for scenario: %s", expectedIsStarted, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is stopped")
    void shouldCorrectlyDetermineIfMeterIsStopped(final TestMeterAnalysis meter,
                                                   final boolean expectedIsStarted,
                                                   final boolean expectedIsStopped,
                                                   final boolean expectedIsOK,
                                                   final boolean expectedIsReject,
                                                   final boolean expectedIsFail,
                                                   final String expectedPath,
                                                   final double expectedIterationsPerSecond,
                                                   final long expectedExecutionTime,
                                                   final long expectedWaitingTime,
                                                   final boolean expectedIsSlow,
                                                   final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if stopped
        final boolean result = meter.isStopped();

        // Then: should match expected result
        assertEquals(expectedIsStopped, result,
                String.format("isStopped() should be %s for scenario: %s", expectedIsStopped, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is OK")
    void shouldCorrectlyDetermineIfMeterIsOK(final TestMeterAnalysis meter,
                                              final boolean expectedIsStarted,
                                              final boolean expectedIsStopped,
                                              final boolean expectedIsOK,
                                              final boolean expectedIsReject,
                                              final boolean expectedIsFail,
                                              final String expectedPath,
                                              final double expectedIterationsPerSecond,
                                              final long expectedExecutionTime,
                                              final long expectedWaitingTime,
                                              final boolean expectedIsSlow,
                                              final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if OK
        final boolean result = meter.isOK();

        // Then: should match expected result
        assertEquals(expectedIsOK, result,
                String.format("isOK() should be %s for scenario: %s", expectedIsOK, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is rejected")
    void shouldCorrectlyDetermineIfMeterIsReject(final TestMeterAnalysis meter,
                                                  final boolean expectedIsStarted,
                                                  final boolean expectedIsStopped,
                                                  final boolean expectedIsOK,
                                                  final boolean expectedIsReject,
                                                  final boolean expectedIsFail,
                                                  final String expectedPath,
                                                  final double expectedIterationsPerSecond,
                                                  final long expectedExecutionTime,
                                                  final long expectedWaitingTime,
                                                  final boolean expectedIsSlow,
                                                  final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if rejected
        final boolean result = meter.isReject();

        // Then: should match expected result
        assertEquals(expectedIsReject, result,
                String.format("isReject() should be %s for scenario: %s", expectedIsReject, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is failed")
    void shouldCorrectlyDetermineIfMeterIsFail(final TestMeterAnalysis meter,
                                                final boolean expectedIsStarted,
                                                final boolean expectedIsStopped,
                                                final boolean expectedIsOK,
                                                final boolean expectedIsReject,
                                                final boolean expectedIsFail,
                                                final String expectedPath,
                                                final double expectedIterationsPerSecond,
                                                final long expectedExecutionTime,
                                                final long expectedWaitingTime,
                                                final boolean expectedIsSlow,
                                                final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if failed
        final boolean result = meter.isFail();

        // Then: should match expected result
        assertEquals(expectedIsFail, result,
                String.format("isFail() should be %s for scenario: %s", expectedIsFail, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly resolve path based on outcome")
    void shouldCorrectlyResolvePathBasedOnOutcome(final TestMeterAnalysis meter,
                                                   final boolean expectedIsStarted,
                                                   final boolean expectedIsStopped,
                                                   final boolean expectedIsOK,
                                                   final boolean expectedIsReject,
                                                   final boolean expectedIsFail,
                                                   final String expectedPath,
                                                   final double expectedIterationsPerSecond,
                                                   final long expectedExecutionTime,
                                                   final long expectedWaitingTime,
                                                   final boolean expectedIsSlow,
                                                   final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: getting path
        final String result = meter.getPath();

        // Then: should match expected path
        assertEquals(expectedPath, result,
                String.format("getPath() should be '%s' for scenario: %s", expectedPath, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly calculate iterations per second")
    void shouldCorrectlyCalculateIterationsPerSecond(final TestMeterAnalysis meter,
                                                      final boolean expectedIsStarted,
                                                      final boolean expectedIsStopped,
                                                      final boolean expectedIsOK,
                                                      final boolean expectedIsReject,
                                                      final boolean expectedIsFail,
                                                      final String expectedPath,
                                                      final double expectedIterationsPerSecond,
                                                      final long expectedExecutionTime,
                                                      final long expectedWaitingTime,
                                                      final boolean expectedIsSlow,
                                                      final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: calculating iterations per second
        final double result = meter.getIterationsPerSecond();

        // Then: should match expected rate
        assertEquals(expectedIterationsPerSecond, result, 0.001,
                String.format("getIterationsPerSecond() should be %.3f for scenario: %s",
                        expectedIterationsPerSecond, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly calculate execution time")
    void shouldCorrectlyCalculateExecutionTime(final TestMeterAnalysis meter,
                                                final boolean expectedIsStarted,
                                                final boolean expectedIsStopped,
                                                final boolean expectedIsOK,
                                                final boolean expectedIsReject,
                                                final boolean expectedIsFail,
                                                final String expectedPath,
                                                final double expectedIterationsPerSecond,
                                                final long expectedExecutionTime,
                                                final long expectedWaitingTime,
                                                final boolean expectedIsSlow,
                                                final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: calculating execution time
        final long result = meter.getExecutionTime();

        // Then: should match expected execution time
        assertEquals(expectedExecutionTime, result,
                String.format("getExecutionTime() should be %d for scenario: %s",
                        expectedExecutionTime, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly calculate waiting time")
    void shouldCorrectlyCalculateWaitingTime(final TestMeterAnalysis meter,
                                              final boolean expectedIsStarted,
                                              final boolean expectedIsStopped,
                                              final boolean expectedIsOK,
                                              final boolean expectedIsReject,
                                              final boolean expectedIsFail,
                                              final String expectedPath,
                                              final double expectedIterationsPerSecond,
                                              final long expectedExecutionTime,
                                              final long expectedWaitingTime,
                                              final boolean expectedIsSlow,
                                              final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: calculating waiting time
        final long result = meter.getWaitingTime();

        // Then: should match expected waiting time
        assertEquals(expectedWaitingTime, result,
                String.format("getWaitingTime() should be %d for scenario: %s",
                        expectedWaitingTime, scenarioDescription));
    }

    @ParameterizedTest
    @MethodSource("provideMeterAnalysisScenarios")
    @DisplayName("should correctly determine if meter is slow")
    void shouldCorrectlyDetermineIfMeterIsSlow(final TestMeterAnalysis meter,
                                                final boolean expectedIsStarted,
                                                final boolean expectedIsStopped,
                                                final boolean expectedIsOK,
                                                final boolean expectedIsReject,
                                                final boolean expectedIsFail,
                                                final String expectedPath,
                                                final double expectedIterationsPerSecond,
                                                final long expectedExecutionTime,
                                                final long expectedWaitingTime,
                                                final boolean expectedIsSlow,
                                                final String scenarioDescription) {
        // Given: a meter scenario (from parameters)
        // When: checking if slow
        final boolean result = meter.isSlow();

        // Then: should match expected result
        assertEquals(expectedIsSlow, result,
                String.format("isSlow() should be %s for scenario: %s", expectedIsSlow, scenarioDescription));
    }
}
