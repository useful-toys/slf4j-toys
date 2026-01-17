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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterState;

/**
 * Unit tests for invalid {@link Meter} operations after Failed termination.
 * <p>
 * This test class validates that lifecycle and configuration operations are invalid after
 * the meter has reached Failed state (unexpected technical failure). These operations should
 * be ignored and logged as errors without corrupting the meter's final state.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>start() after fail():</b> Tests that restarting after failure is invalid</li>
 *   <li><b>inc() after fail():</b> Tests that incrementing counter after failure is invalid</li>
 *   <li><b>progress() after fail():</b> Tests that progress reporting after failure is invalid</li>
 *   <li><b>path() after fail():</b> Tests that changing paths after failure is invalid</li>
 *   <li><b>iterations() after fail():</b> Tests that configuring iterations after failure is invalid</li>
 *   <li><b>timeLimit() after fail():</b> Tests that setting time limits after failure is invalid</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_* markers for each invalid operation</li>
 *   <li><b>State Immutability:</b> Confirms Failed state, failPath, and failMessage remain unchanged</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Failed (terminal unexpected-failure state)
 * <p>
 * <b>Error Handling Pattern:</b> Log INCONSISTENT_* error + ignore operation (state unchanged)
 * <p>
 * <b>Design Principle:</b> Once a meter reaches Failed state, it is immutable. The failure
 * path, failure message, and all other final values are locked and cannot be changed.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsOkStateTest} - Invalid operations after OK</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsRejectedStateTest} - Invalid operations after Rejected</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Invalid termination calls after stop</li>
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
@DisplayName("Group 12: Post-Stop Attribute Updates (Failed) (❌ Tier 4)")
class MeterLifeCyclePostStopInvalidOperationsFailedStateTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Update description after fail (Failed state)
    // ============================================================================
    @Test
    @DisplayName("should reject m(String) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: m("step 1") is called after stop
        meter.m("step 1");

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(String, Object...) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionWithArgsAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: m("step %d", 1) is called after stop
        meter.m("step %d", 1);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(null) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullDescriptionAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: m(null) is called after stop
        meter.m(null);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Increment operations after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject inc() after fail() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: inc() is called after stop
        meter.inc();

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incBy(5) after fail() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncByAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: incBy(5) is called after stop
        meter.incBy(5);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incTo(10) after fail() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncToAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: incTo(10) is called after stop
        meter.incTo(10);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Progress after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject progress() after fail() - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no progress message
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject progress() after inc() and fail() - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterIncAndFail() {
        // Given: a meter with inc() that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start();
        meter.inc();
        meter.fail("technical_error");
        // Then: validate meter is in stopped state with currentIteration=1 (pedagogical validation)
        assertMeterState(meter, true, true, null, null, "technical_error", null, 1, 0, 0);

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        assertMeterState(meter, true, true, null, null, "technical_error", null, 1, 0, 0);

        // Then: logs fail (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no further progress logged
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx() after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: ctx("key1", "value1") is called after stop
        meter.ctx("key1", "value1");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key1"), "should not add context after stop");
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx() update after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextUpdateAfterFail() {
        // Given: a meter with context that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start();
        meter.ctx("key", "val");
        meter.fail("technical_error");
        // Then: validate meter is in stopped state (pedagogical validation)
        // Note: context is cleared after fail() - this is expected behavior
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // When: ctx("key", "val2") is called after stop
        meter.ctx("key", "val2");

        // Then: context still not present (was cleared by fail()), state unchanged after invalid operation
        assertNull(meter.getContext().get("key"), "should not add context after stop");
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs fail (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Set path after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject path() after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path() update after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathUpdateAfterFail() {
        // Given: a meter that has been stopped with fail("original_error")
        final Meter meter = new Meter(logger).start().fail("original_error");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "original_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path(null) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullPathAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: path(null) is called after stop
        meter.path(null);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update time limit after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject limitMilliseconds() after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLimitAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds() update after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLimitUpdateAfterFail() {
        // Given: a meter with timeLimit that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start();
        meter.limitMilliseconds(100);
        meter.fail("technical_error");

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: logs ILLEGAL, meter state unchanged (timeLimit remains 100)
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 100);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(0) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroLimitAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: limitMilliseconds(0) is called after stop
        meter.limitMilliseconds(0);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-1) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeLimitAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: limitMilliseconds(-1) is called after stop
        meter.limitMilliseconds(-1);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update expected iterations after fail (Failed state)
    // ============================================================================

    @Test
    @DisplayName("should reject iterations() after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations() update after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsUpdateAfterFail() {
        // Given: a meter with expectedIterations that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start();
        meter.iterations(50);
        meter.fail("technical_error");

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: logs ILLEGAL, meter state unchanged (expectedIterations remains 50)
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 50, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(0) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroIterationsAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: iterations(0) is called after stop
        meter.iterations(0);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(-5) after fail() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeIterationsAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: iterations(-5) is called after stop
        meter.iterations(-5);

        // Then: logs ILLEGAL, meter state unchanged
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }
}
