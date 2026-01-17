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
 * Unit tests for {@link Meter} configuration before start().
 * <p>
 * This test class validates that Meter configuration methods (path, iterations, timeLimit)
 * can be called in the Created state (before start()) and that these configurations are
 * properly applied when the Meter transitions to Started state and eventually terminates.
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 *   <li><b>path() before start():</b> Tests setting expected ok/reject/fail paths before starting the meter</li>
 *   <li><b>iterations() before start():</b> Validates setting expectedIterations before execution</li>
 *   <li><b>timeLimit() before start():</b> Tests configuring time limits for slowness detection</li>
 *   <li><b>Chained Configuration:</b> Tests combining multiple configuration calls via fluent API</li>
 *   <li><b>State Preservation:</b> Verifies that pre-start configurations persist after start() and termination</li>
 *   <li><b>Configuration → Execution:</b> Validates that pre-configured values affect runtime behavior</li>
 * </ul>
 * <p>
 * <b>State Tested:</b> Created (configuration phase)
 * <p>
 * <b>Lifecycle Pattern:</b> Created → [configure] → Started → [execute] → Terminated
 * <p>
 * <b>Related Tests:</b>
 * <ul>
 *   <li>{@link MeterLifeCycleInitializationTest} - Initial state validation</li>
 *   <li>{@link MeterLifeCyclePostStartConfigurationTest} - Configuration after start</li>
 *   <li>{@link MeterLifeCycleHappyPathTest} - Normal execution flow</li>
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
@DisplayName("Group 4: Pre-Start Attribute Updates (Tier 2)")
class MeterLifeCyclePreStartConfigurationTest {

    @SuppressWarnings("NonConstantLogger")
    @Slf4jMock
    Logger logger;

    // ============================================================================
    // Set time limit
    // ============================================================================

    @Test
    @DisplayName("should set time limit before start()")
    void shouldSetTimeLimitBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: limitMilliseconds(5000) is called before start()
        meter.limitMilliseconds(5000);

        // Then: timeLimit attribute is stored correctly and meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should override time limit when set multiple times")
    void shouldOverrideTimeLimitWhenSetMultipleTimes() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: limitMilliseconds() is called twice
        meter.limitMilliseconds(100);
        meter.limitMilliseconds(5000);

        // Then: last value wins and meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should preserve valid time limit when invalid value attempted")
    void shouldPreserveValidTimeLimitWhenInvalidValueAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: limitMilliseconds(5000) is called, then limitMilliseconds(0) is attempted
        meter.limitMilliseconds(5000);
        meter.limitMilliseconds(0);

        // Then: first valid value is preserved, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should log ILLEGAL when negative time limit attempted")
    void shouldLogIllegalWhenNegativeTimeLimitAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: limitMilliseconds(-1) is called
        meter.limitMilliseconds(-1);

        // Then: meter remains in Created state with no time limit set
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    // ============================================================================
    // Set expected iterations
    // ============================================================================

    @Test
    @DisplayName("should set expected iterations before start()")
    void shouldSetExpectedIterationsBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: iterations(100) is called before start()
        meter.iterations(100);

        // Then: expectedIterations attribute is stored correctly and meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should override expected iterations when set multiple times")
    void shouldOverrideExpectedIterationsWhenSetMultipleTimes() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: iterations() is called twice
        meter.iterations(50);
        meter.iterations(100);

        // Then: last value wins and meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should preserve valid iterations when invalid value attempted")
    void shouldPreserveValidIterationsWhenInvalidValueAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: iterations(100) is called, then iterations(0) is attempted
        meter.iterations(100);
        meter.iterations(0);

        // Then: first valid value is preserved, meter remains in Created state
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should log ILLEGAL when negative iterations attempted")
    void shouldLogIllegalWhenNegativeIterationsAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: iterations(-5) is called
        meter.iterations(-5);

        // Then: meter remains in Created state with no iterations set
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    // ============================================================================
    // Add descriptive message
    // ============================================================================

    @Test
    @DisplayName("should add descriptive message before start()")
    void shouldAddDescriptiveMessageBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m(String) is called before start()
        meter.m("starting operation");

        // Then: description attribute is stored correctly and meter remains in Created state
        assertEquals("starting operation", meter.getDescription(), "should store description correctly");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should override description when m() is called multiple times")
    void shouldOverrideDescriptionWhenMCalledMultipleTimes() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m() is called multiple times
        meter.m("step 1");
        meter.m("step 2");

        // Then: last value wins and meter remains in Created state
        assertEquals("step 2", meter.getDescription(), "should override with last value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should preserve valid message when null value attempted")
    void shouldPreserveValidMessageWhenNullValueAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m("step 1") is called, then m(null) is attempted
        meter.m("step 1");
        meter.m(null);

        // Then: first valid value is preserved, meter remains in Created state
        assertEquals("step 1", meter.getDescription(), "should preserve first valid value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should log ILLEGAL when null message attempted before any valid message")
    void shouldLogIllegalWhenNullMessageAttemptedBeforeAnyValidMessage() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m(null) is called without setting a previous message
        meter.m(null);

        // Then: description remains null, meter remains in Created state
        assertNull(meter.getDescription(), "should remain null when null attempted");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    // ============================================================================
    // Add formatted descriptive message
    // ============================================================================

    @Test
    @DisplayName("should add formatted descriptive message before start()")
    void shouldAddFormattedDescriptiveMessageBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m(format, args) is called before start()
        meter.m("operation %s", "doWork");

        // Then: description attribute is formatted and stored correctly and meter remains in Created state
        assertEquals("operation doWork", meter.getDescription(), "should format description correctly");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should override formatted message when m() is called multiple times")
    void shouldOverrideFormattedMessageWhenMCalledMultipleTimes() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m(format, args) is called multiple times
        meter.m("step %d", 1);
        meter.m("step %d", 2);

        // Then: last value wins and meter remains in Created state
        assertEquals("step 2", meter.getDescription(), "should override with last formatted value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should preserve valid formatted message when null format attempted")
    void shouldPreserveValidFormattedMessageWhenNullFormatAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m("valid: %s", "arg") is called, then m(null, "arg") is attempted
        meter.m("valid: %s", "arg");
        meter.m(null, "arg");

        // Then: null format is rejected with ILLEGAL log, description is reset to null, meter remains in Created state
        assertNull(meter.getDescription(), "should reset description to null when null format attempted");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    @Test
    @DisplayName("should log ILLEGAL when invalid format string attempted")
    void shouldLogIllegalWhenInvalidFormatStringAttempted() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: m("invalid format %z", "arg") is called (invalid format specifier)
        meter.m("invalid format %z", "arg");

        // Then: meter remains in Created state with no description set
        assertNull(meter.getDescription(), "should remain null when invalid format attempted");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: ILLEGAL event logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 1);
    }

    // ============================================================================
    // Add context key-value pairs
    // ============================================================================

    @Test
    @DisplayName("should add context key-value pair before start()")
    void shouldAddContextKeyValuePairBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ctx("key1", "value1") is called before start()
        meter.ctx("key1", "value1");

        // Then: context contains the key-value pair and meter remains in Created state
        assertEquals("value1", meter.getContext().get("key1"), "should store context value correctly");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should override context value when same key set multiple times")
    void shouldOverrideContextValueWhenSameKeySetMultipleTimes() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ctx() is called twice with the same key
        meter.ctx("key", "val1");
        meter.ctx("key", "val2");

        // Then: last value wins and meter remains in Created state
        assertEquals("val2", meter.getContext().get("key"), "should override context with last value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should replace context value when null value set")
    void shouldReplaceContextValueWhenNullValueSet() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ctx("key", "valid") is called, then ctx("key", null) is called
        meter.ctx("key", "valid");
        meter.ctx("key", (String) null);

        // Then: null value is stored as "<null>" placeholder (context stores null as string literal)
        assertEquals("<null>", meter.getContext().get("key"), "should store null as <null> placeholder");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should store multiple different context key-value pairs")
    void shouldStoreMultipleDifferentContextKeyValuePairs() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: ctx() is called multiple times with different keys
        meter.ctx("key1", "value1");
        meter.ctx("key2", "value2");
        meter.ctx("key3", "value3");

        // Then: all context key-value pairs are stored and meter remains in Created state
        assertEquals("value1", meter.getContext().get("key1"), "should store key1 context value");
        assertEquals("value2", meter.getContext().get("key2"), "should store key2 context value");
        assertEquals("value3", meter.getContext().get("key3"), "should store key3 context value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    // ============================================================================
    // Chain multiple configurations
    // ============================================================================

    @Test
    @DisplayName("should chain multiple valid configurations before start()")
    void shouldChainMultipleValidConfigurationsBeforeStart() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: multiple configuration methods are chained
        meter
                .iterations(100)
                .limitMilliseconds(5000)
                .m("starting operation");

        // Then: all attributes are set correctly and meter remains in Created state
        assertEquals("starting operation", meter.getDescription(), "should store description from chain");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should handle chained configuration with last m() value winning")
    void shouldHandleChainedConfigurationWithLastMValueWinning() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: multiple configuration methods are chained with m() called multiple times
        meter
                .m("op1")
                .limitMilliseconds(5000)
                .iterations(100)
                .m("op2");

        // Then: m() last value wins, iterations and limit preserved
        assertEquals("op2", meter.getDescription(), "should override description with last m() value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }

    @Test
    @DisplayName("should ignore invalid values in chained configuration")
    void shouldIgnoreInvalidValuesInChainedConfiguration() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: chained configuration includes invalid values after valid values
        meter
                .limitMilliseconds(5000)
                .iterations(100)
                .limitMilliseconds(0)     // Invalid: 0
                .iterations(-1);           // Invalid: -1

        // Then: all valid values preserved, invalid attempts logged
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);

        // Then: two ILLEGAL events logged
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL);
        AssertLogger.assertEventCount(logger, 2);
    }

    @Test
    @DisplayName("should chain configuration with context operations")
    void shouldChainConfigurationWithContextOperations() {
        // Given: a new Meter
        final Meter meter = new Meter(logger);

        // When: configuration is chained with context operations
        meter
                .iterations(100)
                .limitMilliseconds(5000)
                .m("starting operation")
                .ctx("user", "testUser")
                .ctx("session", "test-session-123");

        // Then: all attributes are set correctly and meter remains in Created state
        assertEquals("starting operation", meter.getDescription(), "should store description from chain");
        assertEquals("testUser", meter.getContext().get("user"), "should store user context value");
        assertEquals("test-session-123", meter.getContext().get("session"), "should store session context value");
        MeterLifeCycleTestHelper.assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);

        // Then: no log events
        AssertLogger.assertEventCount(logger, 0);
    }
}
