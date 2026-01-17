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

/**
 * Unit tests for {@link Meter} try-with-resources lifecycle patterns.
 * <p>
 * This test class validates the special behavior of Meter when used in try-with-resources
 * blocks. The Meter implements {@link AutoCloseable} and has specific semantics for the
 * {@code close()} method depending on whether the meter has been explicitly terminated or not.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>Auto-Fail on Close:</b> Tests that close() automatically calls fail() if meter was started but not terminated</li>
 *   <li><b>Explicit Success:</b> Validates that ok() before close() prevents auto-fail</li>
 *   <li><b>Explicit Rejection:</b> Tests that reject() before close() prevents auto-fail</li>
 *   <li><b>Explicit Failure:</b> Tests that fail() before close() makes close() a no-op</li>
 *   <li><b>Not Started:</b> Validates close() behavior when meter was never started</li>
 *   <li><b>Chained Creation:</b> Tests new Meter(logger).start() pattern in try-with-resources</li>
 *   <li><b>Exception Propagation:</b> Validates meter behavior when exception occurs in try block</li>
 *   <li><b>Fail Path:</b> Tests that auto-fail uses "try-with-resources" as the fail path</li>
 * </ul>
 * <p>
 * <b>close() Behavior:</b>
 * <ul>
 *   <li>If Started + Not Terminated → calls fail("try-with-resources")</li>
 *   <li>If Already Terminated → no-op (logs INCONSISTENT_CLOSE if called explicitly)</li>
 *   <li>If Not Started → no-op</li>
 * </ul>
 * <p>
 * <b>Use Case:</b> Try-with-resources ensures that long-running operations are always
 * terminated, even if an exception occurs. If the developer forgets to call ok/reject/fail,
 * the meter automatically fails with a distinctive path for diagnostic purposes.
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Normal termination patterns</li>
 *   <li>{@link MeterLifeCycleInitializationTest} - Try-with-resources construction patterns</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidTerminationTest} - Behavior after explicit termination</li>
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
@DisplayName("Group 3: Try-With-Resources (Tier 1 + Tier 3)")
class MeterLifeCycleTryWithResourcesTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    @Test
    @DisplayName("should follow try-with-resources flow (implicit failure)")
    void shouldFollowTryWithResourcesFlowImplicitFailure() {
        final Meter meter;
        // Given: a new, started Meter withing try with resources
        try (final Meter m = new Meter(logger).start()) {
            meter = m;
            // do nothing
        }

        // Then: it should be automatically failed on close()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "try-with-resources", null, 0, 0, 0);

        // Then: all log messages recorded correctly
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should follow try-with-resources flow (explicit success)")
    void shouldFollowTryWithResourcesFlowExplicitSuccess() {
        final Meter meter;
        // Given: a new, started Meter withing try with resources
        try (final Meter m = new Meter(logger).start()) {
            meter = m;

            // When: ok() is called
            m.ok();

            // Then: Meter is in stopped state
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        }

        // Then: it should remain in success state after close()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        // Then: all log messages recorded correctly
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should follow try-with-resources flow (path and explicit success)")
    void shouldFollowTryWithResourcesFlowPathAndExplicitSuccess() {
        final Meter meter;
        // Given: a new, started Meter withing try with resources
        try (final Meter m = new Meter(logger).start()) {
            meter = m;

            // When: path() is called
            m.path("customPath");

            // Then: path is set
            MeterLifeCycleTestHelper.assertMeterState(meter, true, false, "customPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            m.ok();

            // Then: Meter is in stopped state with path preserved
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);
        }

        // Then: it should remain in success state after close()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);

        // Then: all log messages recorded correctly
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should follow try-with-resources flow (explicit rejection)")
    void shouldFollowTryWithResourcesFlowExplicitRejection() {
        final Meter meter;
        // Given: a new, started Meter withing try with resources
        try (final Meter m = new Meter(logger).start()) {
            meter = m;

            // When: reject() is called
            m.reject("rejected");

            // Then: Meter is in stopped state with reject path set
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);
        }

        // Then: it should remain in rejection state after close()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);

        // Then: all log messages recorded correctly
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 4);
    }

    @Test
    @DisplayName("should follow try-with-resources flow (explicit failure)")
    void shouldFollowTryWithResourcesFlowExplicitFailure() {
        final Meter meter;
        // Given: a new, started Meter withing try with resources
        try (final Meter m = new Meter(logger).start()) {
            meter = m;

            // When: fail() is called
            m.fail("failed");

            // Then: Meter is in stopped state with failure message
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);
        }

        // Then: it should remain in failure state after close()
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);

        // Then: all log messages recorded correctly
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 3, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 4);
    }

    // ============================================================================
    // Try-with-resources WITHOUT start() (Tier 3 - State-Correcting)
    // ============================================================================

    @Test
    @DisplayName("should transition to Failed via try-with-resources without start() - implicit close()")
    void shouldTransitionToFailedViaTryWithResourcesWithoutStartImplicitClose() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: block executes without calling start(), ok(), reject(), or fail()
             * (meter auto-closes with implicit fail) */
        }

        /* Then: meter is in Failed state after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "try-with-resources", null, 0, 0, 0);

        /* Then: logs INCONSISTENT_CLOSE + ERROR for implicit failure */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_CLOSE);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, "try-with-resources");
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK via try-with-resources without start() - explicit ok()")
    void shouldTransitionToOkViaTryWithResourcesWithoutStartExplicitOk() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: ok() is called without start() */
            m.ok();

            /* Then: Meter is in stopped state (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
        }

        /* Then: meter remains in OK state after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with path via try-with-resources without start() - explicit ok(String)")
    void shouldTransitionToOkWithPathViaTryWithResourcesWithoutStartExplicitOkString() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: ok("success_path") is called without start() */
            m.ok("success_path");

            /* Then: Meter is in stopped state with path (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);
        }

        /* Then: meter remains in OK state with path after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "success_path", null, null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Enum via try-with-resources without start() - explicit ok(Enum)")
    void shouldTransitionToOkWithEnumViaTryWithResourcesWithoutStartExplicitOkEnum() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: ok(Enum) is called without start() */
            m.ok(MeterLifeCycleTestHelper.TestEnum.VALUE1);

            /* Then: Meter is in stopped state with enum path (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);
        }

        /* Then: meter remains in OK state with enum path after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "VALUE1", null, null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Throwable via try-with-resources without start() - explicit ok(Throwable)")
    void shouldTransitionToOkWithThrowableViaTryWithResourcesWithoutStartExplicitOkThrowable() {
        Meter meter = null;
        final RuntimeException exception = new RuntimeException("test cause");
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: ok(Throwable) is called without start() */
            m.ok(exception);

            /* Then: Meter is in stopped state with throwable path (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);
        }

        /* Then: meter remains in OK state with throwable path after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "RuntimeException", null, null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to OK with Object via try-with-resources without start() - explicit ok(Object)")
    void shouldTransitionToOkWithObjectViaTryWithResourcesWithoutStartExplicitOkObject() {
        Meter meter = null;
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: ok(Object) is called without start() */
            m.ok(testObject);

            /* Then: Meter is in stopped state with object path (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);
        }

        /* Then: meter remains in OK state with object path after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, "testObjectString", null, null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_OK + INFO completion report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_OK);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_OK);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected via try-with-resources without start() - explicit reject(String)")
    void shouldTransitionToRejectedViaTryWithResourcesWithoutStartExplicitRejectString() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: reject("business_error") is called without start() */
            m.reject("business_error");

            /* Then: Meter is in stopped state with rejectPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);
        }

        /* Then: meter remains in Rejected state after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "business_error", null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Enum via try-with-resources without start() - explicit reject(Enum)")
    void shouldTransitionToRejectedWithEnumViaTryWithResourcesWithoutStartExplicitRejectEnum() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: reject(Enum) is called without start() */
            m.reject(MeterLifeCycleTestHelper.TestEnum.VALUE2);

            /* Then: Meter is in stopped state with enum rejectPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);
        }

        /* Then: meter remains in Rejected state with enum cause after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "VALUE2", null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Throwable via try-with-resources without start() - explicit reject(Throwable)")
    void shouldTransitionToRejectedWithThrowableViaTryWithResourcesWithoutStartExplicitRejectThrowable() {
        Meter meter = null;
        final IllegalArgumentException exception = new IllegalArgumentException("invalid input");
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: reject(Throwable) is called without start() */
            m.reject(exception);

            /* Then: Meter is in stopped state with throwable rejectPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);
        }

        /* Then: meter remains in Rejected state with throwable cause after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "IllegalArgumentException", null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Rejected with Object via try-with-resources without start() - explicit reject(Object)")
    void shouldTransitionToRejectedWithObjectViaTryWithResourcesWithoutStartExplicitRejectObject() {
        Meter meter = null;
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: reject(Object) is called without start() */
            m.reject(testObject);

            /* Then: Meter is in stopped state with object rejectPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);
        }

        /* Then: meter remains in Rejected state with object cause after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, "testObjectString", null, null, 0, 0, 0);

        /* Then: logs INCONSISTENT_REJECT + INFO rejection report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_REJECT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO, Markers.MSG_REJECT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_REJECT);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed via try-with-resources without start() - explicit fail(String)")
    void shouldTransitionToFailedViaTryWithResourcesWithoutStartExplicitFailString() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: fail("technical_error") is called without start() */
            m.fail("technical_error");

            /* Then: Meter is in stopped state with failPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);
        }

        /* Then: meter remains in Failed state after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "technical_error", null, 0, 0, 0);

        /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Enum via try-with-resources without start() - explicit fail(Enum)")
    void shouldTransitionToFailedWithEnumViaTryWithResourcesWithoutStartExplicitFailEnum() {
        Meter meter = null;
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: fail(Enum) is called without start() */
            m.fail(MeterLifeCycleTestHelper.TestEnum.VALUE1);

            /* Then: Meter is in stopped state with enum failPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);
        }

        /* Then: meter remains in Failed state with enum cause after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "VALUE1", null, 0, 0, 0);

        /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Throwable via try-with-resources without start() - explicit fail(Throwable)")
    void shouldTransitionToFailedWithThrowableViaTryWithResourcesWithoutStartExplicitFailThrowable() {
        Meter meter = null;
        final Exception exception = new Exception("connection timeout");
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: fail(Throwable) is called without start() */
            m.fail(exception);

            /* Then: Meter is in stopped state with throwable failPath and failMessage (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "java.lang.Exception", "connection timeout", 0, 0, 0);
        }

        /* Then: meter remains in Failed state with throwable details after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "java.lang.Exception", "connection timeout", 0, 0, 0);

        /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should transition to Failed with Object via try-with-resources without start() - explicit fail(Object)")
    void shouldTransitionToFailedWithObjectViaTryWithResourcesWithoutStartExplicitFailObject() {
        Meter meter = null;
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();
        /* Given: Meter created in try-with-resources without start() */
        try (final Meter m = new Meter(logger)) {
            meter = m;
            /* When: fail(Object) is called without start() */
            m.fail(testObject);

            /* Then: Meter is in stopped state with object failPath (pedagogical validation) */
            MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);
        }

        /* Then: meter remains in Failed state with object cause after close() */
        MeterLifeCycleTestHelper.assertMeterState(meter, true, true, null, null, "testObjectString", null, 0, 0, 0);

        /* Then: logs INCONSISTENT_FAIL + ERROR failure report, close() does nothing */
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FAIL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.MSG_FAIL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.TRACE, Markers.DATA_FAIL);
        AssertLogger.assertEventCount(logger, 3);
    }
}
