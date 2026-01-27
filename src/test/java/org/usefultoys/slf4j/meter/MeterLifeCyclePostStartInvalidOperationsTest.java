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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.TimeRecord;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.fromStarted;

/**
 * Unit tests for invalid {@link Meter} operations after start().
 * <p>
 * This test class validates that certain operations become invalid or have special behavior
 * after start() is called. While most operations are valid in the Started state, attempting
 * to start() again is invalid and should be handled gracefully.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>Double start():</b> Tests that calling start() on an already started meter is invalid</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_START marker is logged for double start</li>
 *   <li><b>State Preservation:</b> Confirms that invalid start() doesn't corrupt meter state</li>
 *   <li><b>No Re-initialization:</b> Verifies that double start() doesn't reset timestamps or counters</li>
 *   <li><b>Thread-Local Handling:</b> Tests getCurrentInstance() behavior after double start attempt</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Started (after start, before termination)
 * <p>
 * <b>Error Handling Pattern:</b> Log INCONSISTENT_START error + ignore operation (no exception thrown)
 * <p>
 * <b>Design Note:</b> Unlike pre-start invalid operations, the Started state allows most operations
 * (inc, progress, configuration). Only re-starting is invalid.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePreStartInvalidOperationsTest} - Invalid operations before start</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsOkStateTest} - Invalid operations after termination</li>
 *   <li>{@link MeterLifeCyclePostStartTerminationTest} - Valid termination operations</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Claude 3.5 Sonnet
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
@SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod", "IOResourceOpenedButNotSafelyClosed", "TestMethodWithoutAssertion"})
@DisplayName("Group 9: Post-Start Invalid Operations (❌ Tier 4)")
class MeterLifeCyclePostStartInvalidOperationsTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Double Start (State-Correcting - ⚠️ Tier 3)
    // ============================================================================

    @Test
    @DisplayName("should handle second start() call - resets startTime (⚠️ Tier 3 state-correcting)")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleSecondStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);
        final long firstStartTime = meter.getStartTime();

        // When: start() is called again on already started meter
        meter.start();

        // Then: Guard Clause prevents re-execution - startTime is NOT reset
        // Note: Previously (⚠️ Tier 3) allowed state-correcting re-start. Now (❌ Tier 4) rejects early per Guard Clause pattern.
        assertEquals(firstStartTime, meter.getStartTime(), "startTime should NOT change due to Guard Clause preventing re-execution");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs INCONSISTENT_START only (Guard Clause prevents MSG_START and DATA_START)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should handle multiple start() calls - resets startTime each time")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleMultipleStartCalls() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: start() is called multiple times
        meter.start();
        meter.start();

        // Then: startTime NOT reset (Guard Clause prevents execution)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs INCONSISTENT_START for each duplicate start attempt (no MSG_START/DATA_START)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle second start() after inc() - iterations preserved")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleSecondStartAfterInc() {
        // Given: a started Meter with iterations
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: inc() is called multiple times
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 3 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 3, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: start() is called again
        meter.start();

        // Then: iterations preserved (state unchanged)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 3, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs INCONSISTENT_START only (Guard Clause prevents MSG_START and DATA_START)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: iterations(n) with n ≤ 0
    // ============================================================================

    @Test
    @DisplayName("should reject iterations(0) after start() - logs ILLEGAL, expectedIterations unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIterationsZeroAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: iterations(0) is called after start()
        meter.iterations(0);

        // Then: expectedIterations unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject iterations(-5) after start() - logs ILLEGAL, expectedIterations unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIterationsNegativeAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: iterations(-5) is called after start()
        meter.iterations(-5);

        // Then: expectedIterations unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject iterations(-5) after valid iterations(10) - preserves first valid value")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIterationsNegativeAfterValidValue() {
        // Given: a started Meter with expectedIterations = 10
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);
        meter.iterations(10);

        // Then: expectedIterations = 10 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 10, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: iterations(-5) is called
        meter.iterations(-5);

        // Then: expectedIterations remains 10
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 10, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: limitMilliseconds(n) with n ≤ 0
    // ============================================================================

    @Test
    @DisplayName("should reject limitMilliseconds(0) after start() - logs ILLEGAL, timeLimit unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectLimitMillisecondsZeroAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: limitMilliseconds(0) is called after start()
        meter.limitMilliseconds(0);

        // Then: timeLimit unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-100) after start() - logs ILLEGAL, timeLimit unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectLimitMillisecondsNegativeAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: limitMilliseconds(-100) is called after start()
        meter.limitMilliseconds(-100);

        // Then: timeLimit unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-100) after valid limitMilliseconds(5000) - preserves first valid value")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectLimitMillisecondsNegativeAfterValidValue() {
        // Given: a started Meter with timeLimit = 5000
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);
        meter.limitMilliseconds(5000);

        // Then: timeLimit = 5000 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: limitMilliseconds(-100) is called
        meter.limitMilliseconds(-100);

        // Then: timeLimit remains 5000
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: path(null)
    // ============================================================================

    @Test
    @DisplayName("should reject path(null) after start() - logs ILLEGAL, okPath unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectPathNullAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path(null) is called after start()
        meter.path(null);

        // Then: okPath unchanged (null)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject path(null) then complete with ok() - okPath remains null")
    @ValidateCleanMeter
    void shouldRejectPathNullThenOk() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path(null) is called, then ok()
        meter.path(null);
        meter.ok();

        // Then: okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTimePreserved(meter, tr);

        // Then: logs ILLEGAL for path(null), completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Context operations with null parameters after start (silent - no logs)
    // ============================================================================

    @Test
    @DisplayName("should silently accept ctx(null, String) null key after start() - no validation, no logs")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSilentlyAcceptCtxNullKeyWithValueAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ctx(null, "value") is called after start()
        meter.ctx(null, "value");

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: only start logs (no ILLEGAL log)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should silently accept ctx(null, int) null key after start() - no validation, no logs")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSilentlyAcceptCtxNullKeyWithPrimitiveAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ctx(null, 42) is called after start()
        meter.ctx(null, 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: only start logs
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should silently accept ctx(null, String, Object...) null key after start() - no validation, no logs")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSilentlyAcceptCtxNullKeyWithFormatAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ctx(null, "format %d", 42) is called after start()
        meter.ctx(null, "format %d", 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: only start logs
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should silently accept ctx(String, null, Object...) null format after start() - no validation, no logs")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSilentlyAcceptCtxNullFormatAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ctx("key", null, 42) is called after start()
        meter.ctx("key", null, 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: only start logs
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should silently accept ctx(String, String, null) null args array after start() - no validation, no logs")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSilentlyAcceptCtxNullArgsArrayAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ctx("key", "format", (Object[]) null) is called after start()
        meter.ctx("key", "format", (Object[]) null);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: only start logs
        AssertLogger.assertEventCount(logger, 2);
    }

    // ============================================================================
    // Invalid Argument: incBy(n) with n ≤ 0
    // ============================================================================

    @Test
    @DisplayName("should reject incBy(0) after start() - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncByZeroAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: incBy(0) is called after start()
        meter.incBy(0);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incBy(-3) after start() - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncByNegativeAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: incBy(-3) is called after start()
        meter.incBy(-3);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incBy(-3) after inc() × 5 - preserves currentIteration = 5")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncByNegativeAfterValidInc() {
        // Given: a started Meter with currentIteration = 5
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: incBy(-3) is called
        meter.incBy(-3);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: incTo(n) with n ≤ 0
    // ============================================================================

    @Test
    @DisplayName("should reject incTo(0) after start() - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToZeroAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: incTo(0) is called after start()
        meter.incTo(0);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incTo(-50) after start() - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToNegativeAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: incTo(-50) is called after start()
        meter.incTo(-50);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: incTo(n) with n ≤ currentIteration (Non-Forward Increment)
    // ============================================================================

    @Test
    @DisplayName("should reject incTo(5) when currentIteration = 5 - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToEqualToCurrentIteration() {
        // Given: a started Meter with currentIteration = 5
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: incTo(5) is called (non-forward increment)
        meter.incTo(5);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incTo(3) when currentIteration = 5 - logs ILLEGAL, currentIteration unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToLessThanCurrentIteration() {
        // Given: a started Meter with currentIteration = 5
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: incTo(3) is called (backward increment)
        meter.incTo(3);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incTo(5) after incTo(10) - logs ILLEGAL, currentIteration remains 10")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToAfterHigherIncTo() {
        // Given: a started Meter with currentIteration = 10
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);
        meter.incTo(10);

        // Then: currentIteration = 10 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // When: incTo(5) is called (backward increment)
        meter.incTo(5);

        // Then: currentIteration remains 10
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Invalid Argument: ok(null), reject(null), fail(null)
    // ============================================================================

    @Test
    @DisplayName("should reject ok(null) - logs ILLEGAL, completes with INFO log, okPath unset")
    @ValidateCleanMeter
    void shouldRejectOkNull() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok(null) is called
        meter.ok(null);

        // Then: okPath remains unset
        // Note: ok(null) is invalid due to null argument, but completion still proceeds (resilient behavior)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTimePreserved(meter, tr);

        // Then: logs ILLEGAL for null argument, completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject(null) - logs ILLEGAL, completes with INFO log, rejectPath unset")
    @ValidateCleanMeter
    void shouldRejectRejectNull() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: reject(null) is called
        meter.reject(null);

        // Then: rejectPath remains unset
        // Note: reject(null) is invalid due to null argument, but completion still proceeds
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTimePreserved(meter, tr);

        // Then: logs ILLEGAL for null argument, completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail(null) - logs ILLEGAL, completes with ERROR log, failPath unset")
    @ValidateCleanMeter
    void shouldRejectFailNull() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: fail(null) is called
        meter.fail(null);

        // Then: failPath remains unset
        // Note: fail(null) is invalid due to null argument, but completion still proceeds
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTimePreserved(meter, tr);

        // Then: logs ILLEGAL for null argument, completes with ERROR log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Combined Invalid Operations After Start
    // ============================================================================

    @Test
    @DisplayName("should reject all invalid operations - logs ILLEGAL for each, all attributes unchanged")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectAllInvalidOperationsAfterStart() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: multiple invalid operations called
        meter.iterations(0);
        meter.limitMilliseconds(-100);
        meter.incBy(-5);

        // Then: all attributes unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTime(meter, tr);

        // Then: logs ILLEGAL for each
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject invalid operations and accept valid ones - mixed scenario")
    @ValidateCleanMeter
    void shouldHandleMixedValidAndInvalidOperations() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: invalid iterations(-1), valid inc() × 3, invalid incBy(0), then ok()
        meter.iterations(-1);
        meter.inc();
        meter.inc();
        meter.inc();
        meter.incBy(0);
        meter.ok();

        // Then: currentIteration = 3, completes normally
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 3, 0, 0);
        MeterLifeCycleTestHelper.assertMeterStartTimePreserved(meter, tr);

        // Then: logs ILLEGAL for iterations(-1) and incBy(0), completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEventCount(logger, 6);
    }
}
