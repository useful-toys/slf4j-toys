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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.CallerStackTraceThrowable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.slf4j.impl.AssertLogger.*;

public class MeterValidatorTest {

    @Mock
    private Meter meter;
    private MockLogger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logger = new MockLogger("TestLogger");
        lenient().when(meter.getMessageLogger()).thenReturn(logger);
        lenient().when(meter.getFullID()).thenReturn("test-id");
    }

    @Test
    void validateStartPrecondition_whenNotStarted() {
        when(meter.getStartTime()).thenReturn(0L);
        assertTrue(MeterValidator.validateStartPrecondition(meter));
        assertNoEvents(logger);
    }

    @Test
    void validateStartPrecondition_whenAlreadyStarted() {
        when(meter.getStartTime()).thenReturn(1L);
        assertFalse(MeterValidator.validateStartPrecondition(meter));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_START, "Meter already started; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateStopPrecondition_whenOk() {
        when(meter.getStopTime()).thenReturn(0L);
        when(meter.getStartTime()).thenReturn(1L);
        when(meter.checkCurrentInstance()).thenReturn(false);
        MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
        assertNoEvents(logger);
    }

    @Test
    void validateStopPrecondition_whenAlreadyStopped() {
        when(meter.getStopTime()).thenReturn(1L);
        MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter already stopped; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateStopPrecondition_whenNotStarted() {
        when(meter.getStopTime()).thenReturn(0L);
        when(meter.getStartTime()).thenReturn(0L);
        MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter stopped but not started; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateStopPrecondition_whenOutOfOrder() {
        when(meter.getStopTime()).thenReturn(0L);
        when(meter.getStartTime()).thenReturn(1L);
        when(meter.checkCurrentInstance()).thenReturn(true);
        MeterValidator.validateStopPrecondition(meter, Markers.INCONSISTENT_OK);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_OK, "Meter out of order; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateSubCallArguments_whenOk() {
        MeterValidator.validateSubCallArguments(meter, "sub-op");
        assertNoEvents(logger);
    }

    @Test
    void validateSubCallArguments_whenNull() {
        MeterValidator.validateSubCallArguments(meter, null);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.sub(name): Null argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateMCallArguments_withMessage_whenOk() {
        assertTrue(MeterValidator.validateMCallArguments(meter, "message"));
        assertNoEvents(logger);
    }

    @Test
    void validateMCallArguments_withMessage_whenNull() {
        assertFalse(MeterValidator.validateMCallArguments(meter, null));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message): Null argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateAndFormatMCallArguments_whenOk() {
        assertEquals("message 1", MeterValidator.validateAndFormatMCallArguments(meter, "message %d", 1));
        assertNoEvents(logger);
    }

    @Test
    void validateAndFormatMCallArguments_whenNull() {
        assertNull(MeterValidator.validateAndFormatMCallArguments(meter, null, 1));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message, args...): Null argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateAndFormatMCallArguments_whenIllegalFormat1() {
        // This case should not log an error, as String.format handles it by returning the format string itself
        // if there are too many arguments for the format specifiers.
        assertEquals("message 1", MeterValidator.validateAndFormatMCallArguments(meter, "message %s", 1, 2));
        assertNoEvents(logger);
    }

    @Test
    void validateAndFormatMCallArguments_whenIllegalFormat2() {
        assertNull(MeterValidator.validateAndFormatMCallArguments(meter, "message %d", "s"));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.m(message, args...): Illegal string format; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateLimitMillisecondsCallArguments_whenOk() {
        assertTrue(MeterValidator.validateLimitMillisecondsCallArguments(meter, 100L));
        assertNoEvents(logger);
    }

    @Test
    void validateLimitMillisecondsCallArguments_whenNonPositive() {
        assertFalse(MeterValidator.validateLimitMillisecondsCallArguments(meter, 0L));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.limitMilliseconds(timeLimit): Non-positive argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateIterationsCallArguments_whenOk() {
        assertTrue(MeterValidator.validateIterationsCallArguments(meter, 100L));
        assertNoEvents(logger);
    }

    @Test
    void validateIterationsCallArguments_whenNonPositive() {
        assertFalse(MeterValidator.validateIterationsCallArguments(meter, 0L));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.iterations(expectedIterations): Non-positive argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateIncPrecondition_whenOk() {
        when(meter.getStartTime()).thenReturn(1L);
        assertTrue(MeterValidator.validateIncPrecondition(meter));
        assertNoEvents(logger);
    }

    @Test
    void validateIncPrecondition_whenNotStarted() {
        when(meter.getStartTime()).thenReturn(0L);
        assertFalse(MeterValidator.validateIncPrecondition(meter));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_INCREMENT, "Meter not started; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateIncBy_whenOk() {
        assertTrue(MeterValidator.validateIncBy(meter, 10L));
        assertNoEvents(logger);
    }

    @Test
    void validateIncBy_whenNonPositive() {
        assertFalse(MeterValidator.validateIncBy(meter, 0L));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incBy(increment): Non-positive increment; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateIncToArguments_whenOk() {
        when(meter.getCurrentIteration()).thenReturn(5L);
        assertTrue(MeterValidator.validateIncToArguments(meter, 10L));
        assertNoEvents(logger);
    }

    @Test
    void validateIncToArguments_whenNonPositive() {
        assertFalse(MeterValidator.validateIncToArguments(meter, 0L));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-positive argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateIncToArguments_whenNotForward() {
        when(meter.getCurrentIteration()).thenReturn(10L);
        assertFalse(MeterValidator.validateIncToArguments(meter, 10L));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.incTo(currentIteration): Non-forward increment; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateProgressPrecondition_whenOk() {
        when(meter.getStartTime()).thenReturn(1L);
        assertTrue(MeterValidator.validateProgressPrecondition(meter));
        assertNoEvents(logger);
    }

    @Test
    void validateProgressPrecondition_whenNotStarted() {
        when(meter.getStartTime()).thenReturn(0L);
        assertFalse(MeterValidator.validateProgressPrecondition(meter));
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_PROGRESS, "Meter progress but not started; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validatePathArgument_whenOk() {
        MeterValidator.validatePathArgument(meter, "myMethod", "path");
        assertNoEvents(logger);
    }

    @Test
    void validatePathArgument_whenNull() {
        MeterValidator.validatePathArgument(meter, "myMethod", null);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.myMethod: Null argument; id=test-id");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void logBug() {
        Throwable t = new RuntimeException("bug");
        MeterValidator.logBug(meter, "testMethod", t);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.BUG, "Meter.testMethod method threw exception; id=test-id");
        assertEventWithThrowable(logger, 0, RuntimeException.class, "bug");
    }

    @Test
    void validateFinalize_whenNotStopped() {
        when(meter.getStopTime()).thenReturn(0L);
        when(meter.getCategory()).thenReturn("test-category");
        MeterValidator.validateFinalize(meter);
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INCONSISTENT_FINALIZED, "Meter started and never stopped; id={}");
        assertEventWithThrowable(logger, 0, CallerStackTraceThrowable.class);
    }

    @Test
    void validateFinalize_whenAlreadyStopped() {
        when(meter.getStopTime()).thenReturn(1L);
        MeterValidator.validateFinalize(meter);
        assertNoEvents(logger);
    }

    @Test
    void validateFinalize_whenUnknownLogger() {
        when(meter.getStopTime()).thenReturn(0L);
        when(meter.getCategory()).thenReturn(Meter.UNKNOWN_LOGGER_NAME);
        MeterValidator.validateFinalize(meter);
        assertNoEvents(logger);
    }
}
