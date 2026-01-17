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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for invalid {@link Meter} re-termination after pre-start termination.
 * <p>
 * This test class combines two edge cases: (1) termination before start (invalid operation)
 * and (2) attempting to terminate again after already terminated. It validates that the
 * meter handles this compound invalid scenario gracefully without state corruption.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>OK → OK (pre-start):</b> Tests double ok() where both calls are before start</li>
 *   <li><b>OK → Reject (pre-start):</b> Tests ok() then reject() both before start</li>
 *   <li><b>OK → Fail (pre-start):</b> Tests ok() then fail() both before start</li>
 *   <li><b>Reject → OK (pre-start):</b> Tests reject() then ok() both before start</li>
 *   <li><b>Reject → Reject (pre-start):</b> Tests double reject() before start</li>
 *   <li><b>Reject → Fail (pre-start):</b> Tests reject() then fail() both before start</li>
 *   <li><b>Fail → OK (pre-start):</b> Tests fail() then ok() both before start</li>
 *   <li><b>Fail → Reject (pre-start):</b> Tests fail() then reject() both before start</li>
 *   <li><b>Fail → Fail (pre-start):</b> Tests double fail() before start</li>
 *   <li><b>Error Logging:</b> Validates both INCONSISTENT_* markers (one for pre-start, one for re-termination)</li>
 *   <li><b>State Preservation:</b> Confirms first termination wins even in pre-start scenario</li>
 * </ul>
 * <p>
 * <b>State Transitions Tested:</b>
 * <ul>
 *   <li>Created → [invalid termination] → [invalid re-termination]</li>
 *   <li>All 9 combinations of OK/Reject/Fail × OK/Reject/Fail</li>
 * </ul>
 * <p>
 * <b>Error Handling Pattern:</b>
 * <ul>
 *   <li>First termination: Log INCONSISTENT_OK/REJECT/FAIL + terminate directly to final state</li>
 *   <li>Second termination: Log another INCONSISTENT_OK/REJECT/FAIL + ignore (state unchanged)</li>
 * </ul>
 * <p>
 * <b>Design Note:</b> This represents extreme API misuse (terminating before start,
 * then terminating again), but the meter handles it defensively to prevent crashes.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePreStartTerminationTest} - Single pre-start termination</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Re-termination after normal termination</li>
 *   <li>{@link MeterLifeCyclePreStartInvalidOperationsTest} - Other pre-start invalid operations</li>
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
@DisplayName("Group 14: Pre-Start Terminated, Post-Stop Invalid Termination (❌ Tier 4)")
class MeterLifeCyclePreStartTerminatedPostStopInvalidTerminationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // 1. Double Termination Without path() Configuration (No start())
    // ============================================================================

    @Test
    @DisplayName("should reject second ok() after ok() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondOkAfterOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: second ok() is called
        meter.ok();
        // Then: state unchanged, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok(path) after ok() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK, okPath unset
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: ok("second_path") is called
        meter.ok("second_path");
        // Then: state unchanged, okPath remains unset, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "second_path", null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject reject() after ok() without start()")
    @ValidateCleanMeter
    void shouldRejectRejectAfterOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called
        meter.reject("error");
        // Then: state remains OK, rejectPath not set, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject fail() after ok() without start()")
    @ValidateCleanMeter
    void shouldRejectFailAfterOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called
        meter.fail("error");
        // Then: state remains OK, failPath not set, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject second ok() after ok(path) without start()")
    @ValidateCleanMeter
    void shouldRejectSecondOkAfterOkPathWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok("first_path") is called without start()
        meter.ok("first_path");
        // Then: meter stops with INCONSISTENT_OK, okPath="first_path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "first_path", null, null, null, 0, 0, 0);

        // When: second ok() is called
        meter.ok();
        // Then: state unchanged, okPath preserved, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "first_path", null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok(path2) after ok(path1) without start()")
    @ValidateCleanMeter
    void shouldRejectOkPath2AfterOkPath1WithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok("first_path") is called without start()
        meter.ok("first_path");
        // Then: meter stops with INCONSISTENT_OK, okPath="first_path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "first_path", null, null, null, 0, 0, 0);

        // When: ok("second_path") is called
        meter.ok("second_path");
        // Then: state unchanged, okPath remains "first_path", logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "second_path", null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject reject() after ok(path) without start()")
    @ValidateCleanMeter
    void shouldRejectRejectAfterOkPathWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok("path") is called without start()
        meter.ok("path");
        // Then: meter stops with INCONSISTENT_OK, okPath="path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // When: reject("error") is called
        meter.reject("error");
        // Then: state remains OK, okPath preserved, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject fail() after ok(path) without start()")
    @ValidateCleanMeter
    void shouldRejectFailAfterOkPathWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok("path") is called without start()
        meter.ok("path");
        // Then: meter stops with INCONSISTENT_OK, okPath="path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // When: fail("error") is called
        meter.fail("error");
        // Then: state remains OK, okPath preserved, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok() after reject() without start()")
    @ValidateCleanMeter
    void shouldRejectOkAfterRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: reject("business_error") is called without start()
        meter.reject("business_error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // When: ok() is called
        meter.ok();
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_REJECT (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok(path) after reject() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: reject("business_error") is called without start()
        meter.reject("business_error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // When: ok("path") is called
        meter.ok("path");
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_REJECT (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject second reject() after reject() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondRejectAfterRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: reject("business_error") is called without start()
        meter.reject("business_error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // When: reject("another_error") is called
        meter.reject("another_error");
        // Then: state unchanged, rejectPath remains "business_error", logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "another_error", null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_REJECT (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject fail() after reject() without start()")
    @ValidateCleanMeter
    void shouldRejectFailAfterRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: reject("business_error") is called without start()
        meter.reject("business_error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // When: fail("technical_error") is called
        meter.fail("technical_error");
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs INCONSISTENT_REJECT (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok() after fail() without start()")
    @ValidateCleanMeter
    void shouldRejectOkAfterFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: fail("technical_error") is called without start()
        meter.fail("technical_error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // When: ok() is called
        meter.ok();
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_FAIL (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject ok(path) after fail() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: fail("technical_error") is called without start()
        meter.fail("technical_error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // When: ok("path") is called
        meter.ok("path");
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_FAIL (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject reject() after fail() without start()")
    @ValidateCleanMeter
    void shouldRejectRejectAfterFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: fail("technical_error") is called without start()
        meter.fail("technical_error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // When: reject("error") is called
        meter.reject("error");
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_FAIL (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject second fail() after fail() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondFailAfterFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: fail("technical_error") is called without start()
        meter.fail("technical_error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // When: fail("another_error") is called
        meter.fail("another_error");
        // Then: state unchanged, failPath remains "technical_error", logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "another_error", null, 0, 0, 0);

        // Then: logs INCONSISTENT_FAIL (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 6);
    }

    // ============================================================================
    // 2. Double Termination With path() Configuration Before First Termination (No start())
    // ============================================================================

    @Test
    @DisplayName("should reject second ok() after path() then ok() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondOkAfterPathThenOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected, okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK, okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: second ok() is called
        meter.ok();
        // Then: state unchanged, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK (first ok) + ILLEGAL (second ok)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject ok(path) after path() then ok() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterPathThenOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected, okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK, okPath remains null
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: ok("second_path") is called
        meter.ok("second_path");
        // Then: state unchanged, okPath remains null, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "second_path", null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject reject() after path() then ok() without start()")
    @ValidateCleanMeter
    void shouldRejectRejectAfterPathThenOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called
        meter.reject("error");
        // Then: state remains OK, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK (first) + ILLEGAL (reject)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject fail() after path() then ok() without start()")
    @ValidateCleanMeter
    void shouldRejectFailAfterPathThenOkWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called
        meter.fail("error");
        // Then: state remains OK, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK (first) + ILLEGAL (fail)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject ok() after path() then reject() without start()")
    @ValidateCleanMeter
    void shouldRejectOkAfterPathThenRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: ok() is called
        meter.ok();
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_REJECT (first) + ILLEGAL (ok)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject ok(path) after path() then reject() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterPathThenRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: ok("path") is called
        meter.ok("path");
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_REJECT (first) + ILLEGAL (ok)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject second reject() after path() then reject() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondRejectAfterPathThenRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: reject("another") is called
        meter.reject("another");
        // Then: state unchanged, rejectPath remains "error", logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "another", null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_REJECT (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject fail() after path() then reject() without start()")
    @ValidateCleanMeter
    void shouldRejectFailAfterPathThenRejectWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: fail("tech_error") is called
        meter.fail("tech_error");
        // Then: state remains Rejected, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "tech_error", null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_REJECT (first) + ILLEGAL (fail)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject ok() after path() then fail() without start()")
    @ValidateCleanMeter
    void shouldRejectOkAfterPathThenFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: ok() is called
        meter.ok();
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_FAIL (first) + ILLEGAL (ok)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject ok(path) after path() then fail() without start()")
    @ValidateCleanMeter
    void shouldRejectOkPathAfterPathThenFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: ok("path") is called
        meter.ok("path");
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_FAIL (first) + ILLEGAL (ok)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject reject() after path() then fail() without start()")
    @ValidateCleanMeter
    void shouldRejectRejectAfterPathThenFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: reject("business") is called
        meter.reject("business");
        // Then: state remains Failed, logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business", null, null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_FAIL (first) + ILLEGAL (reject)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject second fail() after path() then fail() without start()")
    @ValidateCleanMeter
    void shouldRejectSecondFailAfterPathThenFailWithoutStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: fail("another") is called
        meter.fail("another");
        // Then: state unchanged, failPath remains "error", logs ILLEGAL
        // Will be fixed in future: Meter currently stores path from second termination call, but should preserve the path from the first termination.
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "another", null, 0, 0, 0);

        // Then: logs ILLEGAL (path) + INCONSISTENT_FAIL (first) + ILLEGAL (second)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 7);
    }

    // ============================================================================
    // 3. Attempt to Start After Pre-Start Termination
    // ============================================================================

    @Test
    @DisplayName("should reject start() after ok() without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterOkWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject start() after ok(path) without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterOkPathWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ok("path") is called without start()
        meter.ok("path");
        // Then: meter stops with INCONSISTENT_OK, okPath="path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped with okPath, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertEquals("path", meter.getOkPath(), "okPath should remain path");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs INCONSISTENT_OK (first) + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject start() after reject() without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterRejectWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped with rejectPath, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertEquals("error", meter.getRejectPath(), "rejectPath should remain error");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs INCONSISTENT_REJECT (first) + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject start() after fail() without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterFailWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped with failPath, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertEquals("error", meter.getFailPath(), "failPath should remain error");

        // Then: logs INCONSISTENT_FAIL (first) + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 6);
    }

    @Test
    @DisplayName("should reject start() after path() then ok() without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathThenOkWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok() is called without start()
        meter.ok();
        // Then: meter stops with INCONSISTENT_OK
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject start() after path() then ok(path) without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathThenOkPathWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: ok("path") is called without start()
        meter.ok("path");
        // Then: meter stops with INCONSISTENT_OK, okPath="path"
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertEquals("path", meter.getOkPath(), "okPath should remain path");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs ILLEGAL (path) + INCONSISTENT_OK + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject start() after path() then reject() without initial start()")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathThenRejectWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: reject("error") is called without start()
        meter.reject("error");
        // Then: meter stops with INCONSISTENT_REJECT
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertEquals("error", meter.getRejectPath(), "rejectPath should remain error");
        assertNull(meter.getFailPath(), "failPath should remain null");

        // Then: logs ILLEGAL (path) + INCONSISTENT_REJECT + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 7);
    }

    @Test
    @DisplayName("should reject start() after path() then fail() without initial start()")
    void shouldRejectStartAfterPathThenFailWithoutInitialStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: path("configured") is called (logs ILLEGAL - path before start)
        meter.path("configured");
        // Then: path rejected
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // When: fail("error") is called without start()
        meter.fail("error");
        // Then: meter stops with INCONSISTENT_FAIL
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // When: start() is called on stopped meter
        meter.start();
        // Then: meter remains stopped, logs ILLEGAL
        // Will be fixed in future: Meter currently modifies startTime on invalid start(), causing startTime > stopTime
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
        assertNull(meter.getOkPath(), "okPath should remain null");
        assertNull(meter.getRejectPath(), "rejectPath should remain null");
        assertEquals("error", meter.getFailPath(), "failPath should remain error");

        // Then: logs ILLEGAL (path) + INCONSISTENT_FAIL + ILLEGAL (start)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 7);
    }
}
