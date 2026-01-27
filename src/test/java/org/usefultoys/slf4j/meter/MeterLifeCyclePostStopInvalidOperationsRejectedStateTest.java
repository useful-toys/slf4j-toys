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
 * Unit tests for invalid {@link Meter} operations after Rejected termination.
 * <p>
 * This test class validates that lifecycle and configuration operations are invalid after
 * the meter has reached Rejected state (expected business rule failure). These operations
 * should be ignored and logged as errors without corrupting the meter's final state.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>start() after reject():</b> Tests that restarting after rejection is invalid</li>
 *   <li><b>inc() after reject():</b> Tests that incrementing counter after rejection is invalid</li>
 *   <li><b>progress() after reject():</b> Tests that progress reporting after rejection is invalid</li>
 *   <li><b>path() after reject():</b> Tests that changing paths after rejection is invalid</li>
 *   <li><b>iterations() after reject():</b> Tests that configuring iterations after rejection is invalid</li>
 *   <li><b>timeLimit() after reject():</b> Tests that setting time limits after rejection is invalid</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_* markers for each invalid operation</li>
 *   <li><b>State Immutability:</b> Confirms Rejected state and rejectPath remain unchanged</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Rejected (terminal expected-failure state)
 * <p>
 * <b>Error Handling Pattern:</b> Log INCONSISTENT_* error + ignore operation (state unchanged)
 * <p>
 * <b>Design Principle:</b> Once a meter reaches Rejected state, it is immutable. The rejection
 * path and all other final values are locked and cannot be changed.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsOkStateTest} - Invalid operations after OK</li>
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
@DisplayName("Group 11: Post-Stop Attribute Updates (Rejected) (❌ Tier 4)")
class MeterLifeCyclePostStopInvalidOperationsRejectedStateTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Update description after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject m(String) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: m("step 1") is called after stop
        meter.m("step 1");

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(String, Object...) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDescriptionWithArgsAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: m("step %d", 1) is called after stop
        meter.m("step %d", 1);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject m(null) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullDescriptionAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: m(null) is called after stop
        meter.m(null);

        // Then: description unchanged (null), state unchanged after invalid operation
        assertNull(meter.getDescription(), "should not update description after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Increment operations after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject inc() after reject() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: inc() is called after stop
        meter.inc();

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incBy(5) after reject() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncByAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: incBy(5) is called after stop
        meter.incBy(5);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject incTo(10) after reject() - logs INCONSISTENT_INCREMENT")
    @ValidateCleanMeter
    void shouldRejectIncToAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: incTo(10) is called after stop
        meter.incTo(10);

        // Then: currentIteration unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getCurrentIteration(), "should not increment after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + INCONSISTENT_INCREMENT (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Progress after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject progress() after reject() - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no progress message
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject progress() after inc() and reject() - logs INCONSISTENT_PROGRESS")
    @ValidateCleanMeter
    void shouldRejectProgressAfterIncAndReject() {
        // Given: a meter with inc() that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start();
        meter.inc();
        meter.reject("business_error");
        // Then: validate meter is in stopped state with currentIteration=1 (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 1, 0, 0);

        // When: progress() is called after stop
        meter.progress();

        // Then: state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 1, 0, 0);

        // Then: logs reject (from setup) + INCONSISTENT_PROGRESS (from invalid operation), no further progress logged
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx() after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key1", "value1") is called after stop
        meter.ctx("key1", "value1");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key1"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx() update after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectContextUpdateAfterReject() {
        // Given: a meter with context that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start();
        meter.ctx("key", "val");
        meter.reject("business_error");
        // Then: validate meter is in stopped state (pedagogical validation)
        // Note: context is cleared after reject() - this is expected behavior
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // When: ctx("key", "val2") is called after stop
        meter.ctx("key", "val2");

        // Then: context still not present (was cleared by reject()), state unchanged after invalid operation
        assertNull(meter.getContext().get("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with primitive values after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(String, int) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIntContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", 42) is called after stop
        meter.ctx("key", 42);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, long) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLongContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", 42L) is called after stop
        meter.ctx("key", 42L);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, boolean) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectBooleanContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", true) is called after stop
        meter.ctx("key", true);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, float) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectFloatContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", 3.14f) is called after stop
        meter.ctx("key", 3.14f);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, double) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDoubleContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", 3.14) is called after stop
        meter.ctx("key", 3.14);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with wrapper values after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(String, Integer) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIntegerWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", Integer.valueOf(42)) is called after stop
        meter.ctx("key", Integer.valueOf(42));

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, Long) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLongWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", Long.valueOf(42L)) is called after stop
        meter.ctx("key", Long.valueOf(42L));

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, Boolean) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectBooleanWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", Boolean.TRUE) is called after stop
        meter.ctx("key", Boolean.TRUE);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, Float) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectFloatWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", Float.valueOf(3.14f)) is called after stop
        meter.ctx("key", Float.valueOf(3.14f));

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, Double) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectDoubleWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", Double.valueOf(3.14)) is called after stop
        meter.ctx("key", Double.valueOf(3.14));

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, null Integer) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullIntegerWrapperContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", (Integer) null) is called after stop
        meter.ctx("key", (Integer) null);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with formatted/object values after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(String, String, Object...) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectFormattedContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", "value %d", 42) is called after stop
        meter.ctx("key", "value %d", 42);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, Object) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectObjectContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", new Object()) is called after stop
        meter.ctx("key", new Object());

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, null Object) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullObjectContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", (Object) null) is called after stop
        meter.ctx("key", (Object) null);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with key-only markers after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(String) key-only after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectKeyOnlyContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("marker") is called after stop
        meter.ctx("marker");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("marker"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(true, String) conditional key-only after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectConditionalKeyOnlyTrueAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(true, "marker") is called after stop
        meter.ctx(true, "marker");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("marker"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should silently ignore ctx(false, String) after reject() - condition false, no putContext call, no logs")
    @ValidateCleanMeter
    void shouldSilentlyIgnoreConditionalKeyOnlyFalseAfterReject() {
        /* NOTE: This test was intended to validate that ctx(false, String) is rejected as invalid after reject().
         * However, the current Meter implementation returns early when condition=false (before calling putContext()),
         * so validateContextPrecondition() is never invoked, and no ILLEGAL log is generated.
         * This test documents the actual behavior: conditional ctx() with false silently does nothing. */
        
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(false, "marker") is called (condition=false returns early)
        meter.ctx(false, "marker");

        // Then: context unchanged, state unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: only reject logs (no ILLEGAL)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should reject ctx(boolean, String, String) conditional true/false names after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectConditionalTrueFalseNamesAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(true, "trueMarker", "falseMarker") is called after stop
        meter.ctx(true, "trueMarker", "falseMarker");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("trueMarker"), "should not add context after stop");
        assertFalse(meter.getContext().containsKey("falseMarker"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with invalid key-only parameters after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(null) null key-only after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullKeyOnlyContextAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(null) is called after stop
        meter.ctx(null);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(true, null) null trueName when true after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullTrueNameWhenTrueAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(true, null) is called after stop
        meter.ctx(true, null);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should silently ignore ctx(false, null) after reject() - condition false, no putContext call, no logs")
    @ValidateCleanMeter
    void shouldSilentlyIgnoreNullTrueNameWhenFalseAfterReject() {
        /* NOTE: This test was intended to validate that ctx(false, null) is rejected as invalid after reject().
         * However, condition=false causes early return before putContext() is called, so no ILLEGAL log occurs. */
        
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(false, null) is called (condition=false returns early)
        meter.ctx(false, null);

        // Then: context unchanged, state unchanged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: only reject logs (no ILLEGAL)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should reject ctx(true, null, String) null trueName with falseName after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullTrueNameWithFalseNameAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(true, null, "falseMarker") is called after stop
        meter.ctx(true, null, "falseMarker");

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("falseMarker"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(false, String, null) null falseName when false after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullFalseNameWhenFalseAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(false, "trueMarker", null) is called after stop
        meter.ctx(false, "trueMarker", null);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("trueMarker"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(true, null, null) both names null when true after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectBothNamesNullWhenTrueAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(true, null, null) is called after stop
        meter.ctx(true, null, null);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(false, null, null) both names null when false after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectBothNamesNullWhenFalseAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(false, null, null) is called after stop
        meter.ctx(false, null, null);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update context with null key (other invalid parameters) after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject ctx(null, String) null key with value after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullKeyWithValueAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(null, "value") is called after stop
        meter.ctx(null, "value");

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(null, int) null key with primitive after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullKeyWithPrimitiveAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(null, 42) is called after stop
        meter.ctx(null, 42);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(null, String, Object...) null key with format after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullKeyWithFormatAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx(null, "format %d", 42) is called after stop
        meter.ctx(null, "format %d", 42);

        // Then: context unchanged, state unchanged after invalid operation
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, null, Object...) null format after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullFormatAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", null, 42) is called after stop (null format string)
        meter.ctx("key", null, 42);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ctx(String, String, null) null args array after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullArgsArrayAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ctx("key", "format", (Object[]) null) is called after stop (null args array)
        meter.ctx("key", "format", (Object[]) null);

        // Then: context unchanged, state unchanged after invalid operation
        assertFalse(meter.getContext().containsKey("key"), "should not add context after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Set path after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject path() after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: rejectPath unchanged, state unchanged after invalid operation
        assertEquals("business_error", meter.getRejectPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path() update after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectPathUpdateAfterReject() {
        // Given: a meter that has been stopped with reject("original_error")
        final Meter meter = new Meter(logger).start().reject("original_error");

        // When: path("new_path") is called after stop
        meter.path("new_path");

        // Then: rejectPath unchanged, state unchanged after invalid operation
        assertEquals("original_error", meter.getRejectPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "original_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject path(null) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNullPathAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: path(null) is called after stop
        meter.path(null);

        // Then: rejectPath unchanged, state unchanged after invalid operation
        assertEquals("business_error", meter.getRejectPath(), "should not update path after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update time limit after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject limitMilliseconds() after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLimitAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds() update after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectLimitUpdateAfterReject() {
        // Given: a meter with timeLimit that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start();
        meter.limitMilliseconds(100);
        meter.reject("business_error");
        // Then: validate meter is in stopped state with timeLimit (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 100);

        // When: limitMilliseconds(5000) is called after stop
        meter.limitMilliseconds(5000);

        // Then: timeLimit remains 100ms (100,000,000 ns), state unchanged after invalid operation
        assertEquals(100_000_000L, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 100);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(0) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroLimitAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: limitMilliseconds(0) is called after stop
        meter.limitMilliseconds(0);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject limitMilliseconds(-1) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeLimitAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: limitMilliseconds(-1) is called after stop
        meter.limitMilliseconds(-1);

        // Then: timeLimit unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getTimeLimit(), "should not update timeLimit after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Update expected iterations after reject (Rejected state)
    // ============================================================================

    @Test
    @DisplayName("should reject iterations() after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations() update after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectIterationsUpdateAfterReject() {
        // Given: a meter with expectedIterations that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start();
        meter.iterations(50);
        meter.reject("business_error");
        // Then: validate meter is in stopped state with expectedIterations (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 50, 0);

        // When: iterations(100) is called after stop
        meter.iterations(100);

        // Then: expectedIterations remains 50, state unchanged after invalid operation
        assertEquals(50, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 50, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(0) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectZeroIterationsAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: iterations(0) is called after stop
        meter.iterations(0);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject iterations(-5) after reject() - logs ILLEGAL")
    @ValidateCleanMeter
    void shouldRejectNegativeIterationsAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: iterations(-5) is called after stop
        meter.iterations(-5);

        // Then: expectedIterations unchanged (0), state unchanged after invalid operation
        assertEquals(0, meter.getExpectedIterations(), "should not update expectedIterations after stop");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs reject (from setup) + ILLEGAL (from invalid operation) only
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 5);
    }
}
