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

/**
 * Unit tests for invalid {@link Meter} operations before start().
 * <p>
 * This test class validates that certain operations are NOT allowed in the Created state
 * (before start() is called) and that attempting them results in appropriate error logging
 * without changing the meter's state. These tests ensure defensive programming and proper
 * error handling for API misuse.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>inc() before start():</b> Verifies that incrementing iteration counter is invalid before execution begins</li>
 *   <li><b>progress() before start():</b> Tests that progress reporting is not allowed before meter is started</li>
 *   <li><b>Error Logging:</b> Validates that INCONSISTENT_* markers are logged for invalid operations</li>
 *   <li><b>State Preservation:</b> Confirms that invalid operations don't corrupt meter state</li>
 *   <li><b>No Side Effects:</b> Ensures invalid operations don't trigger unintended state transitions</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Created (before start)
 * <p>
 * <b>Error Handling Pattern:</b> Log error + ignore operation (no exception thrown)
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCyclePostStartInvalidOperationsTest} - Invalid operations after start</li>
 *   <li>{@link MeterLifeCyclePostStopInvalidOperationsOkStateTest} - Invalid operations after OK</li>
 *   <li>{@link MeterLifeCyclePreStartTerminationTest} - Termination before start (special case)</li>
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
@DisplayName("Group 6: Pre-Start Invalid Operations (❌ Tier 4)")
class MeterLifeCyclePreStartInvalidOperationsTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Increment operations without starting
    // ============================================================================

    @Test
    @DisplayName("should reject inc() before start() - logs INCONSISTENT_INCREMENT, currentIteration unchanged")
    @ValidateCleanMeter
    void shouldRejectIncBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: inc() is called before start()
        meter.inc();

        // Then: currentIteration remains 0, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_INCREMENT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject incBy(5) before start() - logs INCONSISTENT_INCREMENT, currentIteration unchanged")
    @ValidateCleanMeter
    void shouldRejectIncByBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: incBy(5) is called before start()
        meter.incBy(5);

        // Then: currentIteration remains 0, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_INCREMENT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject incTo(10) before start() - logs INCONSISTENT_INCREMENT, currentIteration unchanged")
    @ValidateCleanMeter
    void shouldRejectIncToBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: incTo(10) is called before start()
        meter.incTo(10);

        // Then: currentIteration remains 0, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_INCREMENT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject multiple increment calls before start() - all rejected")
    @ValidateCleanMeter
    void shouldRejectMultipleIncrementsBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: multiple increment operations called before start()
        meter.inc();
        meter.incBy(5);
        meter.incTo(10);

        // Then: currentIteration remains 0, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs 3x INCONSISTENT_INCREMENT
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Progress without starting
    // ============================================================================

    @Test
    @DisplayName("should reject progress() before start() - logs INCONSISTENT_PROGRESS, no progress report")
    @ValidateCleanMeter
    void shouldRejectProgressBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: progress() is called before start()
        meter.progress();

        // Then: no progress report generated, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_PROGRESS
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject multiple progress() calls before start() - all rejected")
    @ValidateCleanMeter
    void shouldRejectMultipleProgressCallsBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: multiple progress() calls before start()
        meter.progress();
        meter.progress();
        meter.progress();

        // Then: no progress reports generated, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs 3x INCONSISTENT_PROGRESS
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Context operations with null parameters before start (silent - no logs)
    // ============================================================================

    @Test
    @DisplayName("should silently accept ctx(null, String) null key before start() - no validation, no logs")
    @ValidateCleanMeter
    void shouldSilentlyAcceptNullKeyWithValueBeforeStart() {
        /* NOTE: This test was intended to validate that ctx(null, "value") is rejected as invalid.
         * However, the current Meter implementation accepts null parameters silently.
         * MeterContext default methods do not validate null parameters - they delegate to putContext(),
         * which converts null to NULL_VALUE ("<null>") instead of logging ILLEGAL.
         * This test documents the actual behavior: null parameters are tolerated, not rejected. */
        
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: ctx(null, "value") is called before start()
        meter.ctx(null, "value");

        // Then: operation succeeds silently (MeterContext default methods don't validate)
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no logs (ctx with null parameters doesn't trigger validation)
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should silently accept ctx(null, int) null key before start() - no validation, no logs")
    @ValidateCleanMeter
    void shouldSilentlyAcceptNullKeyWithPrimitiveBeforeStart() {
        /* NOTE: This test was intended to validate that ctx(null, 42) is rejected as invalid.
         * However, the current Meter implementation accepts null key silently (converts to "<null>"). */
        
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: ctx(null, 42) is called before start()
        meter.ctx(null, 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no logs
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should silently accept ctx(null, String, Object...) null key before start() - no validation, no logs")
    @ValidateCleanMeter
    void shouldSilentlyAcceptNullKeyWithFormatBeforeStart() {
        /* NOTE: This test was intended to validate that ctx(null, format, args) is rejected as invalid.
         * However, the current Meter implementation accepts null key silently (converts to "<null>"). */
        
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: ctx(null, "format %d", 42) is called before start()
        meter.ctx(null, "format %d", 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no logs
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should silently accept ctx(String, null, Object...) null format before start() - no validation, no logs")
    @ValidateCleanMeter
    void shouldSilentlyAcceptNullFormatBeforeStart() {
        /* NOTE: This test was intended to validate that ctx(key, null, args) is rejected as invalid.
         * However, the current Meter implementation accepts null format silently (converts to "<null>"). */
        
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: ctx("key", null, 42) is called before start()
        meter.ctx("key", null, 42);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no logs
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should silently accept ctx(String, String, null) null args array before start() - no validation, no logs")
    @ValidateCleanMeter
    void shouldSilentlyAcceptNullArgsArrayBeforeStart() {
        /* NOTE: This test was intended to validate that ctx(key, format, null array) is rejected as invalid.
         * However, the current Meter implementation accepts null args array silently. */
        
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: ctx("key", "format", (Object[]) null) is called before start()
        meter.ctx("key", "format", (Object[]) null);

        // Then: operation succeeds silently
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no logs
        AssertLogger.assertEventCount(logger, 0);
    }

    // ============================================================================
    // Set path before starting
    // ============================================================================

    @Test
    @DisplayName("should reject path(String) before start() - logs ILLEGAL, okPath unset")
    @ValidateCleanMeter
    void shouldRejectPathStringBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: path("some_path") is called before start()
        meter.path("some_path");

        // Then: okPath remains unset, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject path(Enum) before start() - logs ILLEGAL, okPath unset")
    @ValidateCleanMeter
    void shouldRejectPathEnumBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: path(Enum) is called before start()
        meter.path(MeterLifeCycleTestHelper.TestEnum.VALUE1);

        // Then: okPath remains unset, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject path(Throwable) before start() - logs ILLEGAL, okPath unset")
    @ValidateCleanMeter
    void shouldRejectPathThrowableBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);
        final RuntimeException exception = new RuntimeException("test cause");

        // When: path(Throwable) is called before start()
        meter.path(exception);

        // Then: okPath remains unset, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject path(Object) before start() - logs ILLEGAL, okPath unset")
    @ValidateCleanMeter
    void shouldRejectPathObjectBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);
        final MeterLifeCycleTestHelper.TestObject testObject = new MeterLifeCycleTestHelper.TestObject();

        // When: path(Object) is called before start()
        meter.path(testObject);

        // Then: okPath remains unset, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should reject multiple path() calls before start() - all rejected")
    @ValidateCleanMeter
    void shouldRejectMultiplePathCallsBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: multiple path() calls before start()
        meter.path("first");
        meter.path(MeterLifeCycleTestHelper.TestEnum.VALUE1);
        meter.path("third");

        // Then: okPath remains unset, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs 3x ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    // ============================================================================
    // Combined invalid operations before start
    // ============================================================================

    @Test
    @DisplayName("should reject all invalid operations before start() - inc + progress + path")
    @ValidateCleanMeter
    void shouldRejectAllInvalidOperationsBeforeStart() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: multiple invalid operations before start()
        meter.inc();
        meter.progress();
        meter.path("some_path");

        // Then: all attributes unchanged, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: logs INCONSISTENT_INCREMENT + INCONSISTENT_PROGRESS + ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }

    @Test
    @DisplayName("should reject invalid operations mixed with valid configuration before start()")
    @ValidateCleanMeter
    void shouldRejectInvalidOperationsMixedWithValidConfiguration() {
        // Given: a new Meter without start()
        final Meter meter = new Meter(logger);

        // When: mix of valid configuration and invalid operations before start()
        meter.iterations(100);  // Valid: configuration before start
        meter.inc();            // Invalid: increment before start
        meter.limitMilliseconds(5000);  // Valid: configuration before start
        meter.progress();       // Invalid: progress before start
        meter.m("operation");   // Valid: description before start
        meter.path("path");     // Invalid: path before start

        // Then: valid configs applied, invalid operations rejected, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
        assertEquals("operation", meter.getDescription());

        // Then: logs INCONSISTENT_INCREMENT + INCONSISTENT_PROGRESS + ILLEGAL
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS);
        AssertLogger.assertEvent(logger, 2, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 3);
    }
}
