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
import org.usefultoys.slf4j.internal.TestTimeSource;
import org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.TimeRecord;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterStartTimePreserved;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterStopTime;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.fromStarted;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordStop;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordStopWithWindow;

/**
 * Unit tests for {@link Meter} termination after start().
 * <p>
 * This test class validates all termination methods (ok, reject, fail) when called from
 * the Started state (normal lifecycle). It covers the primary happy path and all variations
 * of termination including path handling, description preservation, and state validation.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>ok() Termination:</b> Tests successful completion without path, with path, and after path() configuration</li>
 *   <li><b>ok(path) Variations:</b> Validates path handling with String, Enum, Throwable, and Object types</li>
 *   <li><b>reject() Termination:</b> Tests expected business rule failures with various path types</li>
 *   <li><b>fail() Termination:</b> Tests unexpected technical failures with exception and message handling</li>
 *   <li><b>Path Overriding:</b> Verifies path() vs ok(path) precedence and multiple path() calls</li>
 *   <li><b>Null Handling:</b> Tests behavior when null paths are provided (error logging but graceful handling)</li>
 *   <li><b>Alias Methods:</b> Validates success() alias for ok()</li>
 *   <li><b>State Preservation:</b> Confirms descriptions, iterations, and time limits are preserved through termination</li>
 * </ul>
 * <p>
 * <b>State Transitions Tested:</b>
 * <ul>
 *   <li>Started → OK (successful completion)</li>
 *   <li>Started → Rejected (expected failure)</li>
 *   <li>Started → Failed (unexpected failure)</li>
 * </ul>
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Extended happy path scenarios with progress and iterations</li>
 *   <li>{@link MeterLifeCyclePreStartTerminationTest} - Termination before start (invalid)</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Termination after already terminated</li>
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

@DisplayName("Group 8: Post-Start Termination (✅ Tier 1)")
class MeterLifeCyclePostStartTerminationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Termination via ok() - No Path Configuration
    // ============================================================================

    @Test
    @DisplayName("should terminate via ok() without path")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithoutPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok() is called
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: meter transitions to OK state, okPath unset, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok() with description preserved")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithDescriptionPreserved() {
        // Given: a new, started Meter with description
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("operation") is called
        meter.m("operation");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        // When: ok() is called
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: description preserved, okPath unset, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("operation", meter.getDescription());

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok() with iterations preserved")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithIterationsPreserved() {
        // Given: a new, started Meter with iterations
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: inc() is called 5 times
        meter.inc().inc().inc().inc().inc();
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 5, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        // When: ok() is called
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: currentIteration = 5 preserved, INFO log with metrics
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 5, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via ok(path) with Path Type Variations
    // ============================================================================

    @Test
    @DisplayName("should terminate via ok(String path)")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithStringPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok("success_outcome") is called
        recordStopWithWindow(tr, () -> meter.ok("success_outcome"));

        // Then: okPath = "success_outcome", INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "success_outcome", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok(Enum path)")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithEnumPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok(TestEnum.VALUE1) is called
        recordStopWithWindow(tr, () -> meter.ok(MeterLifeCycleTestHelper.TestEnum.VALUE1));

        // Then: okPath = enum toString(), INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, MeterLifeCycleTestHelper.TestEnum.VALUE1.name(), null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok(Throwable path)")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithThrowablePath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final Exception ex = new Exception("success_cause");

        // When: ok(exception) is called
        recordStopWithWindow(tr, () -> meter.ok(ex));

        // Then: okPath = exception class name, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok(Object path)")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithObjectPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final MeterLifeCycleTestHelper.TestObject obj = new MeterLifeCycleTestHelper.TestObject();

        // When: ok(object) is called
        recordStopWithWindow(tr, () -> meter.ok(obj));

        // Then: okPath = object toString(), INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle ok(null) - logs ILLEGAL but completes")
    @ValidateCleanMeter
    void shouldHandleOkWithNullPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok(null) is called
        recordStopWithWindow(tr, () -> meter.ok(null));

        // Then: okPath remains unset, logs ILLEGAL, completes with INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ILLEGAL + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Termination via ok() after Single path() Call
    // ============================================================================

    @Test
    @DisplayName("should terminate via ok() after path() - path sets default")
    @ValidateCleanMeter
    void shouldTerminateViaOkAfterPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("configured_path") → ok() is called
        meter.path("configured_path");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "configured_path", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: okPath = "configured_path", INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "configured_path", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok(path) overriding previous path()")
    @ValidateCleanMeter
    void shouldTerminateViaOkPathOverridingPreviousPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("configured_path") → ok("override_path") is called
        meter.path("configured_path");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "configured_path", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok("override_path"));

        // Then: okPath = "override_path" (ok(path) overrides path())
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "override_path", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok() with description and path preserved")
    @ValidateCleanMeter
    void shouldTerminateViaOkWithDescriptionAndPathPreserved() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("step") → path("step_path") → ok() is called
        meter.m("step");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        meter.path("step_path");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "step_path", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: okPath = "step_path", description and path both preserved
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "step_path", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("step", meter.getDescription());

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle path(null) then ok() - path rejects null")
    @ValidateCleanMeter
    void shouldHandlePathNullThenOk() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path(null) → ok() is called
        meter.path(null);
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: path rejects null (ILLEGAL), okPath = null, INFO log for ok()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ILLEGAL + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Termination via ok() after Multiple path() Calls (Last Wins)
    // ============================================================================

    @Test
    @DisplayName("should terminate via ok() after two path() calls - last wins")
    @ValidateCleanMeter
    void shouldTerminateViaOkAfterTwoPathCalls() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("first") → path("second") → ok() is called
        meter.path("first");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        meter.path("second");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: okPath = "second" (last wins)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "second", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok() after three path() calls - last wins")
    @ValidateCleanMeter
    void shouldTerminateViaOkAfterThreePathCalls() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("first") → path("second") → path("third") → ok() is called
        meter.path("first");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        meter.path("second");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);

        meter.path("third");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "third", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: okPath = "third" (last wins)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "third", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via ok(path) overriding multiple path() calls")
    @ValidateCleanMeter
    void shouldTerminateViaOkPathOverridingMultiplePathCalls() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("first") → path("second") → ok("final_override") is called
        meter.path("first");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        meter.path("second");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok("final_override"));

        // Then: okPath = "final_override" (ok() overrides last path())
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "final_override", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via ok() with success() Alias
    // ============================================================================

    @Test
    @DisplayName("should terminate via success() alias")
    @ValidateCleanMeter
    void shouldTerminateViaSuccessAlias() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: success() is called
        recordStopWithWindow(tr, () -> meter.success());

        // Then: meter transitions to OK state, okPath unset, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via success(path) alias")
    @ValidateCleanMeter
    void shouldTerminateViaSuccessPathAlias() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: success("alias_path") is called
        recordStopWithWindow(tr, () -> meter.success("alias_path"));

        // Then: okPath = "alias_path", INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "alias_path", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via success() after path() - path sets default")
    @ValidateCleanMeter
    void shouldTerminateViaSuccessAfterPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("configured") → success() is called
        meter.path("configured");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "configured", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.success());

        // Then: okPath = "configured", INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // ok(null) vs path(null) Semantics
    // ============================================================================

    @Test
    @DisplayName("should handle path(validPath) then ok(null) - ok ignores null, path preserved")
    @ValidateCleanMeter
    void shouldHandlePathValidThenOkNull() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("validPath") → ok(null) is called
        meter.path("validPath");
        // Then: validate path was applied (pedagogical validation)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        // When: ok(null) is called
        recordStopWithWindow(tr, () -> meter.ok(null));

        // Then: ok() ignores null (ILLEGAL), okPath = "validPath" preserved
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "validPath", null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ILLEGAL + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Termination via reject(path) with Path Type Variations
    // ============================================================================

    @Test
    @DisplayName("should terminate via reject(String cause)")
    @ValidateCleanMeter
    void shouldTerminateViaRejectWithStringCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: reject("validation_error") is called
        recordStopWithWindow(tr, () -> meter.reject("validation_error"));

        // Then: rejectPath = "validation_error", INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via reject(Enum cause)")
    @ValidateCleanMeter
    void shouldTerminateViaRejectWithEnumCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: reject(TestEnum.VALUE1) is called
        recordStopWithWindow(tr, () -> meter.reject(MeterLifeCycleTestHelper.TestEnum.VALUE1));

        // Then: rejectPath = enum toString(), INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, MeterLifeCycleTestHelper.TestEnum.VALUE1.name(), null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via reject(Throwable cause)")
    @ValidateCleanMeter
    void shouldTerminateViaRejectWithThrowableCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final Exception ex = new IllegalArgumentException("invalid format");

        // When: reject(exception) is called
        recordStopWithWindow(tr, () -> meter.reject(ex));

        // Then: rejectPath = exception class name, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, ex.getClass().getSimpleName(), null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via reject(Object cause)")
    @ValidateCleanMeter
    void shouldTerminateViaRejectWithObjectCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final MeterLifeCycleTestHelper.TestObject obj = new MeterLifeCycleTestHelper.TestObject();

        // When: reject(object) is called
        recordStopWithWindow(tr, () -> meter.reject(obj));

        // Then: rejectPath = object toString(), INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, obj.toString(), null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via reject(path) after path() Call
    // ============================================================================

    @Test
    @DisplayName("should terminate via reject() after path() - reject path independent")
    @ValidateCleanMeter
    void shouldTerminateViaRejectAfterPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("ok_path") → reject("business_error") is called
        meter.path("ok_path");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "ok_path", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.reject("business_error"));

        // Then: rejectPath = "business_error", okPath remains unset
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via reject() after path() with description")
    @ValidateCleanMeter
    void shouldTerminateViaRejectAfterPathWithDescription() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("step") → path("ok_expectation") → reject("precondition_failed") is called
        meter.m("step");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        meter.path("ok_expectation");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "ok_expectation", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.reject("precondition_failed"));

        // Then: rejectPath = "precondition_failed", okPath unset
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "precondition_failed", null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("step", meter.getDescription());

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via reject(path) after Multiple path() Calls
    // ============================================================================

    @Test
    @DisplayName("should terminate via reject() after multiple path() calls")
    @ValidateCleanMeter
    void shouldTerminateViaRejectAfterMultiplePathCalls() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("first") → path("second") → reject("business_rule") is called
        meter.path("first");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        meter.path("second");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.reject("business_rule"));

        // Then: rejectPath = "business_rule", okPath unset
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_rule", null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via fail(path) with Path Type Variations
    // ============================================================================

    @Test
    @DisplayName("should terminate via fail(String cause)")
    @ValidateCleanMeter
    void shouldTerminateViaFailWithStringCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: fail("critical_error") is called
        recordStopWithWindow(tr, () -> meter.fail("critical_error"));

        // Then: failPath = "critical_error", failMessage = null, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "critical_error", null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via fail(Enum cause)")
    @ValidateCleanMeter
    void shouldTerminateViaFailWithEnumCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: fail(TestEnum.VALUE2) is called
        recordStopWithWindow(tr, () -> meter.fail(MeterLifeCycleTestHelper.TestEnum.VALUE2));

        // Then: failPath = enum toString(), failMessage = null, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, MeterLifeCycleTestHelper.TestEnum.VALUE2.name(), null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via fail(Throwable cause) - stores className and message separately")
    @ValidateCleanMeter
    void shouldTerminateViaFailWithThrowableCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final Exception ex = new SQLException("connection refused");

        // When: fail(exception) is called
        recordStopWithWindow(tr, () -> meter.fail(ex));

        // Then: failPath = className, failMessage = message (separated), ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, ex.getClass().getName(), ex.getMessage(), 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via fail(Object cause)")
    @ValidateCleanMeter
    void shouldTerminateViaFailWithObjectCause() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        final MeterLifeCycleTestHelper.TestObject obj = new MeterLifeCycleTestHelper.TestObject();

        // When: fail(object) is called
        recordStopWithWindow(tr, () -> meter.fail(obj));

        // Then: failPath = object toString(), failMessage = null, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, obj.toString(), null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via fail(path) after path() Call
    // ============================================================================

    @Test
    @DisplayName("should terminate via fail() after path() - fail path independent")
    @ValidateCleanMeter
    void shouldTerminateViaFailAfterPath() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("success_expectation") → fail("timeout") is called
        meter.path("success_expectation");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "success_expectation", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.fail("timeout"));

        // Then: failPath = "timeout", okPath remains unset, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "timeout", null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should terminate via fail() after path() with description")
    @ValidateCleanMeter
    void shouldTerminateViaFailAfterPathWithDescription() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("operation") → path("ok_path") → fail("unexpected_exception") is called
        meter.m("operation");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        meter.path("ok_path");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "ok_path", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.fail("unexpected_exception"));

        // Then: failPath = "unexpected_exception", okPath unset, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "unexpected_exception", null, 0, 0, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("operation", meter.getDescription());

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination via fail(path) after Multiple path() Calls
    // ============================================================================

    @Test
    @DisplayName("should terminate via fail() after multiple path() calls")
    @ValidateCleanMeter
    void shouldTerminateViaFailAfterMultiplePathCalls() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: path("first") → path("second") → fail("system_error") is called
        meter.path("first");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "first", null, null, null, 0, 0, 0);

        meter.path("second");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "second", null, null, null, 0, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.fail("system_error"));

        // Then: failPath = "system_error", okPath unset, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "system_error", null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Complex Chains with Attributes + Termination
    // ============================================================================

    @Test
    @DisplayName("should handle complex chain: description + iterations + path + ok()")
    @ValidateCleanMeter
    void shouldHandleComplexChainWithOk() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("operation") → iterations(100) → inc() × 50 → path("checkpoint") → ok() is called
        meter.m("operation");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        meter.iterations(100);
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 100, 0);

        for (int i = 0; i < 50; i++) {
            meter.inc();
        }
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 50, 100, 0);

        meter.path("checkpoint");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "checkpoint", null, null, null, 50, 100, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.ok());

        // Then: description, iterations, okPath all preserved, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "checkpoint", null, null, null, 50, 100, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("operation", meter.getDescription());

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle complex chain: timeLimit + iterations + path + reject()")
    @ValidateCleanMeter
    void shouldHandleComplexChainWithReject() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: limitMilliseconds(5000) → inc() × 25 → path("expected") → reject("performance") is called
        meter.limitMilliseconds(5000);
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 5000);

        for (int i = 0; i < 25; i++) {
            meter.inc();
        }
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 25, 0, 5000);

        meter.path("expected");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "expected", null, null, null, 25, 0, 5000);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.reject("performance_degradation"));

        // Then: timeLimit, iterations, rejectPath all correct, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "performance_degradation", null, null, 25, 0, 5000);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle complex chain: description + context + iterations + fail()")
    @ValidateCleanMeter
    void shouldHandleComplexChainWithFail() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: m("critical") → ctx("user", "admin") → inc() × 10 → fail("auth_failure") is called
        meter.m("critical");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

        meter.ctx("user", "admin");
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
        assertEquals("admin", meter.getContext().get("user"));

        for (int i = 0; i < 10; i++) {
            meter.inc();
        }
        MeterLifeCycleTestHelper.assertMeterState(meter, true, false, null, null, null, null, 10, 0, 0);
        assertMeterStartTimePreserved(meter, tr);

        recordStopWithWindow(tr, () -> meter.fail("auth_failure"));

        // Then: description, iterations, failPath all preserved, ERROR log (context is cleared after emission)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "auth_failure", null, 10, 0, 0);
        assertMeterStopTime(meter, tr);
        assertEquals("critical", meter.getDescription());

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Termination after No Operations (Minimal Meter)
    // ============================================================================

    @Test
    @DisplayName("should handle minimal meter: start() then ok()")
    @ValidateCleanMeter
    void shouldHandleMinimalMeterOk() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: ok() is called
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: clean transition with no additional attributes, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + ok + DATA_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle minimal meter: start() then reject()")
    @ValidateCleanMeter
    void shouldHandleMinimalMeterReject() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: reject("no_work_done") is called
        recordStopWithWindow(tr, () -> meter.reject("no_work_done"));

        // Then: rejectPath captured, INFO log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "no_work_done", null, null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + reject + DATA_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should handle minimal meter: start() then fail()")
    @ValidateCleanMeter
    void shouldHandleMinimalMeterFail() {
        // Given: a new, started Meter
        final Meter meter = new Meter(logger).start();
        final TimeRecord tr = fromStarted(meter);

        // When: fail("no_work_done") is called
        recordStopWithWindow(tr, () -> meter.fail("no_work_done"));

        // Then: failPath captured, ERROR log
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "no_work_done", null, 0, 0, 0);
        assertMeterStopTime(meter, tr);

        // Then: logs start + fail + DATA_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Slow Operation Detection - Marker and Level Changes
    // ============================================================================

    @Test
    @DisplayName("should detect slow operation for ok() - marker changes to MSG_SLOW_OK, level to WARN")
    @ValidateCleanMeter
    void shouldDetectSlowOperationForOk() {
        // Given: a controllable time source with initial time (DAY1)
        final TestTimeSource timeSource = new TestTimeSource(TestTimeSource.DAY1);

        // Given: a new Meter with time limit
        final Meter meter = new Meter(logger).withTimeSource(timeSource);
        meter.limitMilliseconds(10);

        // When: meter starts
        meter.start();
        final TimeRecord tr = fromStarted(meter);

        // When: time advances beyond limit (simulate 50ms elapsed)
        timeSource.advanceMiliseconds(50);

        // When: ok() is called
        recordStop(tr, () -> meter.ok());

        // Then: isSlow() = true, WARN log with MSG_SLOW_OK marker (not MSG_OK)
        assertTrue(meter.isSlow());
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 10);
        assertMeterStopTime(meter, tr);

        // Then: logs start + slow_ok + DATA_SLOW_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.WARN, Markers.MSG_SLOW_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_SLOW_OK);
        AssertLogger.assertEventCount(logger, 4);
    }
}
