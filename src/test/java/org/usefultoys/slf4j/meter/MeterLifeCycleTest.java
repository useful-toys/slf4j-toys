/*
 * Copyright 2026 Daniel Felix Ferber
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;
import org.usefultoys.test.WithLocale;

/**
 * Unit tests for {@link Meter} lifecycle.
 * <p>
 * Tests validate that Meter correctly transitions between states and updates its attributes
 * according to the lifecycle defined in the state diagram.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Normal Success Flow:</b> Verifies attributes and logs for start() and ok()</li>
 *   <li><b>Success with Path:</b> Verifies attributes and logs when a path is provided to ok()</li>
 *   <li><b>Rejection Flow:</b> Verifies attributes and logs for reject()</li>
 *   <li><b>Failure Flow:</b> Verifies attributes and logs for fail()</li>
 *   <li><b>Try-With-Resources:</b> Verifies automatic failure on close()</li>
 *   <li><b>Iteration Tracking:</b> Verifies currentIteration and expectedIterations</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 * @author Co-authored-by: GitHub Copilot using GPT-5.2
 */
@ValidateCharset
@ResetMeterConfig
@WithLocale("en")
@WithMockLogger
@ValidateCleanMeter
@WithMockLoggerDebug
class MeterLifeCycleTest {

    @Slf4jMock
    private Logger logger;

    enum TestEnum {
        VALUE1, VALUE2
    }

    static class TestObject {
        @Override
        public String toString() {
            return "testObjectString";
        }
    }

    private void assertMeterState(Meter meter, boolean started, boolean stopped, String okPath, String rejectPath, String failPath, String failMessage, long currentIteration, long expectedIterations, long timeLimitMilliseconds) {
        if (started) {
            assertTrue(meter.getStartTime() > 0, "startTime should be > 0");
        } else {
            assertEquals(0, meter.getStartTime(), "startTime should be 0");
        }

        if (stopped) {
            assertTrue(meter.getStopTime() > 0, "stopTime should be > 0");
            assertTrue(meter.getStopTime() >= meter.getStartTime(), "stopTime should be >= startTime");
        } else {
            assertEquals(0, meter.getStopTime(), "stopTime should be 0");
        }

        if (okPath == null) {
            assertNull(meter.getOkPath(), "okPath should be null");
        } else {
            assertEquals(okPath, meter.getOkPath(), "okPath should match expected value: " + okPath);
        }

        if (rejectPath == null) {
            assertNull(meter.getRejectPath(), "rejectPath should be null");
        } else {
            assertEquals(rejectPath, meter.getRejectPath(), "rejectPath should match expected value: " + rejectPath);
        }

        if (failPath == null) {
            assertNull(meter.getFailPath(), "failPath should be null");
        } else {
            assertEquals(failPath, meter.getFailPath(), "failPath should match expected value: " + failPath);
        }

        if (failMessage == null) {
            assertNull(meter.getFailMessage(), "failMessage should be null");
        } else {
            assertEquals(failMessage, meter.getFailMessage(), "failMessage should match expected value: " + failMessage);
        }

        assertEquals(currentIteration, meter.getCurrentIteration(), "currentIteration should match expected value: " + currentIteration);
        assertEquals(expectedIterations, meter.getExpectedIterations(), "expectedIterations should match expected value: " + expectedIterations);
        assertEquals(timeLimitMilliseconds * 1000 * 1000, meter.getTimeLimit(), "timeLimit should match expected value: " + timeLimitMilliseconds + "ms");

        assertTrue(meter.getCreateTime() > 0, "createTime should be > 0");
        if (stopped) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStopTime(), "lastCurrentTime should be >= stopTime");
        } else if (started) {
            assertTrue(meter.getLastCurrentTime() >= meter.getStartTime(), "lastCurrentTime should be >= startTime");
        } else {
            assertEquals(meter.getCreateTime(), meter.getLastCurrentTime(), "lastCurrentTime should be equal to createTime");
        }
    }

    @Nested
    @DisplayName("Group 1: Meter Initialization (Base Guarantee)")
    class MeterInitialization {
        @Test
        @DisplayName("should create meter with logger - initial state")
        void shouldCreateMeterWithLoggerInitialState() {
            // Given: a new Meter with logger only
            final Meter meter = new Meter(logger);

            // Then: meter has expected initial state (startTime = 0, stopTime = 0)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
            
            // Then: no logs yet
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should create meter with logger + operationName")
        void shouldCreateMeterWithOperationName() {
            // Given: a new Meter with logger and operationName
            final String operationName = "testOperation";
            final Meter meter = new Meter(logger, operationName);

            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: operationName is captured correctly
            assertEquals(operationName, meter.getOperation(), "operation name should match");
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
        }

        @Test
        @DisplayName("should create meter with logger + operationName + parent")
        void shouldCreateMeterWithParent() {
            // Given: a new Meter with logger, operationName, and parent
            final String operationName = "childOperation";
            final String parentId = "parent-meter-id";
            final Meter meter = new Meter(logger, operationName, parentId);

            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // Then: operationName and parent are captured correctly
            assertEquals(operationName, meter.getOperation(), "operation name should match");
            assertEquals(parentId, meter.getParent(), "parent should match");
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");
        }

        @Test
        @DisplayName("should start meter successfully")
        @ValidateCleanMeter(expectDirtyStack = true)
        void shouldStartMeterSuccessfully() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            
            // Then: before start(), getCurrentInstance() returns unknown meter
            final Meter currentBeforeStart = Meter.getCurrentInstance();
            assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                "before start(), getCurrentInstance() should return unknown meter");

            // When: start() is called
            meter.start();

            // Then: Meter transitions from Created to Started
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);
            
            // Then: after start(), meter becomes current in thread-local and is returned by getCurrentInstance()
            final Meter currentAfterStart = Meter.getCurrentInstance();
            assertEquals(meter, currentAfterStart, "after start(), meter should be current in thread-local");

            // Then: log messages recorded correctly with system status information
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        @Test
        @DisplayName("should start meter in try-with-resources (start in block)")
        void shouldStartMeterInTryWithResourcesSequential() {
            // Given: Meter is created in try-with-resources
            try (Meter m = new Meter(logger)) {
                // Then: meter has expected initial state before start
                assertMeterState(m, false, false, null, null, null, null, 0, 0, 0);
                
                // Then: before start(), getCurrentInstance() returns unknown meter
                final Meter currentBeforeStart = Meter.getCurrentInstance();
                assertEquals(Meter.UNKNOWN_LOGGER_NAME, currentBeforeStart.getCategory(),
                    "before start(), getCurrentInstance() should return unknown meter");
                
                // When: start() is called in the block
                m.start();
                
                // Then: meter is transitioned to executing state
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
                
                // Then: after start(), meter becomes current in thread-local
                final Meter currentAfterStart = Meter.getCurrentInstance();
                assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }

        @Test
        @DisplayName("should start meter with chained call in try-with-resources")
        void shouldStartMeterInTryWithResourcesChained() {
            // Given: Meter is created with chained start() in try-with-resources
            try (Meter m = new Meter(logger).start()) {
                // Then: meter is in executing state (created and started in single expression)
                assertMeterState(m, true, false, null, null, null, null, 0, 0, 0);
                
                // Then: meter becomes current in thread-local after start()
                final Meter currentAfterStart = Meter.getCurrentInstance();
                assertEquals(m, currentAfterStart, "after start(), meter should be current in thread-local");
            }

            // Then: start log messages recorded correctly
            AssertLogger.assertEvent(logger, 0, Level.DEBUG, Markers.MSG_START);
            AssertLogger.assertEvent(logger, 1, Level.TRACE, Markers.DATA_START);
        }
    }

    @Nested
    @DisplayName("Group 2: Pre-Start Configuration (Tier 2 - Valid Non-State-Changing)")
    class PreStartConfiguration {
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 5000);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when negative time limit attempted")
        void shouldLogIllegalWhenNegativeTimeLimitAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: limitMilliseconds(-1) is called
            meter.limitMilliseconds(-1);

            // Then: meter remains in Created state with no time limit set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when negative iterations attempted")
        void shouldLogIllegalWhenNegativeIterationsAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: iterations(-5) is called
            meter.iterations(-5);

            // Then: meter remains in Created state with no iterations set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
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
            assertEquals("starting operation", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("step 1", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when null message attempted before any valid message")
        void shouldLogIllegalWhenNullMessageAttemptedBeforeAnyValidMessage() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m(null) is called without setting a previous message
            meter.m(null);

            // Then: description remains null, meter remains in Created state
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
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
            assertEquals("operation doWork", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("step 2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
        }

        @Test
        @DisplayName("should log ILLEGAL when invalid format string attempted")
        void shouldLogIllegalWhenInvalidFormatStringAttempted() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: m("invalid format %z", "arg") is called (invalid format specifier)
            meter.m("invalid format %z", "arg");

            // Then: meter remains in Created state with no description set
            assertNull(meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
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
            assertEquals("value1", meter.getContext().get("key1"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("val2", meter.getContext().get("key"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            AssertLogger.assertEventCount(logger, 0);
        }

        @Test
        @DisplayName("should replace context value when null value set")
        void shouldReplaceContextValueWhenNullValueSet() {
            // Given: a new Meter
            final Meter meter = new Meter(logger);

            // When: ctx("key", "valid") is called, then ctx("key", null) is called
            meter.ctx("key", "valid");
            meter.ctx("key", (String)null);

            // Then: null value is stored as "<null>" placeholder (context stores null as string literal)
            assertEquals("<null>", meter.getContext().get("key"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("value1", meter.getContext().get("key1"));
            assertEquals("value2", meter.getContext().get("key2"));
            assertEquals("value3", meter.getContext().get("key3"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
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
            assertEquals("starting operation", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
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
            assertEquals("op2", meter.getDescription());
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
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
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 1, Level.ERROR, Markers.ILLEGAL);
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
            assertEquals("starting operation", meter.getDescription());
            assertEquals("testUser", meter.getContext().get("user"));
            assertEquals("test-session-123", meter.getContext().get("session"));
            assertMeterState(meter, false, false, null, null, null, null, 0, 100, 5000);
            AssertLogger.assertEventCount(logger, 0);
        }
    }


    @Nested
    @DisplayName("Success Flow")
    class SuccessFlow {
        @Test
        @DisplayName("should follow normal success flow")
        void shouldFollowNormalSuccessFlow() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (String)")
        void shouldFollowSuccessFlowWithPathString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok("customPath") is called
            meter.ok("customPath");

            // Then: Meter is in stopped state with path set
            assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Enum)")
        void shouldFollowSuccessFlowWithPathEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(TestEnum.VALUE1) is called
            meter.ok(TestEnum.VALUE1);

            // Then: Meter is in stopped state with enum path
            assertMeterState(meter, true, true, TestEnum.VALUE1.name(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Throwable)")
        void shouldFollowSuccessFlowWithPathThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(exception) is called
            final Exception ex = new RuntimeException("error");
            meter.ok(ex);

            // Then: Meter is in stopped state with exception class as path
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path (Object)")
        void shouldFollowSuccessFlowWithPathObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(object) is called
            final TestObject obj = new TestObject();
            meter.ok(obj);

            // Then: Meter is in stopped state with object toString as path
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (String)")
        void shouldFollowSuccessFlowWithPathMethodString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("predefinedPath") is called
            meter.path("predefinedPath");

            // Then: path is set
            assertMeterState(meter, true, false, "predefinedPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with path preserved
            assertMeterState(meter, true, true, "predefinedPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Enum)")
        void shouldFollowSuccessFlowWithPathMethodEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(TestEnum.VALUE2) is called
            meter.path(TestEnum.VALUE2);

            // Then: enum path is set
            assertMeterState(meter, true, false, TestEnum.VALUE2.name(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with enum path preserved
            assertMeterState(meter, true, true, TestEnum.VALUE2.name(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Throwable)")
        void shouldFollowSuccessFlowWithPathMethodThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(exception) is called
            final Exception ex = new IllegalArgumentException("invalid");
            meter.path(ex);

            // Then: exception class is set as path
            assertMeterState(meter, true, false, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with exception path preserved
            assertMeterState(meter, true, true, ex.getClass().getSimpleName(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path() method (Object)")
        void shouldFollowSuccessFlowWithPathMethodObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(object) is called
            final TestObject obj = new TestObject();
            meter.path(obj);

            // Then: object toString is set as path
            assertMeterState(meter, true, false, obj.toString(), null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with object path preserved
            assertMeterState(meter, true, true, obj.toString(), null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow with path override")
        void shouldFollowSuccessFlowWithPathOverride() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("initialPath") is called
            meter.path("initialPath");

            // Then: initial path is set
            assertMeterState(meter, true, false, "initialPath", null, null, null, 0, 0, 0);

            // When: ok("finalPath") is called (overriding initialPath)
            meter.ok("finalPath");

            // Then: final path overrides initial path
            assertMeterState(meter, true, true, "finalPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should override path when path() is called twice")
        void shouldOverridePathWhenPathCalledTwice() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("firstPath") is called
            meter.path("firstPath");

            // Then: first path is set
            assertMeterState(meter, true, false, "firstPath", null, null, null, 0, 0, 0);

            // When: path("secondPath") is called (should override firstPath)
            meter.path("secondPath");

            // Then: second path overrides first path
            assertMeterState(meter, true, false, "secondPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with second path preserved
            assertMeterState(meter, true, true, "secondPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error and keep previous path when path(non-null) then path(null)")
        void shouldKeepPreviousPathWhenPathNullAfterNonNull() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("validPath") is called
            meter.path("validPath");

            // Then: path is set
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            // When: path(null) is called (should log error and ignore, keeping previous path)
            meter.path(null);

            // Then: path is kept as "validPath" (null argument ignored)
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            // When: ok() is called
            meter.ok();

            // Then: Meter is in stopped state with okPath="validPath"
            assertMeterState(meter, true, true, "validPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should complete with set path when ok(null) is called")
        void shouldCompleteWithPathWhenOkNullAfterPathNonNull() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path("validPath") is called
            meter.path("validPath");

            // Then: path is set
            assertMeterState(meter, true, false, "validPath", null, null, null, 0, 0, 0);

            // When: ok(null) is called (should complete with validPath preserved)
            meter.ok(null);

            // Then: Meter is in stopped state with validPath preserved
            assertMeterState(meter, true, true, "validPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow using success() alias")
        void shouldFollowSuccessFlowUsingSuccessAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success() is called
            meter.success();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow success flow using success(path) alias")
        void shouldFollowSuccessFlowUsingSuccessPathAlias() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: success("aliasPath") is called
            meter.success("aliasPath");

            // Then: Meter is in stopped state with path set
            assertMeterState(meter, true, true, "aliasPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log error and continue when path(null) is called")
        void shouldLogErrorAndContinueWhenPathNullIsCalled() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: path(null) is called (should log error but not throw)
            meter.path(null);

            // Then: path remains null
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 0);

            // When: ok() is called after null path
            meter.ok();

            // Then: Meter is in stopped state
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should complete successfully when ok(null) is called")
        void shouldLogErrorAndContinueWhenOkNullIsCalled() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: ok(null) is called (should complete operation without path)
            meter.ok(null);

            // Then: Meter is in stopped state with no path set
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.ILLEGAL);
            AssertLogger.assertEvent(logger, 3, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 4, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log slow operation when limit is exceeded")
        void shouldLogSlowOperationWhenLimitIsExceeded() throws InterruptedException {
            // Given: a new Meter
            final Meter meter = new Meter(logger);
            
            // Then: meter has expected initial state
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);
            
            // When: limitMilliseconds(1) is called
            meter.limitMilliseconds(1);
            
            // Then: time limit is set
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 1);
            
            // When: start() is called
            meter.start();
            
            // Then: meter is in executing state with time limit
            assertMeterState(meter, true, false, null, null, null, null, 0, 0, 1);

            // When: operation takes longer than limit
            Thread.sleep(10);
            meter.ok();

            // Then: Meter is in stopped state with time limit preserved
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 1);

            // Then: MSG_SLOW_OK (WARN) should be logged instead of MSG_OK (INFO)
            AssertLogger.assertEvent(logger, 2, Level.WARN, Markers.MSG_SLOW_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_SLOW_OK);
        }
    }

    @Nested
    @DisplayName("Created State Path Calls")
    class CreatedStatePathCallTests {
        @Test
        @DisplayName("should NOT modify okPath when path(String) is called before start()")
        void shouldNotModifyPathWhenPathStringBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path("pathId") is called before start()
            meter.path("pathId");

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL,
                "Meter path but not started");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Enum) is called before start()")
        void shouldNotModifyPathWhenPathEnumBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(TestEnum.VALUE1) is called before start()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL,
                "Meter path but not started");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Throwable) is called before start()")
        void shouldNotModifyPathWhenPathThrowableBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(new Exception()) is called before start()
            meter.path(new RuntimeException("test"));

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL,
                "Meter path but not started");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Object) is called before start()")
        void shouldNotModifyPathWhenPathObjectBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(new TestObject()) is called before start()
            meter.path(new TestObject());

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL,
                "Meter path but not started");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(null) is called before start()")
        void shouldNotModifyPathWhenPathNullBeforeStart() {
            // Given: a new, not yet started Meter
            final Meter meter = new Meter(logger);

            // When: path(null) is called before start() (null argument)
            meter.path(null);

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, false, false, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker for null argument
            AssertLogger.assertEvent(logger, 0, Level.ERROR, Markers.ILLEGAL,
                "Null argument");
        }
    }

    @Nested
    @DisplayName("Rejection Flow")
    class RejectionFlow {
        @Test
        @DisplayName("should follow rejection flow (String)")
        void shouldFollowRejectionFlowString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject("businessRule") is called
            meter.reject("businessRule");

            // Then: Meter is in stopped state with reject path set
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Enum)")
        void shouldFollowRejectionFlowEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(TestEnum.VALUE1) is called
            meter.reject(TestEnum.VALUE1);

            // Then: Meter is in stopped state with enum reject path
            assertMeterState(meter, true, true, null, TestEnum.VALUE1.name(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Throwable)")
        void shouldFollowRejectionFlowThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(exception) is called
            final Exception ex = new RuntimeException("rejected");
            meter.reject(ex);

            // Then: Meter is in stopped state with exception class as reject path
            assertMeterState(meter, true, true, null, ex.getClass().getSimpleName(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow rejection flow (Object)")
        void shouldFollowRejectionFlowObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: reject(object) is called
            final TestObject obj = new TestObject();
            meter.reject(obj);

            // Then: Meter is in stopped state with object toString as reject path
            assertMeterState(meter, true, true, null, obj.toString(), null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }
    }

    @Nested
    @DisplayName("Failure Flow")
    class FailureFlow {
        @Test
        @DisplayName("should follow failure flow (Throwable)")
        void shouldFollowFailureFlowThrowable() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(exception) is called
            final Exception ex = new RuntimeException("technical error");
            meter.fail(ex);

            // Then: Meter is in stopped state with exception details in fail path
            assertMeterState(meter, true, true, null, null, ex.getClass().getName(), ex.getMessage(), 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (String)")
        void shouldFollowFailureFlowString() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail("technical error") is called
            meter.fail("technical error");

            // Then: Meter is in stopped state with failure message
            assertMeterState(meter, true, true, null, null, "technical error", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (Enum)")
        void shouldFollowFailureFlowEnum() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(TestEnum.VALUE2) is called
            meter.fail(TestEnum.VALUE2);

            // Then: Meter is in stopped state with enum fail path
            assertMeterState(meter, true, true, null, null, TestEnum.VALUE2.name(), null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow failure flow (Object)")
        void shouldFollowFailureFlowObject() {
            // Given: a new, started Meter
            final Meter meter = new Meter(logger).start();

            // When: fail(object) is called
            final TestObject obj = new TestObject();
            meter.fail(obj);

            // Then: Meter is in stopped state with object toString as fail path
            assertMeterState(meter, true, true, null, null, obj.toString(), null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }
    }

    @Nested
    @DisplayName("Try-With-Resources")
    class TryWithResources {
        @Test
        @DisplayName("should follow try-with-resources flow (implicit failure)")
        void shouldFollowTryWithResourcesFlowImplicitFailure() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
                meter = m;
                // do nothing
            }

            // Then: it should be automatically failed on close()
            assertMeterState(meter, true, true, null, null, "try-with-resources", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit success)")
        void shouldFollowTryWithResourcesFlowExplicitSuccess() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
                meter = m;
                
                // When: ok() is called
                m.ok();
                
                // Then: Meter is in stopped state
                assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);
            }

            // Then: it should remain in success state after close()
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (path and explicit success)")
        void shouldFollowTryWithResourcesFlowPathAndExplicitSuccess() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
                meter = m;
                
                // When: path() is called
                m.path("customPath");
                
                // Then: path is set
                assertMeterState(meter, true, false, "customPath", null, null, null, 0, 0, 0);
                
                // When: ok() is called
                m.ok();
                
                // Then: Meter is in stopped state with path preserved
                assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);
            }

            // Then: it should remain in success state after close()
            assertMeterState(meter, true, true, "customPath", null, null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit rejection)")
        void shouldFollowTryWithResourcesFlowExplicitRejection() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
                meter = m;
                
                // When: reject() is called
                m.reject("rejected");
                
                // Then: Meter is in stopped state with reject path set
                assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);
            }

            // Then: it should remain in rejection state after close()
            assertMeterState(meter, true, true, null, "rejected", null, null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_REJECT);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_REJECT);
        }

        @Test
        @DisplayName("should follow try-with-resources flow (explicit failure)")
        void shouldFollowTryWithResourcesFlowExplicitFailure() {
            final Meter meter;
            // Given: a new, started Meter withing try with resources
            try (Meter m = new Meter(logger).start()) {
                meter = m;
                
                // When: fail() is called
                m.fail("failed");
                
                // Then: Meter is in stopped state with failure message
                assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);
            }

            // Then: it should remain in failure state after close()
            assertMeterState(meter, true, true, null, null, "failed", null, 0, 0, 0);

            // Then: all log messages recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.ERROR, Markers.MSG_FAIL);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_FAIL);
        }
    }

    @Nested
    @DisplayName("Stopped State Path Calls")
    class StoppedStatePathCallTests {
        @Test
        @DisplayName("should NOT modify okPath when path(String) is called after ok()")
        void shouldNotModifyPathWhenPathStringAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path("newPath") is called after ok()
            meter.path("newPath");

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Enum) is called after ok()")
        void shouldNotModifyPathWhenPathEnumAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(TestEnum.VALUE1) is called after ok()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Throwable) is called after ok()")
        void shouldNotModifyPathWhenPathThrowableAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(new Exception()) is called after ok()
            meter.path(new RuntimeException("test"));

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Object) is called after ok()")
        void shouldNotModifyPathWhenPathObjectAfterOk() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().ok();

            // When: path(new TestObject()) is called after ok()
            meter.path(new TestObject());

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(String) is called after reject()")
        void shouldNotModifyPathWhenPathStringAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path("newPath") is called after reject()
            meter.path("newPath");

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Enum) is called after reject()")
        void shouldNotModifyPathWhenPathEnumAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(TestEnum.VALUE1) is called after reject()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Throwable) is called after reject()")
        void shouldNotModifyPathWhenPathThrowableAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(new Exception()) is called after reject()
            meter.path(new RuntimeException("test"));

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Object) is called after reject()")
        void shouldNotModifyPathWhenPathObjectAfterReject() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(new TestObject()) is called after reject()
            meter.path(new TestObject());

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(String) is called after fail()")
        void shouldNotModifyPathWhenPathStringAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path("newPath") is called after fail()
            meter.path("newPath");

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Enum) is called after fail()")
        void shouldNotModifyPathWhenPathEnumAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(TestEnum.VALUE1) is called after fail()
            meter.path(TestEnum.VALUE1);

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Throwable) is called after fail()")
        void shouldNotModifyPathWhenPathThrowableAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(new Exception()) is called after fail()
            meter.path(new RuntimeException("test"));

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(Object) is called after fail()")
        void shouldNotModifyPathWhenPathObjectAfterFail() {
            // Given: a new, stopped Meter
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(new TestObject()) is called after fail()
            meter.path(new TestObject());

            // Then: okPath should remain null (illegal call ignored)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Meter path but already stopped");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(null) is called after ok()")
        void shouldNotModifyPathWhenPathNullAfterOk() {
            // Given: a new, stopped Meter with a success path
            final Meter meter = new Meter(logger).start().ok("successPath");

            // When: path(null) is called after ok() (null argument)
            meter.path(null);

            // Then: okPath should remain "successPath" (illegal calls ignored)
            assertMeterState(meter, true, true, "successPath", null, null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker for null argument
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Null argument");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(null) is called after reject()")
        void shouldNotModifyPathWhenPathNullAfterReject() {
            // Given: a new, stopped Meter with a reject path and ok path set
            final Meter meter = new Meter(logger).start().reject("businessRule");

            // When: path(null) is called after reject() (null argument)
            meter.path(null);

            // Then: okPath should remain null (illegal calls ignored)
            assertMeterState(meter, true, true, null, "businessRule", null, null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker for null argument
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Null argument");
        }

        @Test
        @DisplayName("should NOT modify okPath when path(null) is called after fail()")
        void shouldNotModifyPathWhenPathNullAfterFail() {
            // Given: a new, stopped Meter with a fail path and ok path set
            final Meter meter = new Meter(logger).start().fail("error occurred");

            // When: path(null) is called after fail() (null argument)
            meter.path(null);

            // Then: okPath should remain null (illegal calls ignored)
            assertMeterState(meter, true, true, null, null, "error occurred", null, 0, 0, 0);

            // Then: error logged with ILLEGAL marker for null argument
            AssertLogger.assertEvent(logger, 4, Level.ERROR, Markers.ILLEGAL,
                "Null argument");
        }
    }

    @Nested
    @DisplayName("Iteration Tracking")
    class IterationTracking {
        @Test
        @DisplayName("should track iterations with incBy and incTo")
        void shouldTrackIterationsWithIncByAndIncTo() {
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: incBy(10) is called
            meter.incBy(10);
            assertMeterState(meter, true, false, null, null, null, null, 10, 100, 0);

            // When: incTo(50) is called
            meter.incTo(50);
            assertMeterState(meter, true, false, null, null, null, null, 50, 100, 0);

            meter.ok();
            assertMeterState(meter, true, true, null, null, null, null, 50, 100, 0);
        }

        @Test
        @DisplayName("should log progress when progressPeriodMilliseconds is zero")
        void shouldLogProgressWhenProgressPeriodMillisecondsIsZero() throws InterruptedException {
            // Given: progress throttling disabled (period = 0ms)
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: progress advances and enough time elapses
            meter.inc();
            Thread.sleep(5);
            meter.progress();

            // Then: meter is still started and progress is tracked
            assertMeterState(meter, true, false, null, null, null, null, 1, 100, 0);

            // When: operation completes successfully
            meter.ok();

            // Then: meter is stopped OK
            assertMeterState(meter, true, true, null, null, null, null, 1, 100, 0);

            // Then: progress and OK log messages are recorded correctly
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should NOT log progress when progressPeriodMilliseconds is high")
        void shouldNotLogProgressWhenProgressPeriodMillisecondsIsHigh() throws InterruptedException {
            // Given: progress throttling period is high
            MeterConfig.progressPeriodMilliseconds = 10_000;
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: progress advances but period has not elapsed
            meter.inc();
            Thread.sleep(5);
            meter.progress();
            meter.ok();

            // Then: meter is stopped OK and iteration tracking is preserved
            assertMeterState(meter, true, true, null, null, null, null, 1, 100, 0);

            // Then: only start and OK logs exist (no progress logs)
            AssertLogger.assertEventCount(logger, 4);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log progress when using incBy with progressPeriodMilliseconds zero")
        void shouldLogProgressWhenUsingIncByWithProgressPeriodMillisecondsZero() throws InterruptedException {
            // Given: progress throttling disabled (period = 0ms)
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: progress advances via incBy
            meter.incBy(10);
            Thread.sleep(5);
            meter.progress();
            meter.ok();

            // Then: iteration tracking and logs reflect progress
            assertMeterState(meter, true, true, null, null, null, null, 10, 100, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }

        @Test
        @DisplayName("should log progress when using incTo with progressPeriodMilliseconds zero")
        void shouldLogProgressWhenUsingIncToWithProgressPeriodMillisecondsZero() throws InterruptedException {
            // Given: progress throttling disabled (period = 0ms)
            MeterConfig.progressPeriodMilliseconds = 0;
            final Meter meter = new Meter(logger);
            meter.iterations(100);
            meter.start();

            // When: progress advances via incTo
            meter.incTo(50);
            Thread.sleep(5);
            meter.progress();
            meter.ok();

            // Then: iteration tracking and logs reflect progress
            assertMeterState(meter, true, true, null, null, null, null, 50, 100, 0);
            AssertLogger.assertEvent(logger, 2, Level.INFO, Markers.MSG_PROGRESS);
            AssertLogger.assertEvent(logger, 3, Level.TRACE, Markers.DATA_PROGRESS);
            AssertLogger.assertEvent(logger, 4, Level.INFO, Markers.MSG_OK);
            AssertLogger.assertEvent(logger, 5, Level.TRACE, Markers.DATA_OK);
        }
    }
}
