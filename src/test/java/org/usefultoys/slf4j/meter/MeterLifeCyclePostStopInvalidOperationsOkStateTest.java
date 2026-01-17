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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for invalid {@link Meter} operations after OK termination.
 * <p>
 * This test class validates that certain operations (start, inc, progress, configuration)
 * are invalid after the meter has reached OK state. These operations should be ignored and
 * logged as errors without corrupting the meter's final state.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>start() after ok():</b> Tests that restarting a completed meter is invalid</li>
 *   <li><b>inc() after ok():</b> Tests that incrementing iteration counter post-termination is invalid</li>
 *   <li><b>progress() after ok():</b> Tests that progress reporting after completion is invalid</li>
 *   <li><b>path() after ok():</b> Tests that changing paths after termination is invalid</li>
 *   <li><b>iterations() after ok():</b> Tests that configuring iterations post-termination is invalid</li>
 *   <li><b>timeLimit() after ok():</b> Tests that setting time limits after completion is invalid</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_* markers for each invalid operation</li>
 *   <li><b>State Immutability:</b> Confirms OK state and final values remain unchanged</li>
 *   <li><b>No Side Effects:</b> Ensures invalid operations don't trigger additional logging or state changes</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> OK (terminal success state)
 * <p>
 * <b>Error Handling Pattern:</b> Log INCONSISTENT_* error + ignore operation (state unchanged)
 * <p>
 * <b>Design Principle:</b> Once a meter reaches OK state, it is immutable. All lifecycle
 * and configuration operations become invalid and are logged as errors.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsRejectedStateTest} - Invalid operations after Rejected</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsFailedStateTest} - Invalid operations after Failed</li>
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
@DisplayName("Group 10: Post-Stop Attribute Updates (OK) (❌ Tier 4)")
class MeterLifeCyclePostStopInvalidOperationsOkStateTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Update description after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject m(String) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: m("step 1") is called after stop
        meter.m("step 1");

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(String, Object...) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionWithArgsAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: m("step %d", 1) is called after stop
        meter.m("step %d", 1);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(null) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullDescriptionAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: m(null) is called after stop
        meter.m(null);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(String) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: m("step 1") is called after stop
        meter.m("step 1");

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(String, Object...) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionWithArgsAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: m("step %d", 1) is called after stop
        meter.m("step %d", 1);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(null) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullDescriptionAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: m(null) is called after stop
        meter.m(null);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Increment operations after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject inc() after ok() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: inc() is called after stop
        meter.inc();

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incBy(5) after ok() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncByAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: incBy(5) is called after stop
        meter.incBy(5);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incTo(10) after ok() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncToAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: incTo(10) is called after stop
        meter.incTo(10);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject inc() after ok(path) - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: inc() is called after stop
        meter.inc();

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incBy(5) after ok(path) - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncByAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: incBy(5) is called after stop
        meter.incBy(5);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incTo(10) after ok(path) - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncToAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: incTo(10) is called after stop
        meter.incTo(10);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Progress after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject progress() after ok() - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no progress message
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject progress() after ok() with inc - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterOkWithInc() {
        // Given: a meter that has been stopped with ok() after inc()
        final Meter meter = new Meter(logger).start();
        meter.inc();
        meter.ok();
        // Then: validate meter is in stopped state with currentIteration=1 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 1, 0, 0);

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 1, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no further progress logged
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject progress() after ok(path) - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no progress message
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject progress() after ok(path) with inc - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterOkWithPathAndInc() {
        // Given: a meter that has been stopped with ok("completion_path") after inc()
        final Meter meter = new Meter(logger).start();
        meter.inc();
        meter.ok("completion_path");
        // Then: validate meter is in stopped state with currentIteration=1 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 1, 0, 0);

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 1, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no further progress logged
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx() after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: ctx("key1", "value1") is called after stop
        meter.ctx("key1", "value1");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key1"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx() after ok() with existing context - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextUpdateAfterOk() {
        // Given: a meter with context, stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.ctx("key", "val");
        meter.ok();
        // Then: validate meter is in stopped state (pedagogical validation)
        // Note: context is cleared after ok() - this is expected behavior
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: ctx("key", "val2") is called after stop
        meter.ctx("key", "val2");

        // Then: context still not present (was cleared by ok()), state unchanged after invalid operation
        assertNull(meter.getContext().get("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx() after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: ctx("key1", "value1") is called after stop
        meter.ctx("key1", "value1");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key1"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx() after ok(path) with existing context - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextUpdateAfterOkWithPath() {
        // Given: a meter with context, stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start();
        meter.ctx("key", "val");
        meter.ok("completion_path");
        // Then: validate meter is in stopped state (pedagogical validation)
        // Note: context is cleared after ok() - this is expected behavior
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // When: ctx("key", "val2") is called after stop
        meter.ctx("key", "val2");

        // Then: context still not present (was cleared by ok()), state unchanged after invalid operation
        assertNull(meter.getContext().get("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Set path after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject path() after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: okPath unchanged (null), state unchanged after invalid operation
        assertNull(meter.getOkPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path() after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("original_path")
        final Meter meter = new Meter(logger).start().ok("original_path");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: okPath remains "original_path", state unchanged after invalid operation
        assertEquals("original_path", meter.getOkPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "original_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path(null) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullPathAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: path(null) is called after stop
        meter.path(null);

        // Then: okPath unchanged (null), state unchanged after invalid operation
        assertNull(meter.getOkPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path() after ok(completion_path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathAfterOkWithCompletionPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: okPath remains "completion_path", state unchanged after invalid operation
        assertEquals("completion_path", meter.getOkPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path(null) after ok(completion_path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullPathAfterOkWithCompletionPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: path(null) is called after stop
        meter.path(null);

        // Then: okPath unchanged, state unchanged after invalid operation
        assertEquals("completion_path", meter.getOkPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update time limit after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject limitMilliseconds() after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectTimeLimitAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds() after ok() with existing limit - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectTimeLimitUpdateAfterOk() {
        // Given: a meter with timeLimit, stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.limitMilliseconds(100);
        meter.ok();
        // Then: validate meter is in stopped state with timeLimit (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 100);

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit remains 100ms (100,000,000 ns), state unchanged after invalid operation
        assertEquals(100_000_000L, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 100);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(0) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroTimeLimitAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: limitMilliseconds(0) is called after stop
        meter.limitMilliseconds(0);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-1) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeTimeLimitAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: limitMilliseconds(-1) is called after stop
        meter.limitMilliseconds(-1);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds() after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectTimeLimitAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds() after ok(path) with existing limit - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectTimeLimitUpdateAfterOkWithPath() {
        // Given: a meter with timeLimit, stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start();
        meter.limitMilliseconds(100);
        meter.ok("completion_path");
        // Then: validate meter is in stopped state with timeLimit (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 100);

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit remains 100ms (100,000,000 ns), state unchanged after invalid operation
        assertEquals(100_000_000L, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 100);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(0) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroTimeLimitAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: limitMilliseconds(0) is called after stop
        meter.limitMilliseconds(0);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-1) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeTimeLimitAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: limitMilliseconds(-1) is called after stop
        meter.limitMilliseconds(-1);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update expected iterations after stop (OK state)
    // ============================================================================

    @Test
    @DisplayName("should reject iterations() after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations() after ok() with existing iterations - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsUpdateAfterOk() {
        // Given: a meter with expectedIterations, stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.iterations(50);
        meter.ok();
        // Then: validate meter is in stopped state with expectedIterations (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 50, 0);

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations remains 50, state unchanged after invalid operation
        assertEquals(50, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 50, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(0) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroIterationsAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: iterations(0) is called after stop
        meter.iterations(0);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(-5) after ok() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeIterationsAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: iterations(-5) is called after stop
        meter.iterations(-5);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations() after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations() after ok(path) with existing iterations - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsUpdateAfterOkWithPath() {
        // Given: a meter with expectedIterations, stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start();
        meter.iterations(50);
        meter.ok("completion_path");
        // Then: validate meter is in stopped state with expectedIterations (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 50, 0);

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations remains 50, state unchanged after invalid operation
        assertEquals(50, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 50, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(0) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroIterationsAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: iterations(0) is called after stop
        meter.iterations(0);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(-5) after ok(path) - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeIterationsAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("completion_path")
        final Meter meter = new Meter(logger).start().ok("completion_path");

        // When: iterations(-5) is called after stop
        meter.iterations(-5);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "completion_path", null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }
}
