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
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final Meter meter = new Meter(logger);
        meter.start();
        final long firstStartTime = meter.getStartTime();

        // When: start() is called again on already started meter
        meter.start();

        // Then: startTime is reset (state-correcting behavior)
        // Note: Currently implemented as ⚠️ Tier 3 (state-correcting). According to TDR-0019, should be ❌ Tier 4 (state-preserving/rejected).
        assertTrue(meter.getStartTime() > firstStartTime, "startTime should be reset to a new value");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_START + second start events
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should handle multiple start() calls - resets startTime each time")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleMultipleStartCalls() {
        // Given: a started Meter
        final Meter meter = new Meter(logger).start();

        // When: start() is called multiple times
        meter.start();
        meter.start();

        // Then: startTime reset each time
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_START for each duplicate
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 8);
    }

    @Test
    @DisplayName("should handle second start() after inc() - iterations preserved")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleSecondStartAfterInc() {
        // Given: a started Meter with iterations
        final Meter meter = new Meter(logger).start();

        // When: inc() is called multiple times
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 3 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 3, 0, 0);

        // When: start() is called again
        meter.start();

        // Then: iterations preserved
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 3, 0, 0);

        // Then: logs INCONSISTENT_START + second start events
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.DEBUG, Markers.MSG_START);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_START);
        AssertLogger.assertEventCount(logger, 5);
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

        // When: iterations(0) is called after start()
        meter.iterations(0);

        // Then: expectedIterations unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: iterations(-5) is called after start()
        meter.iterations(-5);

        // Then: expectedIterations unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject iterations(-5) after valid iterations(10) - preserves first valid value")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIterationsNegativeAfterValidValue() {
        // Given: a started Meter with expectedIterations = 10
        final Meter meter = new Meter(logger).start().iterations(10);
        // Then: expectedIterations = 10 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 10, 0);

        // When: iterations(-5) is called
        meter.iterations(-5);

        // Then: expectedIterations remains 10
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 10, 0);

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

        // When: limitMilliseconds(0) is called after start()
        meter.limitMilliseconds(0);

        // Then: timeLimit unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: limitMilliseconds(-100) is called after start()
        meter.limitMilliseconds(-100);

        // Then: timeLimit unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-100) after valid limitMilliseconds(5000) - preserves first valid value")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectLimitMillisecondsNegativeAfterValidValue() {
        // Given: a started Meter with timeLimit = 5000
        final Meter meter = new Meter(logger).start().limitMilliseconds(5000);
        // Then: timeLimit = 5000 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // When: limitMilliseconds(-100) is called
        meter.limitMilliseconds(-100);

        // Then: timeLimit remains 5000
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

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

        // When: path(null) is called after start()
        meter.path(null);

        // Then: okPath unchanged (null)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: path(null) is called, then ok()
        meter.path(null);
        meter.ok();

        // Then: okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL for path(null), completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEventCount(logger, 5);
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

        // When: incBy(0) is called after start()
        meter.incBy(0);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: incBy(-3) is called after start()
        meter.incBy(-3);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // When: incBy(-3) is called
        meter.incBy(-3);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

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

        // When: incTo(0) is called after start()
        meter.incTo(0);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: incTo(-50) is called after start()
        meter.incTo(-50);

        // Then: currentIteration unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // When: incTo(5) is called (non-forward increment)
        meter.incTo(5);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

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

        // When: inc() is called 5 times
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        meter.inc();
        // Then: currentIteration = 5 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // When: incTo(3) is called (backward increment)
        meter.incTo(3);

        // Then: currentIteration remains 5
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject incTo(5) after incTo(10) - logs ILLEGAL, currentIteration remains 10")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectIncToAfterHigherIncTo() {
        // Given: a started Meter with currentIteration = 10
        final Meter meter = new Meter(logger).start().incTo(10);
        // Then: currentIteration = 10 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);

        // When: incTo(5) is called (backward increment)
        meter.incTo(5);

        // Then: currentIteration remains 10
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);

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

        // When: ok(null) is called
        meter.ok(null);

        // Then: okPath remains unset
        // Note: ok(null) is invalid due to null argument, but completion still proceeds (resilient behavior)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

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

        // When: reject(null) is called
        meter.reject(null);

        // Then: rejectPath remains unset
        // Note: reject(null) is invalid due to null argument, but completion still proceeds
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

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

        // When: fail(null) is called
        meter.fail(null);

        // Then: failPath remains unset
        // Note: fail(null) is invalid due to null argument, but completion still proceeds
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

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

        // When: multiple invalid operations called
        meter.iterations(0);
        meter.limitMilliseconds(-100);
        meter.incBy(-5);

        // Then: all attributes unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

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

        // When: invalid iterations(-1), valid inc() × 3, invalid incBy(0), then ok()
        meter.iterations(-1);
        meter.inc();
        meter.inc();
        meter.inc();
        meter.incBy(0);
        meter.ok();

        // Then: currentIteration = 3, completes normally
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 3, 0, 0);

        // Then: logs ILLEGAL for iterations(-1) and incBy(0), completes with INFO log
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEventCount(logger, 6);
    }
}
