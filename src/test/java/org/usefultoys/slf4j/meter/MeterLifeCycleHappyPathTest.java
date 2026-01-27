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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4j.internal.TestTimeSource;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertLogs;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterState;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterStopTimeWindow;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterTime;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.configureLogger;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.event;

/**
 * Unit tests for {@link Meter} happy path lifecycle - successful operation completion.
 * <p>
 * This test class validates the normal, successful flow of Meter operations from creation
 * through start to successful completion via {@code ok()}. It covers all variations of
 * the success path including path handling, iteration tracking, progress reporting, and
 * time limit monitoring.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>Simple Success:</b> Created → Started → OK without any custom paths or configurations</li>
 *   <li><b>Success with Path:</b> OK completion with String, Enum, Object, and Throwable paths</li>
 *   <li><b>Path Overriding:</b> Tests path() method and ok(path) overriding previous paths</li>
 *   <li><b>Rejection Path:</b> Created → Started → Rejected (expected business rule failure)</li>
 *   <li><b>Failure Path:</b> Created → Started → Failed (unexpected technical failure)</li>
 *   <li><b>Iteration Tracking:</b> Tests currentIteration and expectedIterations with inc() and progress()</li>
 *   <li><b>Progress Reporting:</b> Validates throttling and slow detection during progress()</li>
 *   <li><b>Time Limits:</b> Tests slowness detection based on configured time limits</li>
 *   <li><b>Mixed Scenarios:</b> Combined iteration + progress + time limit scenarios</li>
 * </ul>
 * <p>
 * <b>State Transitions Tested:</b>
 * <ul>
 *   <li>Created → Started → OK (primary happy path)</li>
 *   <li>Created → Started → Rejected (expected failure)</li>
 *   <li>Created → Started → Failed (unexpected failure)</li>
 * </ul>
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleInitializationTest} - Meter construction and initialization</li>
 *   <li>{@link MeterLifeCycleTryWithResourcesTest} - Try-with-resources patterns</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Invalid termination after stop</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
 * @author Co-authored-by: Google Gemini using gemini-2.0-flash-thinking-exp
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod", "IOResourceOpenedButNotSafelyClosed", "TestMethodWithoutAssertion"})
@DisplayName("Group 2: Happy Path (Tier 1)")
class MeterLifeCycleHappyPathTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Created → Started → OK (simple)
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK (simple)")
    void shouldTransitionCreatedToStartedToOkSimple(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter completes successfully
        final long t1 = System.nanoTime();
        meter.ok();
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    // ============================================================================
    // Created → Started → OK with custom path
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with String path via ok(String)")
    void shouldTransitionCreatedToStartedToOkWithStringPathViaOkString(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter completes successfully with path
        final long t1 = System.nanoTime();
        meter.ok("success_path");
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath set
        assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "success_path"),
                event(Level.TRACE, Markers.DATA_OK, "success_path")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with String path via path() then ok()")
    void shouldTransitionCreatedToStartedToOkWithStringPathViaPathThenOk(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: path is set
        meter.path("custom_path");
        // Then: path was applied (pedagogical validation)
        assertMeterState(meter, true, false, "custom_path", null, null, null, 0, 0, 0);

        // When: meter completes successfully
        final long t1 = System.nanoTime();
        meter.ok();
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath set
        assertMeterState(meter, true, true, "custom_path", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "custom_path"),
                event(Level.TRACE, Markers.DATA_OK, "custom_path")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with Enum path")
    void shouldTransitionCreatedToStartedToOkWithEnumPath(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter completes successfully with Enum path
        final long t1 = System.nanoTime();
        meter.ok(MeterLifeCycleTestHelper.TestEnum.VALUE1);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath as Enum.toString()
        assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with enum path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "VALUE1"),
                event(Level.TRACE, Markers.DATA_OK,"VALUE1")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with Throwable path")
    void shouldTransitionCreatedToStartedToOkWithThrowablePath(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final Throwable throwable = new RuntimeException("test exception");

        // When: meter completes successfully with Throwable path
        final long t1 = System.nanoTime();
        meter.ok(throwable);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath as Throwable class name
        assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with throwable path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "RuntimeException"),
                event(Level.TRACE, Markers.DATA_OK, "RuntimeException")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with Object path")
    void shouldTransitionCreatedToStartedToOkWithObjectPath(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: meter completes successfully with Object path
        final long t1 = System.nanoTime();
        meter.ok(testObject);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath as Object.toString()
        assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with object path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "testObjectString"),
                event(Level.TRACE, Markers.DATA_OK,"testObjectString")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with ok() overriding path()")
    void shouldTransitionCreatedToStartedToOkWithOkOverridingPath(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: initial path is set
        meter.path("default_path");
        // Then: okPath = "default_path" (pedagogical validation)
        assertMeterState(meter, true, false, "default_path", null, null, null, 0, 0, 0);

        // When: ok() overrides path
        final long t1 = System.nanoTime();
        meter.ok("override_path");
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath from ok() (overrides path())
        assertMeterState(meter, true, true, "override_path", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with overridden path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "override_path"),
                event(Level.TRACE, Markers.DATA_OK,"override_path")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with multiple path() calls (last wins)")
    void shouldTransitionCreatedToStartedToOkWithMultiplePathCalls(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: first path is set
        meter.path("first");
        // Then: okPath = "first" (pedagogical validation)
        assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        // When: path is overridden
        meter.path("second");
        // Then: okPath = "second" (pedagogical validation)
        assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

        // When: path is overridden again
        meter.path("third");
        // Then: okPath = "third" (last wins)
        assertMeterState(meter, true, false, "third", null, null, null, 0, 0, 0);

        // When: meter completes
        final long t1 = System.nanoTime();
        meter.ok();
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in OK state with okPath from last path() call
        assertMeterState(meter, true, true, "third", null, null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs ok completion with last path (MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK, "third"),
                event(Level.TRACE, Markers.DATA_OK,"third")
        );
    }

    // ============================================================================
    // Created → Started → Rejected
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected with String cause")
    void shouldTransitionCreatedToStartedToRejectedWithStringCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter is rejected
        final long t1 = System.nanoTime();
        meter.reject("business_error");
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Rejected state
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs rejection (MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT,"business_error"),
                event(Level.TRACE, Markers.DATA_REJECT,"business_error")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected with Enum cause")
    void shouldTransitionCreatedToStartedToRejectedWithEnumCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter is rejected with Enum
        final long t1 = System.nanoTime();
        meter.reject(MeterLifeCycleTestHelper.TestEnum.VALUE2);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Rejected state with rejectPath as Enum.toString()
        assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs rejection with enum cause (MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT, "VALUE2"),
                event(Level.TRACE, Markers.DATA_REJECT,"VALUE2")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected with Throwable cause")
    void shouldTransitionCreatedToStartedToRejectedWithThrowableCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final Throwable throwable = new IllegalArgumentException("validation failed");

        // When: meter is rejected with Throwable
        final long t1 = System.nanoTime();
        meter.reject(throwable);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Rejected state with rejectPath as Throwable class name
        assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs rejection with throwable cause (MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT, "IllegalArgumentException"),
                event(Level.TRACE, Markers.DATA_REJECT, "IllegalArgumentException")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected with Object cause")
    void shouldTransitionCreatedToStartedToRejectedWithObjectCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: meter is started and rejected with Object
        final long t1 = System.nanoTime();
        meter.reject(testObject);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Rejected state with rejectPath as Object.toString()
        assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs rejection with object cause (MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT, "testObjectString"),
                event(Level.TRACE, Markers.DATA_REJECT, "testObjectString")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected after setting path() expectation")
    void shouldTransitionCreatedToStartedToRejectedAfterSettingPathExpectation(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path is set for expected ok
        meter.path("expected_ok_path");
        // Then: okPath = "expected_ok_path" (pedagogical validation)
        assertMeterState(meter, true, false, "expected_ok_path", null, null, null, 0, 0, 0);

        // When: meter is rejected instead
        meter.reject("business_error");

        // Then: meter should be in Rejected state with rejectPath (okPath cleared, rejection overrides expectation)
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs rejection overriding path expectation (MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT, "business_error"),
                event(Level.TRACE, Markers.DATA_REJECT, "business_error")
        );
    }

    // ============================================================================
    // Created → Started → Failed
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed with String cause")
    void shouldTransitionCreatedToStartedToFailedWithStringCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter fails
        final long t1 = System.nanoTime();
        meter.fail("technical_error");
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Failed state
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs failure (MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "technical_error"),
                event(Level.TRACE, Markers.DATA_FAIL, "technical_error")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed with Enum cause")
    void shouldTransitionCreatedToStartedToFailedWithEnumCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: meter fails with Enum
        final long t1 = System.nanoTime();
        meter.fail(MeterLifeCycleTestHelper.TestEnum.VALUE1);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Failed state with failPath as Enum.toString()
        assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs failure with enum cause (MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "VALUE1"),
                event(Level.TRACE, Markers.DATA_FAIL, "VALUE1")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed with Throwable cause")
    void shouldTransitionCreatedToStartedToFailedWithThrowableCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final Throwable throwable = new RuntimeException("system failure");

        // When: meter fails with Throwable
        final long t1 = System.nanoTime();
        meter.fail(throwable);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Failed state with failPath as class name and failMessage
        assertMeterState(meter, true, true, null, null, "java.lang.RuntimeException", "system failure", 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs failure with throwable cause (MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "java.lang.RuntimeException"),
                event(Level.TRACE, Markers.DATA_FAIL, "java.lang.RuntimeException")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed with Object cause")
    void shouldTransitionCreatedToStartedToFailedWithObjectCause(final Level level) {
        configureLogger(logger, level);

        // Given: a new started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: meter fails with Object
        final long t1 = System.nanoTime();
        meter.fail(testObject);
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Failed state with failPath as Object.toString()
        assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs failure with object cause (MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "testObjectString"),
                event(Level.TRACE, Markers.DATA_FAIL, "testObjectString")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed after setting path() expectation")
    void shouldTransitionCreatedToStartedToFailedAfterSettingPathExpectation(final Level level) {
        configureLogger(logger, level);

        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final long expectedStartTime = meter.getLastCurrentTime();

        // When: path is set for expected ok
        meter.path("expected_ok_path");
        // Then: okPath = "expected_ok_path" (pedagogical validation)
        assertMeterState(meter, true, false, "expected_ok_path", null, null, null, 0, 0, 0);

        // When: meter fails instead
        final long t1 = System.nanoTime();
        meter.fail("critical_error");
        final long t2 = System.nanoTime();
        final long expectedStopTime = meter.getLastCurrentTime();

        // Then: meter should be in Failed state with failPath (okPath cleared, failure overrides expectation)
        assertMeterState(meter, true, true, null, null, "critical_error", null, 0, 0, 0);
        // Then: meter should preserve start time and set stop time
        assertMeterTime(meter, expectedStartTime, expectedStopTime);
        // Then: meter stop time is within expected window
        assertMeterStopTimeWindow(meter, t1, t2);

        // Then: logs failure overriding path expectation (MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "critical_error"),
                event(Level.TRACE, Markers.DATA_FAIL, "critical_error")
        );
    }

    // ============================================================================
    // Created → Started → OK with mixed iterations and progress
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with mixed iterations and progress")
    void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with expected iterations configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger).withTimeSource(timeSource);
        meter.iterations(15);
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter is started, incremented with progress calls, and completes
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.ok();

        // Then: meter should be in OK state with correct iteration count
        assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);

        // Then: logs progress events + ok completion (2x MSG_PROGRESS + 2x DATA_PROGRESS + MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("Created → Started → OK with mixed iterations and consecutive progress")
    void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress2(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with expected iterations configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger).withTimeSource(timeSource);
        meter.iterations(15);
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter is started, incremented with progress calls, and completes
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

        // Second batch: no iterations
        meter.progress(); // won't print log with no more iterations
        // Then: validate state after progress() with no new iterations (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.ok();

        // Then: meter should be in OK state with correct iteration count
        assertMeterState(meter, true, true, null, null, null, null, 10, 15, 0);

        // Then: logs progress event + ok completion (MSG_PROGRESS + DATA_PROGRESS + MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with mixed iterations and progress (no time advance)")
    void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress3(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with expected iterations configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger).withTimeSource(timeSource);
        meter.iterations(15);
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter is started, incremented with progress calls, and completes
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);
        timeSource.advanceMiliseconds(0); // Execute 0ms
        meter.progress(); // won't print log since no time has passed
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.ok();

        // Then: meter should be in OK state with correct iteration count
        assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);

        // Then: progress messages should have been logged (skip indices 0 and 1 from start())
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with mixed iterations and progress (with throttling)")
    void shouldTransitionCreatedToStartedToOkWithMixedIterationsAndProgress4(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with expected iterations configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger).withTimeSource(timeSource);
        meter.iterations(15);
        MeterConfig.progressPeriodMilliseconds = 50; // Enable throttling for test

        // When: meter is started, incremented with progress calls, and completes
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 0);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress(); // won't log due to throttling (40 < 50)
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 0);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 0);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 0);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.ok();

        // Then: meter should be in OK state with correct iteration count
        assertMeterState(meter, true, true, null, null, null, null, 15, 15, 0);

        // Then: logs progress event + ok completion with throttling (MSG_PROGRESS + DATA_PROGRESS + MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    // ============================================================================
    // Created → Started → OK with time limit (NOT slow)
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with time limit (NOT slow)")
    void shouldTransitionCreatedToStartedToOkWithTimeLimitNotSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a started meter with time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .limitMilliseconds(50)
                .start();

        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 0, 50);

        // When: operation completes within time limit
        timeSource.advanceMiliseconds(10);
        meter.ok();

        // Then: meter should be in OK state and NOT slow
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 50);
        assertFalse(meter.isSlow(), "meter should NOT be slow");

        // Then: logs ok completion (not slow, MSG_OK + DATA_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    // ============================================================================
    // Created → Started → OK with time limit (IS slow)
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with time limit (IS slow)")
    void shouldTransitionCreatedToStartedToOkWithTimeLimitIsSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a started meter with time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .limitMilliseconds(50)
                .start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 0, 50);

        // When: operation exceeds time limit
        timeSource.advanceMiliseconds(100); // Execute ~100ms
        meter.ok();

        // Then: meter should be in OK state and IS slow
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 50);
        assertTrue(meter.isSlow(), "meter should be slow");

        // Then: logs slow ok completion (MSG_SLOW_OK + DATA_SLOW_OK)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.WARN, Markers.MSG_SLOW_OK),
                event(Level.TRACE, Markers.DATA_SLOW_OK)
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Reject with time limit (IS slow)")
    void shouldTransitionCreatedToStartedToRejectWithTimeLimitIsSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a started meter with time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .limitMilliseconds(50)
                .start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 0, 50);

        // When: operation exceeds time limit
        timeSource.advanceMiliseconds(100); // Execute ~100ms
        meter.reject("business_error");

        // Then: meter should be in OK state and IS slow
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 50);
        assertTrue(meter.isSlow(), "meter should be slow");

        // Then: logs rejection (slow operation does not affect reject marker, MSG_REJECT + DATA_REJECT)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_REJECT, "business_error"),
                event(Level.TRACE, Markers.DATA_REJECT, "business_error")
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Fail with time limit (IS slow)")
    void shouldTransitionCreatedToStartedToFailWithTimeLimitIsSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a started meter with time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .limitMilliseconds(50)
                .start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 0, 50);

        // When: operation exceeds time limit
        timeSource.advanceMiliseconds(100); // Execute ~100ms
        meter.fail("business_error");

        // Then: meter should be in OK state and IS slow
        assertMeterState(meter, true, true, null, null, "business_error", null, 0, 0, 50);
        assertTrue(meter.isSlow(), "meter should be slow");

        // Then: logs failure (slow operation does not affect fail marker, MSG_FAIL + DATA_FAIL)
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.ERROR, Markers.MSG_FAIL, "business_error"),
                event(Level.TRACE, Markers.DATA_FAIL, "business_error")
        );
    }

    // ============================================================================
    // Created → Started → OK with high iteration count + time limit (NOT slow)
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with high iteration count + time limit (NOT slow)")
    void shouldTransitionCreatedToStartedToOkWithHighIterationCountAndTimeLimitNotSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with iterations and time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .iterations(15)
                .limitMilliseconds(100); // Increased to ensure NOT slow
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter executes with iterations and completes within time limit
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 100);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.ok();

        // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
        assertMeterState(meter, true, true, null, null, null, null, 15, 15, 100);
        assertFalse(meter.isSlow(), "meter should NOT be slow");

        // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_OK),
                event(Level.TRACE, Markers.DATA_OK)
        );
    }

    // ============================================================================
    // Created → Started → OK with high iteration count + strict time limit (IS slow)
    // ============================================================================

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → OK with high iteration count + strict time limit (IS slow)")
    void shouldTransitionCreatedToStartedToOkWithHighIterationCountAndStrictTimeLimitIsSlow(final Level level) throws InterruptedException {
        configureLogger(logger, level);

        // Given: a meter with iterations and strict time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .iterations(15)
                .limitMilliseconds(50);
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter executes with iterations and exceeds time limit
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 50);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 50);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 50);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 50);
        timeSource.advanceMiliseconds(40); // Execute ~40ms (total ~80ms), exceeds 50ms limit
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 50);
        assertTrue(meter.isSlow(), "meter should be slow");

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 50);
        timeSource.advanceMiliseconds(40); // Execute ~40ms
        meter.ok();

        // Then: meter should be in OK state with correct iterations and IS slow
        assertMeterState(meter, true, true, null, null, null, null, 15, 15, 50);
        assertTrue(meter.isSlow(), "meter should be slow");

        // Then: WARN log includes slow operation warning with timing details (skip indices 0 and 1 from start())
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_SLOW_PROGRESS),
                event(Level.WARN, Markers.MSG_SLOW_OK),
                event(Level.TRACE, Markers.DATA_SLOW_OK)
        );
    }

    // ============================================================================
    // Created → Started → Rejected with iterations
    // ============================================================================
    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Rejected with iterations")
    void shouldTransitionCreatedToStartedToRejectedWithIterations(final Level level) {
        configureLogger(logger, level);

        // Given: a meter with iterations and time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .iterations(15)
                .limitMilliseconds(100); // Increased to ensure NOT slow
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter executes with iterations and completes within time limit
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 100);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.reject("validation_failed");

        // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
        assertMeterState(meter, true, true, null, "validation_failed", null, null, 15, 15, 100);
        assertFalse(meter.isSlow(), "meter should NOT be slow");

        // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_REJECT),
                event(Level.TRACE, Markers.DATA_REJECT)
        );
    }

    @ParameterizedTest
    @MethodSource("org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper#logLevelScenarios")
    @DisplayName("should transition Created → Started → Failed with iterations")
    void shouldTransitionCreatedToStartedToFailedWithIterations(final Level level) {
        configureLogger(logger, level);

        // Given: a meter with iterations and time limit configured
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);
        final Meter meter = new Meter(logger)
                .withTimeSource(timeSource)
                .iterations(15)
                .limitMilliseconds(100); // Increased to ensure NOT slow
        MeterConfig.progressPeriodMilliseconds = 0; // Disable throttling for test

        // When: meter executes with iterations and completes within time limit
        meter.start();
        // Then: validate configured initial state after start()
        assertMeterState(meter, true, false, null, null, null, null, 0, 15, 100);

        // First batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after first batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 5, 15, 100);

        // Second batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after second batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.progress();
        // Then: validate state after progress() - still running (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 10, 15, 100);

        // Third batch: 5 iterations
        for (int i = 0; i < 5; i++) {
            meter.inc();
        }
        // Then: validate currentIteration after third batch (pedagogical validation)
        assertMeterState(meter, true, false, null, null, null, null, 15, 15, 100);
        timeSource.advanceMiliseconds(5); // Execute ~5ms
        meter.fail("validation_failed");

        // Then: meter should be in OK state with correct iterations and NOT slow (15 < 100)
        assertMeterState(meter, true, true, null, null, "validation_failed",null, 15, 15, 100);
        assertFalse(meter.isSlow(), "meter should NOT be slow");

        // Then: completion report includes all progress metrics (skip indices 0 and 1 from start())
        assertLogs(logger, level,
                event(Level.DEBUG, Markers.MSG_START),
                event(Level.TRACE, Markers.DATA_START),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.INFO, Markers.MSG_PROGRESS),
                event(Level.TRACE, Markers.DATA_PROGRESS),
                event(Level.ERROR, Markers.MSG_FAIL),
                event(Level.TRACE, Markers.DATA_FAIL)
        );
    }
}
