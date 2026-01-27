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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.CallerStackTraceThrowable;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEventWithThrowable;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvents;

/**
 * Unit tests for {@link MeterValidator}.
 * <p>
 * Tests validate that MeterValidator correctly performs precondition checks,
 * argument validation, and error logging for various Meter operations.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Start Precondition:</b> Validates that start operations are only allowed when meter is not started</li>
 *   <li><b>Stop Precondition:</b> Validates preconditions before stopping (started, not already stopped, in order)</li>
 *   <li><b>Sub-call Arguments:</b> Validates sub-operation name arguments are not null</li>
 *   <li><b>Message Arguments:</b> Validates message strings and formatted arguments are valid</li>
 *   <li><b>Increment Arguments:</b> Validates iteration increment values are positive and forward</li>
 *   <li><b>Progress Precondition:</b> Validates meter is started before progress reporting</li>
 *   <li><b>Path Arguments:</b> Validates path arguments are not null</li>
 *   <li><b>Finalization:</b> Validates finalization checks for started-but-not-stopped meters</li>
 *   <li><b>Error Logging:</b> Validates proper error logging with markers and stack traces</li>
 * </ul>
 */
@ValidateCharset
@ResetMeterConfig
@WithMockLogger
@ValidateCleanMeter
public class MeterValidatorTest {

    @Mock
    protected Meter meter;

    @Slf4jMock
    protected Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(meter.getMessageLogger()).thenReturn(logger);
        lenient().when(meter.getFullID()).thenReturn("test-id");
    }

    @Nested
    @DisplayName("Start Precondition Tests")
    class StartPreconditionTests {

        @Test
        @DisplayName("should validate start precondition when meter is not started")
        void shouldValidateStartPreconditionWhenNotStarted() {
            // Given: meter has not been started (start time = 0)
            when(meter.getStartTime()).thenReturn(0L);
            // When: validateStartPrecondition is called
            // Then: should return true and log no events
            assertTrue(MeterValidator.validateStartPrecondition(meter), "should allow start when not started");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should prevent start when meter is already started")
        void shouldValidateStartPreconditionWhenAlreadyStarted() {
            // Given: meter has already been started (start time = 1L)
            when(meter.getStartTime()).thenReturn(1L);
            // When: validateStartPrecondition is called
            // Then: should return false and log error event
            assertFalse(MeterValidator.validateStartPrecondition(meter), "should reject start when already started");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START, "Meter already started; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Stop Precondition Tests")
    class StopPreconditionTests {

        @Test
        @DisplayName("should validate stop precondition when conditions are met")
        void shouldValidateStopPreconditionWhenOk() {
            // Given: meter has been started and not stopped, and is the current instance
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getStartTime()).thenReturn(1L);
            when(meter.checkCurrentInstance()).thenReturn(false);
            // When: validateStopPrecondition is called
            final boolean result = MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
            // Then: should return true and not log any events
            assertTrue(result, "should return true when meter can proceed to stop");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should prevent stop when meter is already stopped")
        void shouldValidateStopPreconditionWhenAlreadyStopped() {
            // Given: meter has already been stopped (stop time = 1L)
            when(meter.getStopTime()).thenReturn(1L);
            // When: validateStopPrecondition is called
            final boolean result = MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
            // Then: should return false and log error event
            assertFalse(result, "should return false when meter is already stopped");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter already stopped; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }

        @Test
        @DisplayName("should prevent stop when meter is not started")
        void shouldValidateStopPreconditionWhenNotStarted() {
            // Given: meter has not been started (start time = 0L) and not stopped (stop time = 0L)
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getStartTime()).thenReturn(0L);
            // When: validateStopPrecondition is called
            final boolean result = MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
            // Then: should return true (warning only, not blocker) and log error event
            assertTrue(result, "should return true even with warning (not started is not a blocker)");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter stopped but not started; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }

        @Test
        @DisplayName("should prevent stop when meter is out of order")
        void shouldValidateStopPreconditionWhenOutOfOrder() {
            // Given: meter is started but out of order (checkCurrentInstance returns true)
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getStartTime()).thenReturn(1L);
            when(meter.checkCurrentInstance()).thenReturn(true);
            // When: validateStopPrecondition is called
            final boolean result = MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
            // Then: should return true (warning only, not blocker) and log error event
            assertTrue(result, "should return true even with warning (out of order is not a blocker)");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter out of order; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("M Precondition Tests")
    class MPreconditionTests {

        @Test
        @DisplayName("should validate m precondition when meter is not stopped")
        void shouldValidateMPreconditionWhenNotStopped() {
            // Given: meter has not been stopped (stop time = 0L)
            when(meter.getStopTime()).thenReturn(0L);
            // When: validateMPrecondition is called
            // Then: should return true and log no events
            assertTrue(MeterValidator.validateMPrecondition(meter), "should allow m() when not stopped");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject m when meter is already stopped")
        void shouldValidateMPreconditionWhenStopped() {
            // Given: meter has already been stopped (stop time = 1L)
            when(meter.getStopTime()).thenReturn(1L);
            // When: validateMPrecondition is called
            // Then: should return false and log error event
            assertFalse(MeterValidator.validateMPrecondition(meter), "should reject m() when already stopped");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Meter m but already stopped; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Sub-call Arguments Tests")
    class SubCallArgumentsTests {

        @Test
        @DisplayName("should validate sub-call arguments when name is provided")
        void shouldValidateSubCallArgumentsWhenOk() {
            // Given: a valid sub-operation name
            // When: validateSubCallArguments is called
            // Then: should not log any events
            MeterValidator.validateSubCallArguments(meter, "sub-op");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject sub-call when name is null")
        void shouldValidateSubCallArgumentsWhenNull() {
            // Given: a null sub-operation name
            // When: validateSubCallArguments is called
            // Then: should log illegal argument error
            MeterValidator.validateSubCallArguments(meter, null);
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.sub(name): Null argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Message Arguments Tests")
    class MessageArgumentsTests {

        @Test
        @DisplayName("should validate message call arguments when message is provided")
        void shouldValidateMCallArgumentsWithMessageWhenOk() {
            // Given: a valid message string
            // When: validateMCallArguments is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateMCallArguments(meter, "message"), "should accept valid message");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject message call when message is null")
        void shouldValidateMCallArgumentsWithMessageWhenNull() {
            // Given: a null message
            // When: validateMCallArguments is called
            // Then: should return false and log illegal argument error
            assertFalse(MeterValidator.validateMCallArguments(meter, null), "should reject null message");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message): Null argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Format and Message Arguments Tests")
    class FormatAndMessageArgumentsTests {

        @Test
        @DisplayName("should format message with arguments when format is valid")
        void shouldValidateAndFormatMCallArgumentsWhenOk() {
            // Given: a valid format string and arguments
            // When: validateAndFormatMCallArguments is called
            // Then: should return formatted message and not log any events
            assertEquals("message 1", MeterValidator.validateAndFormatMCallArguments(meter, "message %d", 1), "should format message correctly");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject format when message is null")
        void shouldValidateAndFormatMCallArgumentsWhenNull() {
            // Given: a null message format string
            // When: validateAndFormatMCallArguments is called
            // Then: should return null and log illegal argument error
            assertNull(MeterValidator.validateAndFormatMCallArguments(meter, null, 1), "should return null for null format");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message, args...): Null argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }

        @Test
        @DisplayName("should format message with extra arguments when there are too many args")
        void shouldValidateAndFormatMCallArgumentsWhenIllegalFormat1() {
            // Given: format string with fewer specifiers than provided arguments
            // When: validateAndFormatMCallArguments is called
            // Then: String.format handles it gracefully by returning formatted message
            assertEquals("message 1", MeterValidator.validateAndFormatMCallArguments(meter, "message %s", 1, 2), "should format message with extra args");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject format when argument type does not match specifier")
        void shouldValidateAndFormatMCallArgumentsWhenIllegalFormat2() {
            // Given: format string with %d specifier but string argument
            // When: validateAndFormatMCallArguments is called
            // Then: should return null and log illegal format error
            assertNull(MeterValidator.validateAndFormatMCallArguments(meter, "message %d", "s"), "should return null for mismatched format");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message, args...): Illegal string format; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Time Limit Arguments Tests")
    class TimeLimitArgumentsTests {

        @Test
        @DisplayName("should validate limit milliseconds when value is positive")
        void shouldValidateLimitMillisecondsCallArgumentsWhenOk() {
            // Given: a positive time limit in milliseconds
            // When: validateLimitMillisecondsCallArguments is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateLimitMillisecondsCallArguments(meter, 100L), "should accept positive limit");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject limit milliseconds when value is not positive")
        void shouldValidateLimitMillisecondsCallArgumentsWhenNonPositive() {
            // Given: a non-positive time limit value
            // When: validateLimitMillisecondsCallArguments is called
            // Then: should return false and log illegal argument error
            assertFalse(MeterValidator.validateLimitMillisecondsCallArguments(meter, 0L), "should reject non-positive limit");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.limitMilliseconds(timeLimit): Non-positive argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Time Limit Precondition Tests")
    class TimeLimitPreconditionTests {

        @Test
        @DisplayName("should validate time limit precondition when not stopped")
        void shouldValidateLimitMillisecondsPreconditionWhenNotStopped() {
            // Given: meter is not stopped
            when(meter.getStopTime()).thenReturn(0L);
            // When: validateLimitMillisecondsPrecondition is called
            // Then: should return true
            assertTrue(MeterValidator.validateLimitMillisecondsPrecondition(meter), "should accept when meter not stopped");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject time limit precondition when already stopped")
        void shouldValidateLimitMillisecondsPreconditionWhenStopped() {
            // Given: meter is already stopped
            when(meter.getStopTime()).thenReturn(12345L);
            // When: validateLimitMillisecondsPrecondition is called
            // Then: should return false and log illegal precondition error
            assertFalse(MeterValidator.validateLimitMillisecondsPrecondition(meter), "should reject when meter already stopped");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Meter limitMilliseconds but already stopped; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Iterations Arguments Tests")
    class IterationsArgumentsTests {

        @Test
        @DisplayName("should validate iterations when count is positive")
        void shouldValidateIterationsCallArgumentsWhenOk() {
            // Given: a positive iteration count
            // When: validateIterationsCallArguments is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateIterationsCallArguments(meter, 100L), "should accept positive iteration count");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject iterations when count is not positive")
        void shouldValidateIterationsCallArgumentsWhenNonPositive() {
            // Given: a non-positive iteration count
            // When: validateIterationsCallArguments is called
            // Then: should return false and log illegal argument error
            assertFalse(MeterValidator.validateIterationsCallArguments(meter, 0L), "should reject non-positive iteration count");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.iterations(expectedIterations): Non-positive argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Iterations Precondition Tests")
    class IterationsPreconditionTests {

        @Test
        @DisplayName("should validate iterations precondition when not stopped")
        void shouldValidateIterationsPreconditionWhenNotStopped() {
            // Given: meter is not stopped
            when(meter.getStopTime()).thenReturn(0L);
            // When: validateIterationsPrecondition is called
            // Then: should return true
            assertTrue(MeterValidator.validateIterationsPrecondition(meter), "should accept when meter not stopped");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject iterations precondition when already stopped")
        void shouldValidateIterationsPreconditionWhenStopped() {
            // Given: meter is already stopped
            when(meter.getStopTime()).thenReturn(12345L);
            // When: validateIterationsPrecondition is called
            // Then: should return false and log illegal precondition error
            assertFalse(MeterValidator.validateIterationsPrecondition(meter), "should reject when meter already stopped");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Meter iterations but already stopped; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Increment Precondition Tests")
    class IncrementPreconditionTests {

        @Test
        @DisplayName("should validate increment precondition when meter is started")
        void shouldValidateIncPreconditionWhenOk() {
            // Given: meter has been started (start time = 1L)
            when(meter.getStartTime()).thenReturn(1L);
            // When: validateIncPrecondition is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateIncPrecondition(meter), "should allow increment when started");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should prevent increment when meter is not started")
        void shouldValidateIncPreconditionWhenNotStarted() {
            // Given: meter has not been started (start time = 0L)
            when(meter.getStartTime()).thenReturn(0L);
            // When: validateIncPrecondition is called
            // Then: should return false and log inconsistent increment error
            assertFalse(MeterValidator.validateIncPrecondition(meter), "should reject increment when not started");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT, "Meter not started; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Increment By Tests")
    class IncrementByTests {

        @Test
        @DisplayName("should validate increment-by when value is positive")
        void shouldValidateIncByWhenOk() {
            // Given: a positive increment value
            // When: validateIncBy is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateIncByArguments(meter, 10L), "should accept positive increment");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject increment-by when value is not positive")
        void shouldValidateIncByWhenNonPositive() {
            // Given: a non-positive increment value
            // When: validateIncBy is called
            // Then: should return false and log illegal argument error
            assertFalse(MeterValidator.validateIncByArguments(meter, 0L), "should reject non-positive increment");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incBy(increment): Non-positive increment; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Increment To Arguments Tests")
    class IncrementToArgumentsTests {

        @Test
        @DisplayName("should validate increment-to when value is forward")
        void shouldValidateIncToArgumentsWhenOk() {
            // Given: current iteration is 5 and target is 10 (forward direction)
            when(meter.getCurrentIteration()).thenReturn(5L);
            // When: validateIncToArguments is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateIncToArguments(meter, 10L), "should accept forward increment");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject increment-to when value is not positive")
        void shouldValidateIncToArgumentsWhenNonPositive() {
            // Given: a non-positive target iteration value
            // When: validateIncToArguments is called
            // Then: should return false and log illegal argument error
            assertFalse(MeterValidator.validateIncToArguments(meter, 0L), "should reject non-positive target iteration");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-positive argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }

        @Test
        @DisplayName("should reject increment-to when value does not move forward")
        void shouldValidateIncToArgumentsWhenNotForward() {
            // Given: current iteration is 10 and target is also 10 (no forward movement)
            when(meter.getCurrentIteration()).thenReturn(10L);
            // When: validateIncToArguments is called
            // Then: should return false and log non-forward increment error
            assertFalse(MeterValidator.validateIncToArguments(meter, 10L), "should reject non-forward increment");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-forward increment; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Progress Precondition Tests")
    class ProgressPreconditionTests {

        @Test
        @DisplayName("should validate progress precondition when meter is started")
        void shouldValidateProgressPreconditionWhenOk() {
            // Given: meter has been started (start time = 1L)
            when(meter.getStartTime()).thenReturn(1L);
            // When: validateProgressPrecondition is called
            // Then: should return true and not log any events
            assertTrue(MeterValidator.validateProgressPrecondition(meter), "should allow progress when started");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should prevent progress when meter is not started")
        void shouldValidateProgressPreconditionWhenNotStarted() {
            // Given: meter has not been started (start time = 0L)
            when(meter.getStartTime()).thenReturn(0L);
            // When: validateProgressPrecondition is called
            // Then: should return false and log inconsistent progress error
            assertFalse(MeterValidator.validateProgressPrecondition(meter), "should reject progress when not started");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS, "Meter progress but not started; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Path Arguments Tests")
    class PathArgumentsTests {

        @Test
        @DisplayName("should validate path argument when path is provided")
        void shouldValidatePathArgumentWhenOk() {
            // Given: a valid path argument
            // When: validatePathArgument is called
            // Then: should not log any events
            MeterValidator.validatePathArgument(meter, "myMethod", "path");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject path argument when path is null")
        void shouldValidatePathArgumentWhenNull() {
            // Given: a null path argument
            // When: validatePathArgument is called
            // Then: should log illegal argument error
            MeterValidator.validatePathArgument(meter, "myMethod", null);
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.myMethod: Null argument; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Context Precondition Tests")
    class ContextPreconditionTests {

        @Test
        @DisplayName("should validate context precondition when not stopped")
        void shouldValidateContextPreconditionWhenNotStopped() {
            // Given: meter is not stopped
            when(meter.getStopTime()).thenReturn(0L);
            // When: validateContextPrecondition is called
            // Then: should return true
            assertTrue(MeterValidator.validateContextPrecondition(meter), "should accept when meter not stopped");
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should reject context precondition when already stopped")
        void shouldValidateContextPreconditionWhenStopped() {
            // Given: meter is already stopped
            when(meter.getStopTime()).thenReturn(12345L);
            // When: validateContextPrecondition is called
            // Then: should return false and log illegal precondition error
            assertFalse(MeterValidator.validateContextPrecondition(meter), "should reject when meter already stopped");
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Meter putContext but already stopped; id=test-id");
            assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
        }
    }

    @Nested
    @DisplayName("Error Logging Tests")
    class ErrorLoggingTests {

        @Test
        @DisplayName("should log bug when exception is thrown")
        void shouldLogBugWhenExceptionThrown() {
            // Given: a throwable exception from a meter method
            final Throwable t = new RuntimeException("bug");
            // When: logBug is called
            // Then: should log bug marker with exception information
            MeterValidator.logBug(meter, "testMethod", t);
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.BUG, "Meter.testMethod method threw exception; id=test-id");
            assertEventWithThrowable(logger, 0, RuntimeException.class, "bug");
        }
    }

    @Nested
    @DisplayName("Finalization Tests")
    class FinalizationTests {

        @Test
        @DisplayName("should log error when meter is not stopped during finalization")
        void shouldValidateFinalizeWhenNotStopped() {
            // Given: meter has been started but not stopped (start time = 1L, stop time = 0L) and has a valid category
            when(meter.getStartTime()).thenReturn(1L);
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getCategory()).thenReturn("test-category");
            // When: validateFinalize is called
            // Then: should log finalization error
            MeterValidator.validateFinalize(meter);
            assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FINALIZED, "Meter started but never stopped; id=test-id");
        }

        @Test
        @DisplayName("should not log error when meter is already stopped")
        void shouldValidateFinalizeWhenAlreadyStopped() {
            // Given: meter has been started and stopped (start time = 1L, stop time = 2L)
            when(meter.getStartTime()).thenReturn(1L);
            when(meter.getStopTime()).thenReturn(2L);
            // When: validateFinalize is called
            // Then: should not log any events
            MeterValidator.validateFinalize(meter);
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should not log error when meter uses unknown logger name")
        void shouldValidateFinalizeWhenUnknownLogger() {
            // Given: meter has been started but not stopped and uses unknown logger name
            when(meter.getStartTime()).thenReturn(1L);
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getCategory()).thenReturn(Meter.UNKNOWN_LOGGER_NAME);
            // When: validateFinalize is called
            // Then: should not log any events (unknown logger is acceptable)
            MeterValidator.validateFinalize(meter);
            assertNoEvents(logger);
        }

        @Test
        @DisplayName("should not log error when meter was never started")
        void shouldValidateFinalizeWhenNotStarted() {
            // Given: meter was never started (start time = 0L)
            when(meter.getStartTime()).thenReturn(0L);
            when(meter.getStopTime()).thenReturn(0L);
            when(meter.getCategory()).thenReturn("test-category");
            // When: validateFinalize is called
            // Then: should not log any events
            MeterValidator.validateFinalize(meter);
            assertNoEvents(logger);
        }
    }
}
