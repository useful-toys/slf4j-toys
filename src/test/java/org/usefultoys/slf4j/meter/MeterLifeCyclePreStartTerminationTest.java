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
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordCreateWithWindow;
import static org.usefultoys.slf4j.meter.MeterLifeCycleTestHelper.recordStopWithWindow;

/**
 * Unit tests for {@link Meter} termination before start().
 * <p>
 * This test class validates the special case where termination methods (ok, reject, fail)
 * are called in the Created state (before start()). This is an invalid operation pattern
 * that results in error logging and immediate termination without proper lifecycle execution.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>ok() before start():</b> Tests direct success termination without execution</li>
 *   <li><b>reject() before start():</b> Tests rejection before meter execution begins</li>
 *   <li><b>fail() before start():</b> Tests failure termination in Created state</li>
 *   <li><b>Error Logging:</b> Validates INCONSISTENT_* markers are logged for pre-start termination</li>
 *   <li><b>State Transition:</b> Confirms meter transitions directly from Created to Terminated (skipping Started)</li>
 *   <li><b>Timestamps:</b> Verifies startTime remains 0 but stopTime is set</li>
 *   <li><b>Path Preservation:</b> Tests that termination paths are correctly recorded</li>
 * </ul>
 * <p>
 * <b>State Transitions Tested:</b>
 * <ul>
 *   <li>Created → OK (invalid, but handled gracefully)</li>
 *   <li>Created → Rejected (invalid, but handled gracefully)</li>
 *   <li>Created → Failed (invalid, but handled gracefully)</li>
 * </ul>
 * <p>
 * <b>Design Note:</b> These scenarios represent API misuse but are handled defensively
 * to prevent application crashes. The meter logs errors and transitions to terminal state.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Normal termination after start</li>
 *   <li>{@link MeterLifeCyclePreStartInvalidOperationsTest} - Other pre-start invalid operations</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Termination after already stopped</li>
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
@DisplayName("Group 5: Pre-Start Termination (Tier 3)")
class MeterLifeCyclePreStartTerminationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // OK without starting (Created → OK)
    // ============================================================================

    @Test
    @DisplayName("should transition to OK when ok() called without start()")
    void shouldTransitionToOkWhenOkCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, ()->new Meter(logger));

        // When: ok() is called without start()
        recordStopWithWindow(tr, ()->meter.ok());

        // Then: meter transitions to OK state despite missing start()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with String path when ok(String) called without start()")
    void shouldTransitionToOkWithStringPathWhenOkStringCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: ok("success_path") is called without start()
        recordStopWithWindow(tr, () -> meter.ok("success_path"));

        // Then: meter transitions to OK state with path
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Enum path when ok(Enum) called without start()")
    void shouldTransitionToOkWithEnumPathWhenOkEnumCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: ok(Enum) is called without start()
        recordStopWithWindow(tr, () -> meter.ok(MeterLifeCycleTestHelper.TestEnum.VALUE1));

        // Then: meter transitions to OK state with enum toString as path
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Throwable path when ok(Throwable) called without start()")
    void shouldTransitionToOkWithThrowablePathWhenOkThrowableCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final RuntimeException exception = new RuntimeException("test cause");

        // When: ok(Throwable) is called without start()
        recordStopWithWindow(tr, () -> meter.ok(exception));

        // Then: meter transitions to OK state with throwable simple class name as path
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Object path when ok(Object) called without start()")
    void shouldTransitionToOkWithObjectPathWhenOkObjectCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: ok(Object) is called without start()
        recordStopWithWindow(tr, () -> meter.ok(testObject));

        // Then: meter transitions to OK state with object toString as path
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Reject without starting (Created → Rejected)
    // ============================================================================

    @Test
    @DisplayName("should transition to Rejected when reject(String) called without start()")
    void shouldTransitionToRejectedWhenRejectStringCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: reject("business_error") is called without start()
        recordStopWithWindow(tr, () -> meter.reject("business_error"));

        // Then: meter transitions to Rejected state despite missing start()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Enum cause when reject(Enum) called without start()")
    void shouldTransitionToRejectedWithEnumCauseWhenRejectEnumCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: reject(Enum) is called without start()
        recordStopWithWindow(tr, () -> meter.reject(MeterLifeCycleTestHelper.TestEnum.VALUE2));

        // Then: meter transitions to Rejected state with enum toString as cause
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Throwable cause when reject(Throwable) called without start()")
    void shouldTransitionToRejectedWithThrowableCauseWhenRejectThrowableCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final IllegalArgumentException exception = new IllegalArgumentException("invalid input");

        // When: reject(Throwable) is called without start()
        recordStopWithWindow(tr, () -> meter.reject(exception));

        // Then: meter transitions to Rejected state with throwable simple class name as cause
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Object cause when reject(Object) called without start()")
    void shouldTransitionToRejectedWithObjectCauseWhenRejectObjectCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: reject(Object) is called without start()
        recordStopWithWindow(tr, () -> meter.reject(testObject));

        // Then: meter transitions to Rejected state with object toString as cause
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Fail without starting (Created → Failed)
    // ============================================================================

    @Test
    @DisplayName("should transition to Failed when fail(String) called without start()")
    void shouldTransitionToFailedWhenFailStringCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: fail("technical_error") is called without start()
        recordStopWithWindow(tr, () -> meter.fail("technical_error"));

        // Then: meter transitions to Failed state (failMessage null for non-Throwable)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Enum cause when fail(Enum) called without start()")
    void shouldTransitionToFailedWithEnumCauseWhenFailEnumCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: fail(Enum) is called without start()
        recordStopWithWindow(tr, () -> meter.fail(MeterLifeCycleTestHelper.TestEnum.VALUE1));

        // Then: meter transitions to Failed state (failMessage null for non-Throwable)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Throwable cause when fail(Throwable) called without start()")
    void shouldTransitionToFailedWithThrowableCauseWhenFailThrowableCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final Exception exception = new Exception("connection timeout");

        // When: fail(Throwable) is called without start()
        recordStopWithWindow(tr, () -> meter.fail(exception));

        // Then: meter transitions to Failed state with throwable full class name as path and message as failMessage
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "java.lang.Exception", "connection timeout", 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Object cause when fail(Object) called without start()")
    void shouldTransitionToFailedWithObjectCauseWhenFailObjectCalledWithoutStart() {
        // Given: a new Meter without start()
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: fail(Object) is called without start()
        recordStopWithWindow(tr, () -> meter.fail(testObject));

        // Then: meter transitions to Failed state (failMessage null for non-Throwable)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Pre-configured attributes preserved on self-correcting termination
    // ============================================================================

    @Test
    @DisplayName("should preserve pre-configured attributes when ok() called without start()")
    void shouldPreservePreConfiguredAttributesWhenOkCalledWithoutStart() {
        // Given: a new Meter with pre-configured attributes
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.iterations(100);
        meter.limitMilliseconds(5000);
        meter.m("operation description");

        // When: ok() is called without start()
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: all pre-configured attributes are preserved in terminal state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 100, 5000);
        assertEquals("operation description", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK (with attributes)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "operation description");
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve pre-configured attributes when reject() called without start()")
    void shouldPreservePreConfiguredAttributesWhenRejectCalledWithoutStart() {
        // Given: a new Meter with pre-configured attributes
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.iterations(50);
        meter.limitMilliseconds(3000);
        meter.m("validation check");

        // When: reject() is called without start()
        recordStopWithWindow(tr, () -> meter.reject("validation_error"));

        // Then: all pre-configured attributes are preserved in terminal state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 50, 3000);
        assertEquals("validation check", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT (with attributes)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "validation check");
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve pre-configured attributes when fail() called without start()")
    void shouldPreservePreConfiguredAttributesWhenFailCalledWithoutStart() {
        // Given: a new Meter with pre-configured attributes
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.iterations(200);
        meter.limitMilliseconds(10000);
        meter.m("database operation");

        // When: fail() is called without start()
        recordStopWithWindow(tr, () -> meter.fail("connection_error"));

        // Then: all pre-configured attributes are preserved (failMessage null for String)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "connection_error", null, 0, 200, 10000);
        assertEquals("database operation", meter.getDescription());
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL (with attributes)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "database operation");
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Context preserved on self-correcting termination
    // ============================================================================

    @Test
    @DisplayName("should preserve context when ok() called without start()")
    void shouldPreserveContextWhenOkCalledWithoutStart() {
        // Given: a new Meter with context
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.ctx("user", "alice");
        meter.ctx("action", "import");

        // When: ok() is called without start()
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: context is preserved in terminal state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_OK + MSG_OK + DATA_OK (with context)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "user", "alice");
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "action", "import");
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve context when reject() called without start()")
    void shouldPreserveContextWhenRejectCalledWithoutStart() {
        // Given: a new Meter with context
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.ctx("user", "bob");
        meter.ctx("action", "export");

        // When: reject() is called without start()
        recordStopWithWindow(tr, () -> meter.reject("validation_error"));

        // Then: context is preserved in terminal state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "validation_error", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT (with context)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "user", "bob");
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "action", "export");
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should preserve context when fail() called without start()")
    void shouldPreserveContextWhenFailCalledWithoutStart() {
        // Given: a new Meter with context
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));
        meter.ctx("user", "charlie");
        meter.ctx("action", "delete");

        // When: fail() is called without start()
        recordStopWithWindow(tr, () -> meter.fail("permission_error"));

        // Then: context is preserved (failMessage null for String)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "permission_error", null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL (with context)
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "user", "charlie");
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "action", "delete");
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Path set before starting (rejected, then termination)
    // ============================================================================

    @Test
    @DisplayName("should reject path() before start() then transition to OK")
    void shouldRejectPathBeforeStartThenTransitionToOk() {
        // Given: a new Meter
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: path() is called before start(), then ok() is called
        meter.path("custom_path");
        recordStopWithWindow(tr, () -> meter.ok());

        // Then: path() was rejected (ILLEGAL), okPath remains undefined after ok()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs ILLEGAL (path before start) + INCONSISTENT_OK + MSG_OK + DATA_OK
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should reject path() before start() then transition to Rejected")
    void shouldRejectPathBeforeStartThenTransitionToRejected() {
        // Given: a new Meter
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: path() is called before start(), then reject() is called
        meter.path("custom_path");
        recordStopWithWindow(tr, () -> meter.reject("business_error"));

        // Then: path() was rejected (ILLEGAL), meter still reaches Rejected state
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs ILLEGAL (path before start) + INCONSISTENT_REJECT + MSG_REJECT + DATA_REJECT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should reject path() before start() then transition to Failed")
    void shouldRejectPathBeforeStartThenTransitionToFailed() {
        // Given: a new Meter
        final TimeRecord tr = new TimeRecord();
        final Meter meter = recordCreateWithWindow(tr, () -> new Meter(logger));

        // When: path() is called before start(), then fail() is called
        meter.path("custom_path");
        recordStopWithWindow(tr, () -> meter.fail("technical_error"));

        // Then: path() was rejected (ILLEGAL), meter reaches Failed (failMessage null for String)
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        MeterLifeCycleTestHelper.assertMeterNotStartedStopTime(meter, tr);

        // Then: logs ILLEGAL (path before start) + INCONSISTENT_FAIL + MSG_FAIL + DATA_FAIL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }
}
