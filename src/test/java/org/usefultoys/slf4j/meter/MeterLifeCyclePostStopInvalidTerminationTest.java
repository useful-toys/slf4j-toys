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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.assertMeterState;

/**
 * Unit tests for invalid {@link Meter} termination after stop.
 * <p>
 * This test class validates that calling termination methods (ok, reject, fail) on an already
 * terminated Meter is invalid and results in appropriate error logging without changing the
 * meter's final state. These tests ensure idempotency and defensive programming.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>Double ok():</b> Tests calling ok() on a meter already in OK state</li>
 *   <li><b>ok() after reject():</b> Tests attempting success termination after rejection</li>
 *   <li><b>ok() after fail():</b> Tests attempting success after failure</li>
 *   <li><b>Double reject():</b> Tests calling reject() on already rejected meter</li>
 *   <li><b>reject() after ok():</b> Tests attempting rejection after success</li>
 *   <li><b>Double fail():</b> Tests calling fail() on already failed meter</li>
 *   <li><b>Cross-termination:</b> Tests all invalid combinations of termination methods</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_* markers for each invalid termination attempt</li>
 *   <li><b>State Immutability:</b> Confirms first termination wins, subsequent calls are ignored</li>
 *   <li><b>Path Preservation:</b> Verifies original termination path is preserved</li>
 * </ul>
 * <p>
 * <b>States Tested:</b> OK, Rejected, Failed (all terminal states)
 * <p>
 * <b>Error Handling Pattern:</b> Log INCONSISTENT_* error + ignore operation (state unchanged)
 * <p>
 * <b>Design Principle:</b> First termination wins. Once terminated, the meter's final state
 * is immutable. Subsequent termination attempts are logged as errors but don't corrupt state.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePostStartTerminationTest} - Valid termination from Started state</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsOkStateTest} - Other invalid operations after OK</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsRejectedStateTest} - Other invalid operations after Rejected</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsFailedStateTest} - Other invalid operations after Failed</li>
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
@DisplayName("Group 13: Post-Stop Invalid Termination (❌ Tier 4)")
class MeterLifeCyclePostStopInvalidTerminationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Double Termination Without path() Configuration
    // ============================================================================

    @Test
    @DisplayName("should reject ok() after ok() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: ok() is called again
        meter.ok();

        // Then: state unchanged (OK) - first termination wins
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs ok (from setup) + INCONSISTENT_OK (from invalid operation)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after ok() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: ok("second_path") is called
        meter.ok("second_path");

        // Then: okPath remains unset - first termination wins
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after ok() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: reject("error") is called
        meter.reject("error");

        // Then: state should remain OK - first termination wins
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after ok() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: fail("error") is called
        meter.fail("error");

        // Then: state should remain OK - first termination wins
        assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok() after ok(path) - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("first_path")
        final Meter meter = new Meter(logger).start().ok("first_path");

        // When: ok() is called again
        meter.ok();

        // Then: okPath remains "first_path" - first termination wins
        assertMeterState(meter, true, true, "first_path", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after ok(path) - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("first_path")
        final Meter meter = new Meter(logger).start().ok("first_path");

        // When: ok("second_path") is called
        meter.ok("second_path");

        // Then: okPath remains "first_path" - first termination wins
        assertMeterState(meter, true, true, "first_path", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after ok(path) - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("path")
        final Meter meter = new Meter(logger).start().ok("path");

        // When: reject("error") is called
        meter.reject("error");

        // Then: state remains OK with okPath - first termination wins
        assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after ok(path) - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("path")
        final Meter meter = new Meter(logger).start().ok("path");

        // When: fail("error") is called
        meter.fail("error");

        // Then: state remains OK with okPath - first termination wins
        assertMeterState(meter, true, true, "path", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok() after reject() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ok() is called
        meter.ok();

        // Then: state remains Rejected - first termination wins
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after reject() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: ok("path") is called
        meter.ok("path");

        // Then: state remains Rejected - first termination wins
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after reject() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: reject("another_error") is called
        meter.reject("another_error");

        // Then: rejectPath remains "business_error" - first termination wins
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after reject() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterReject() {
        // Given: a meter that has been stopped with reject("business_error")
        final Meter meter = new Meter(logger).start().reject("business_error");

        // When: fail("technical_error") is called
        meter.fail("technical_error");

        // Then: state remains Rejected - first termination wins
        assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok() after fail() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: ok() is called
        meter.ok();

        // Then: state remains Failed - first termination wins
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after fail() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: ok("path") is called
        meter.ok("path");

        // Then: state remains Failed - first termination wins
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after fail() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: reject("error") is called
        meter.reject("error");

        // Then: state remains Failed - first termination wins
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after fail() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterFail() {
        // Given: a meter that has been stopped with fail("technical_error")
        final Meter meter = new Meter(logger).start().fail("technical_error");

        // When: fail("another_error") is called
        meter.fail("another_error");

        // Then: failPath remains "technical_error" - first termination wins
        assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Double Termination With path() Configuration Before First Termination
    // ============================================================================

    @Test
    @DisplayName("should reject ok() after path()->ok() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterPathAndOk() {
        // Given: a meter configured with path() and stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok();

        // When: ok() is called again
        meter.ok();

        // Then: okPath remains "configured" - first termination wins
        assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after path()->ok() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterPathAndOk() {
        // Given: a meter configured with path() and stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok();

        // When: ok("second_path") is called
        meter.ok("second_path");

        // Then: okPath remains "configured" - first termination wins
        assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after path()->ok() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterPathAndOk() {
        // Given: a meter configured with path() and stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok();

        // When: reject("error") is called
        meter.reject("error");

        // Then: state remains OK with okPath - first termination wins
        assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after path()->ok() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterPathAndOk() {
        // Given: a meter configured with path() and stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok();

        // When: fail("error") is called
        meter.fail("error");

        // Then: state remains OK with okPath - first termination wins
        assertMeterState(meter, true, true, "configured", null, null, null, 0, 0, 0);

        // Then: logs start + first ok + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok() after path()->reject() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterPathAndReject() {
        // Given: a meter configured with path() and stopped with reject()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.reject("error");

        // When: ok() is called
        meter.ok();

        // Then: state remains Rejected with rejectPath - first termination wins
        assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after path()->reject() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterPathAndReject() {
        // Given: a meter configured with path() and stopped with reject()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.reject("error");

        // When: ok("path") is called
        meter.ok("path");

        // Then: state remains Rejected - first termination wins
        assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after path()->reject() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterPathAndReject() {
        // Given: a meter configured with path() and stopped with reject()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.reject("error");

        // When: reject("another") is called
        meter.reject("another");

        // Then: rejectPath remains "error" - first termination wins
        assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after path()->reject() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterPathAndReject() {
        // Given: a meter configured with path() and stopped with reject()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.reject("error");

        // When: fail("tech_error") is called
        meter.fail("tech_error");

        // Then: state remains Rejected - first termination wins
        assertMeterState(meter, true, true, null, "error", null, null, 0, 0, 0);

        // Then: logs start + first reject + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok() after path()->fail() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkAfterPathAndFail() {
        // Given: a meter configured with path() and stopped with fail()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.fail("error");

        // When: ok() is called
        meter.ok();

        // Then: state remains Failed with failPath - first termination wins
        assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject ok(path) after path()->fail() - logs INCONSISTENT_OK")
    @ValidateCleanMeter
    void shouldRejectOkWithPathAfterPathAndFail() {
        // Given: a meter configured with path() and stopped with fail()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.fail("error");

        // When: ok("path") is called
        meter.ok("path");

        // Then: state remains Failed - first termination wins
        assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_OK
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject reject() after path()->fail() - logs INCONSISTENT_REJECT")
    @ValidateCleanMeter
    void shouldRejectRejectAfterPathAndFail() {
        // Given: a meter configured with path() and stopped with fail()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.fail("error");

        // When: reject("business") is called
        meter.reject("business");

        // Then: state remains Failed - first termination wins
        assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_REJECT
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject fail() after path()->fail() - logs INCONSISTENT_FAIL")
    @ValidateCleanMeter
    void shouldRejectFailAfterPathAndFail() {
        // Given: a meter configured with path() and stopped with fail()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.fail("error");

        // When: fail("another") is called
        meter.fail("another");

        // Then: failPath remains "error" - first termination wins
        assertMeterState(meter, true, true, null, null, "error", null, 0, 0, 0);

        // Then: logs start + first fail + INCONSISTENT_FAIL
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEventCount(logger, 5);
    }

    // ============================================================================
    // Attempt to Restart After Termination
    // ============================================================================

    @Test
    @DisplayName("should reject start() after ok() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterOk() {
        // Given: a meter that has been stopped with ok()
        final Meter meter = new Meter(logger).start().ok();

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - state remains unchanged, second start is rejected early
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");

        // Then: logs start + ok + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after ok(path) - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterOkWithPath() {
        // Given: a meter that has been stopped with ok("path")
        final Meter meter = new Meter(logger).start().ok("path");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - okPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("path", meter.getOkPath(), "okPath should be preserved");

        // Then: logs start + ok + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after reject() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterReject() {
        // Given: a meter that has been stopped with reject("error")
        final Meter meter = new Meter(logger).start().reject("error");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - rejectPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("error", meter.getRejectPath(), "rejectPath should be preserved");

        // Then: logs start + reject + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after fail() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterFail() {
        // Given: a meter that has been stopped with fail("error")
        final Meter meter = new Meter(logger).start().fail("error");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - failPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("error", meter.getFailPath(), "failPath should be preserved");

        // Then: logs start + fail + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after path()->ok() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathAndOk() {
        // Given: a meter configured with path() and stopped with ok()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok();

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - okPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("configured", meter.getOkPath(), "okPath should be preserved");

        // Then: logs start + ok + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after path()->ok(path) - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathAndOkWithPath() {
        // Given: a meter configured with path() and stopped with ok("path")
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.ok("path");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - okPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("path", meter.getOkPath(), "okPath should be preserved");

        // Then: logs start + ok + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after path()->reject() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathAndReject() {
        // Given: a meter configured with path() and stopped with reject()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.reject("error");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - rejectPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("error", meter.getRejectPath(), "rejectPath should be preserved");

        // Then: logs start + reject + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }

    @Test
    @DisplayName("should reject start() after path()->fail() - logs INCONSISTENT_START")
    @ValidateCleanMeter
    void shouldRejectStartAfterPathAndFail() {
        // Given: a meter configured with path() and stopped with fail()
        final Meter meter = new Meter(logger).start();
        meter.path("configured");
        meter.fail("error");

        // When: start() is called again
        meter.start();

        // Then: Guard Clause prevents restart - failPath is preserved, state remains unchanged
        assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        assertTrue(meter.getStopTime() > 0, "stopTime should be > 0 (from first termination)");
        assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime (start is rejected, not executed)");
        assertEquals("error", meter.getFailPath(), "failPath should be preserved");

        // Then: logs start + fail + INCONSISTENT_START (no second start events due to Guard Clause)
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 4, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START);
        AssertLogger.assertEventCount(logger, 5);
    }
}
