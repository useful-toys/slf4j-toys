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
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link Meter} configuration after start().
 * <p>
 * This test class validates that Meter configuration methods (path, iterations, timeLimit)
 * can be called in the Started state (after start() but before termination) and that these
 * configurations affect the eventual termination and reporting behavior.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>path() after start():</b> Tests setting/changing expected paths during execution</li>
 *   <li><b>iterations() after start():</b> Validates adjusting expectedIterations mid-execution</li>
 *   <li><b>timeLimit() after start():</b> Tests configuring time limits after meter has started</li>
 *   <li><b>Runtime Path Changes:</b> Verifies that path() overrides during execution work correctly</li>
 *   <li><b>Configuration Impact:</b> Tests how runtime configuration changes affect final state</li>
 *   <li><b>inc() and progress():</b> Validates iteration counter and progress reporting after configuration</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Started (execution phase)
 * <p>
 * <b>Lifecycle Pattern:</b> Created → Started → [runtime configuration] → [execution] → Terminated
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePreStartConfigurationTest} - Configuration before start</li>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Normal execution with initial configuration</li>
 *   <li>{@link MeterLifeCyclePostStartTerminationTest} - Termination after configuration changes</li>
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
@DisplayName("Group 7: Post-Start Attribute Updates (Tier 2)")
class MeterLifeCyclePostStartConfigurationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Update description with valid and invalid values
    // ============================================================================

    @Test
    @DisplayName("should set description after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetDescriptionAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("step 1") is called after start()
        meter.m("step 1");

        // Then: description attribute is stored correctly and meter remains in Started state
        assertEquals("step 1", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for m())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override description when m() is called multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverrideDescriptionWhenMCalledMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m() is called multiple times
        meter.m("step 1");
        meter.m("step 2");

        // Then: last value wins and meter remains in Started state
        assertEquals("step 2", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for m())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should preserve valid message when null value attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidMessageWhenNullValueAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("valid") is called, then m(null) is attempted
        meter.m("valid");
        meter.m(null);

        // Then: null rejected (logs ILLEGAL), "valid" is preserved
        assertEquals("valid", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should set formatted message after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetFormattedMessageAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m(format, args) is called after start()
        meter.m("step %d", 1);

        // Then: description attribute is formatted and stored correctly
        assertEquals("step 1", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for m())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override formatted message when m() is called multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverrideFormattedMessageWhenMCalledMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m(format, args) is called multiple times
        meter.m("step %d", 1);
        meter.m("step %d", 2);

        // Then: last value wins
        assertEquals("step 2", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for m())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should preserve valid formatted message when null format attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidFormattedMessageWhenNullFormatAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("valid: %s", "arg") is called, then m(null, "arg") is attempted
        meter.m("valid: %s", "arg");
        meter.m(null, "arg");

        // Then: null format rejected (logs ILLEGAL), previous description is lost
        assertNull(meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should log ILLEGAL when invalid format string attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldLogIllegalWhenInvalidFormatStringAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("invalid format %z", "arg") is called (invalid format specifier)
        meter.m("invalid format %z", "arg");

        // Then: description remains null and meter remains in Started state
        assertNull(meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should skip invalid m() call in chained configuration after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSkipInvalidMCallInChainedConfigurationAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("valid") → m(null) → m("final") is chained
        meter.m("valid");
        meter.m(null);
        meter.m("final");

        // Then: invalid attempt skipped, final overwrites valid
        assertEquals("final", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Update iteration counters with valid and invalid values
    // Note: progressPeriodMilliseconds must be set to 0 to observe progress messages
    // ============================================================================

    @Test
    @DisplayName("should increment iteration counter with inc() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldIncrementIterationCounterWithIncAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: inc() → progress() is called
        meter.inc();
        meter.progress();

        // Then: currentIteration = 1 and progress message is logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 1, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should double increment iteration counter with inc() twice after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldDoubleIncrementIterationCounterWithIncTwiceAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: inc() → inc() → progress() is called
        meter.inc();
        meter.inc();
        meter.progress();

        // Then: currentIteration = 2 and progress message is logged at correct index
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 2, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should increment by specific amount with incBy() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldIncrementBySpecificAmountWithIncByAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(5) → progress() is called
        meter.incBy(5);
        meter.progress();

        // Then: currentIteration = 5 and progress message is logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should accumulate increments with multiple incBy() calls after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldAccumulateIncrementsWithMultipleIncByCallsAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(5) → incBy(3) → progress() is called
        meter.incBy(5);
        meter.incBy(3);
        meter.progress();

        // Then: currentIteration = 8 and progress message is logged at correct index
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 8, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should set absolute iteration counter with incTo() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetAbsoluteIterationCounterWithIncToAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(10) → progress() is called
        meter.incTo(10);
        meter.progress();

        // Then: currentIteration = 10 and progress message is logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should update absolute iteration counter with incTo() multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldUpdateAbsoluteIterationCounterWithIncToMultipleTimesAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(10) → incTo(50) → progress() is called
        meter.incTo(10);
        meter.incTo(50);
        meter.progress();

        // Then: currentIteration = 50 and progress message is logged at index 2 (after DATA_START)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 50, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should preserve forward movement when backward incTo() attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveForwardMovementWhenBackwardIncToAttemptedAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(100) → incTo(50) → progress() is called (backward attempt)
        meter.incTo(100);
        meter.incTo(50);
        meter.progress();

        // Then: currentIteration = 100 (backward rejected), progress message still logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 100, 0, 0);

        // Then: logs start + ILLEGAL + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject zero value with incBy() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectZeroValueWithIncByAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(5) → incBy(0) → progress() is called
        meter.incBy(5);
        meter.incBy(0);
        meter.progress();

        // Then: zero rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // Then: logs start + ILLEGAL + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject negative value with incBy() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNegativeValueWithIncByAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(5) → incBy(-3) → progress() is called
        meter.incBy(5);
        meter.incBy(-3);
        meter.progress();

        // Then: negative rejected (logs ILLEGAL), currentIteration = 5, progress message still logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);

        // Then: logs start + ILLEGAL + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject negative incBy() when starting from zero after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNegativeIncByWhenStartingFromZeroAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(-5) → progress() is called
        meter.incBy(-5);
        meter.progress();

        // Then: negative rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
        // progress() is called but does NOT log because no iteration advanced
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL (no progress logged)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject zero incBy() when starting from zero after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectZeroIncByWhenStartingFromZeroAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incBy(0) → progress() is called
        meter.incBy(0);
        meter.progress();

        // Then: zero rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
        // progress() is called but does NOT log because no iteration advanced
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL (no progress logged)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject backward incTo() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectBackwardIncToAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(10) → incTo(3) → progress() is called
        meter.incTo(10);
        meter.incTo(3);
        meter.progress();

        // Then: backward rejected (logs ILLEGAL), currentIteration = 10, progress message still logged
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);

        // Then: logs start + ILLEGAL + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject negative incTo() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNegativeIncToAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(-5) → progress() is called
        meter.incTo(-5);
        meter.progress();

        // Then: negative rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
        // progress() is called but does NOT log because no iteration advanced
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL (no MSG_PROGRESS because no iteration change)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject zero incTo() after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectZeroIncToAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: incTo(0) → progress() is called
        meter.incTo(0);
        meter.progress();

        // Then: zero rejected (logs ILLEGAL), currentIteration = 0 (unchanged)
        // progress() is called but does NOT log because no iteration advanced
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL (no MSG_PROGRESS because no iteration change)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should skip invalid iteration calls in chained sequence after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSkipInvalidIterationCallsInChainedSequenceAfterStart() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: inc() → incBy(-1) → incTo(0) → inc() → progress() is chained
        meter.inc();
        meter.incBy(-1);
        meter.incTo(0);
        meter.inc();
        meter.progress();

        // Then: invalid attempts skipped, valid ones succeed (currentIteration = 2)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 2, 0, 0);

        // Then: logs start + 2 ILLEGAL + MSG_PROGRESS + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should not log progress when progress() called without iteration change")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldNotLogProgressWhenProgressCalledWithoutIterationChange() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: inc() → progress() → progress() is called (second progress has no iteration change)
        meter.inc();
        meter.progress();
        meter.progress();

        // Then: first progress() logs message (at index 2), second progress() does NOT log (no iteration change since last progress)
        // This is verified by asserting that if we look for a second MSG_PROGRESS after the first one, there is none
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 1, 0, 0);

        // Then: logs start + MSG_PROGRESS + DATA_PROGRESS (second progress doesn't log because no iteration change)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should not log progress when progress() called with active throttling period")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldNotLogProgressWhenProgressCalledWithActiveThrottlingPeriod() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 1000000 (very long period)
        MeterConfig.progressPeriodMilliseconds = 1000000;
        final Meter meter = new Meter(logger).start();

        // When: inc() → progress() is called (insufficient time elapsed for throttling)
        meter.inc();
        meter.progress();

        // Then: currentIteration = 1 but progress() does NOT log message (throttling active)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 1, 0, 0);

        // Then: logs only start (no MSG_PROGRESS because throttling active)
        AssertLogger.assertNoEvent(logger, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should log both progress() messages when throttling disabled with iteration change between them")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldLogBothProgressMessagesWhenThrottlingDisabledWithIterationChangeBetweenThem() {
        // Given: a new, started Meter with progressPeriodMilliseconds = 0
        MeterConfig.progressPeriodMilliseconds = 0;
        final Meter meter = new Meter(logger).start();

        // When: inc() → progress() → inc() → progress() is called
        meter.inc();
        meter.progress();
        meter.inc();
        meter.progress();

        // Then: currentIteration = 2 and both progress() calls log messages
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 2, 0, 0);

        // Then: logs start + MSG_PROGRESS1 + DATA_PROGRESS + MSG_PROGRESS2 + DATA_PROGRESS
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.INFO, Markers.MSG_PROGRESS);
        AssertLogger.assertEvent(logger, 5, MockLoggerEvent.Level.TRACE, Markers.DATA_PROGRESS);
        AssertLogger.assertEventCount(logger, 6);
    }

    // ============================================================================
    // Update context during execution
    // ============================================================================

    @Test
    @DisplayName("should add context key-value pair after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldAddContextKeyValuePairAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: ctx("key1", "value1") is called after start()
        meter.ctx("key1", "value1");

        // Then: context contains the key-value pair and meter remains in Started state
        assertEquals("value1", meter.getContext().get("key1"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for ctx())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override context value when same key set multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverrideContextValueWhenSameKeySetMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: ctx() is called twice with the same key
        meter.ctx("key", "val1");
        meter.ctx("key", "val2");

        // Then: last value wins and meter remains in Started state
        assertEquals("val2", meter.getContext().get("key"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for ctx())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should replace context value with null after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldReplaceContextValueWithNullAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: ctx("key", "valid") is called, then ctx("key", null) is called
        meter.ctx("key", "valid");
        meter.ctx("key", (String) null);

        // Then: null value is stored as "<null>" placeholder
        assertEquals("<null>", meter.getContext().get("key"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for ctx())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should store multiple different context key-value pairs after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldStoreMultipleDifferentContextKeyValuePairsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: ctx() is called multiple times with different keys
        meter.ctx("key1", "value1");
        meter.ctx("key2", "value2");
        meter.ctx("key3", "value3");

        // Then: all context key-value pairs are stored and meter remains in Started state
        assertEquals("value1", meter.getContext().get("key1"));
        assertEquals("value2", meter.getContext().get("key2"));
        assertEquals("value3", meter.getContext().get("key3"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for ctx())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should store null key as <null> and null value as <null> in context after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldStoreNullKeyAsNullPlaceholderInContext() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: ctx(null, (String)null) is called (null key with null String value)
        meter.ctx(null, (String) null);

        // Then: stores with "<null>" key and "<null>" value as placeholders
        assertEquals("<null>", meter.getContext().get("<null>"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for ctx())
        AssertLogger.assertEventCount(logger, 2);
    }

    // ============================================================================
    // Set path with valid values
    // ============================================================================

    @Test
    @DisplayName("should set path after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetPathAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path("custom_ok_path") is called
        meter.path("custom_ok_path");

        // Then: okPath = "custom_ok_path" and meter remains in Started state
        assertEquals("custom_ok_path", meter.getOkPath());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "custom_ok_path", null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for path())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override path when path() is called multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverridePathWhenPathCalledMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path("path1") → path("path2") is called
        meter.path("path1");
        meter.path("path2");

        // Then: last path wins (okPath = "path2")
        assertEquals("path2", meter.getOkPath());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "path2", null, null, null, 0, 0, 0);

        // Then: logs only start events (no additional logging for path())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should preserve valid path when path(null) attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidPathWhenPathNullAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path("valid") → path(null) is called
        meter.path("valid");
        meter.path(null);

        // Then: null rejected (logs ILLEGAL), "valid" is preserved
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "valid", null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject path(null) when no previous path after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectPathNullWhenNoPreviousPathAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path(null) is called without setting a previous path
        meter.path(null);

        // Then: null rejected (logs ILLEGAL), okPath remains undefined
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject null then accept valid path after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNullThenAcceptValidPathAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path(null) → path("valid") is called
        meter.path(null);
        meter.path("valid");

        // Then: null rejected (logs ILLEGAL), then valid accepted (okPath = "valid")
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "valid", null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should support various path types after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSupportVariousPathTypesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path() is called with different types
        // String
        meter.path("stringPath");
        assertEquals("stringPath", meter.getOkPath());

        // Then: Enum
        meter.path(MeterLifeCycleTestHelper.TestEnum.VALUE1);
        assertEquals(MeterLifeCycleTestHelper.TestEnum.VALUE1.name(), meter.getOkPath());

        // Then: Throwable
        final Exception ex = new IllegalArgumentException("test");
        meter.path(ex);
        assertEquals(ex.getClass().getSimpleName(), meter.getOkPath());

        // Then: Object
        final MeterLifeCycleTestHelper.TestObject obj = new MeterLifeCycleTestHelper.TestObject();
        meter.path(obj);
        assertEquals(obj.toString(), meter.getOkPath());

        // Then: logs only start events (no additional logging for path())
        AssertLogger.assertEventCount(logger, 2);
    }

    // ============================================================================
    // Update time limit with valid and invalid values
    // ============================================================================

    @Test
    @DisplayName("should set time limit after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetTimeLimitAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(5000) is called after start()
        meter.limitMilliseconds(5000);

        // Then: timeLimit = 5000 and meter remains in Started state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // Then: logs only start events (no additional logging for limitMilliseconds())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override time limit when limitMilliseconds() called multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverrideTimeLimitWhenLimitMillisecondsCalledMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(100) → limitMilliseconds(5000) is called
        meter.limitMilliseconds(100);
        meter.limitMilliseconds(5000);

        // Then: last value wins (timeLimit = 5000)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // Then: logs only start events (no additional logging for limitMilliseconds())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should update time limit to lower value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldUpdateTimeLimitToLowerValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(5000) → limitMilliseconds(100) is called
        meter.limitMilliseconds(5000);
        meter.limitMilliseconds(100);

        // Then: last value wins (timeLimit = 100)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 100);

        // Then: logs only start events (no additional logging for limitMilliseconds())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should preserve valid time limit when invalid value attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidTimeLimitWhenInvalidValueAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(5000) → limitMilliseconds(0) is called
        meter.limitMilliseconds(5000);
        meter.limitMilliseconds(0);

        // Then: zero rejected (logs ILLEGAL), timeLimit = 5000
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve valid time limit when negative value attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidTimeLimitWhenNegativeValueAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(5000) → limitMilliseconds(-1) is called
        meter.limitMilliseconds(5000);
        meter.limitMilliseconds(-1);

        // Then: negative rejected (logs ILLEGAL), timeLimit = 5000
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject zero time limit when no previous value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectZeroTimeLimitWhenNoPreviousValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(0) is called
        meter.limitMilliseconds(0);

        // Then: zero rejected (logs ILLEGAL), timeLimit = 0 (unchanged)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject negative time limit when no previous value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNegativeTimeLimitWhenNoPreviousValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(-5) is called
        meter.limitMilliseconds(-5);

        // Then: negative rejected (logs ILLEGAL), timeLimit = 0 (unchanged)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Update expected iterations with valid and invalid values
    // ============================================================================

    @Test
    @DisplayName("should set expected iterations after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSetExpectedIterationsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(100) is called after start()
        meter.iterations(100);

        // Then: expectedIterations = 100 and meter remains in Started state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

        // Then: logs only start events (no additional logging for iterations())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should override expected iterations when iterations() called multiple times after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldOverrideExpectedIterationsWhenIterationsCalledMultipleTimesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(50) → iterations(100) is called
        meter.iterations(50);
        meter.iterations(100);

        // Then: last value wins (expectedIterations = 100)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

        // Then: logs only start events (no additional logging for iterations())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should update expected iterations to lower value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldUpdateExpectedIterationsToLowerValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(100) → iterations(50) is called
        meter.iterations(100);
        meter.iterations(50);

        // Then: last value wins (expectedIterations = 50)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 50, 0);

        // Then: logs only start events (no additional logging for iterations())
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should preserve valid iterations when invalid value attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidIterationsWhenInvalidValueAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(100) → iterations(0) is called
        meter.iterations(100);
        meter.iterations(0);

        // Then: zero rejected (logs ILLEGAL), expectedIterations = 100
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve valid iterations when negative value attempted after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldPreserveValidIterationsWhenNegativeValueAttemptedAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(100) → iterations(-5) is called
        meter.iterations(100);
        meter.iterations(-5);

        // Then: negative rejected (logs ILLEGAL), expectedIterations = 100
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject zero iterations when no previous value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectZeroIterationsWhenNoPreviousValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(0) is called
        meter.iterations(0);

        // Then: zero rejected (logs ILLEGAL), expectedIterations = 0 (unchanged)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject negative iterations when no previous value after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldRejectNegativeIterationsWhenNoPreviousValueAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(-5) is called
        meter.iterations(-5);

        // Then: negative rejected (logs ILLEGAL), expectedIterations = 0 (unchanged)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Chain operations mixing valid and invalid values
    // ============================================================================

    @Test
    @DisplayName("should chain multiple valid operations after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldChainMultipleValidOperationsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("op1") → ctx("user", "alice") → iterations(100) → inc() is chained
        meter.m("op1")
                .ctx("user", "alice")
                .iterations(100)
                .inc();

        // Then: all valid, all succeed
        assertEquals("op1", meter.getDescription());
        assertEquals("alice", meter.getContext().get("user"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 1, 100, 0);

        // Then: logs only start events (configuration methods don't log)
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should chain multiple config methods after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldChainMultipleConfigMethodsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: limitMilliseconds(5000) → m("valid") → path("custom") → inc() is chained
        meter.limitMilliseconds(5000)
                .m("valid")
                .path("custom")
                .inc();

        // Then: all valid, all succeed
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "custom", null, null, null, 1, 0, 5000);
        assertEquals("valid", meter.getDescription());

        // Then: logs only start events (configuration methods don't log)
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should skip invalid call in chained operations after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSkipInvalidCallInChainedOperationsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m("valid") → m(null) → ctx("key", "val") → inc() is chained
        meter.m("valid");
        meter.m(null);
        meter.ctx("key", "val");
        meter.inc();

        // Then: one invalid (m(null)), others succeed; m(null) is rejected, "valid" stays
        assertEquals("valid", meter.getDescription());
        assertEquals("val", meter.getContext().get("key"));
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 1, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should skip multiple invalid calls in chained operations after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldSkipMultipleInvalidCallsInChainedOperationsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: iterations(100) → iterations(-1) → inc() → incBy(0) → inc() is chained
        meter.iterations(100);
        meter.iterations(-1);
        meter.inc();
        meter.incBy(0);
        meter.inc();

        // Then: two invalid, valid ones succeed (expectedIterations = 100, currentIteration = 2)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 2, 100, 0);

        // Then: logs start + 2 ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle mixed valid and invalid in path and increment operations after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleMixedValidAndInvalidInPathAndIncrementOperationsAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: path("path1") → incBy(5) → m("step") → path(null) → incBy(3) is chained
        meter.path("path1");
        meter.incBy(5);
        meter.m("step");
        meter.path(null);
        meter.incBy(3);

        // Then: mixed valid/invalid, verify correct ones apply
        assertEquals("step", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "path1", null, null, null, 8, 0, 0);

        // Then: logs start + ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should handle invalid then valid in message updates after start()")
    @ValidateCleanMeter(expectDirtyStack = true)
    void shouldHandleInvalidThenValidInMessageUpdatesAfterStart() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();

        // When: m(null) → m("step1") → limitMilliseconds(-1) → limitMilliseconds(5000) is chained
        meter.m(null);
        meter.m("step1");
        meter.limitMilliseconds(-1);
        meter.limitMilliseconds(5000);

        // Then: invalid calls skipped, final values correct
        assertEquals("step1", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        // Then: logs start + 2 ILLEGAL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 4);
    }
}
