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
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCleanMeter;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.slf4j.impl.MockLoggerEvent.Level.ERROR;
import static org.usefultoys.slf4j.meter.Markers.INVALID_TRANSITION;

/**
 * Verifies the shared, process-wide null-object returned by {@link Meter#getCurrentInstance()} when no meter is
 * active on the current thread (see TDR-0042): it must never be allocated per call, must remain identical across
 * threads, and every mutating operation called on it must be a well-defined no-op that logs an
 * {@code INVALID_TRANSITION} instead of mutating shared state.
 *
 * @author Daniel Felix Ferber
 */
@ResetMeterConfig
@ValidateCleanMeter
@DisplayName("Meter.getCurrentInstance() shared unknown null-object")
class MeterUnknownInstanceTest {

    private MockLogger unknownLogger;

    @BeforeEach
    void resolveUnknownLogger() {
        /* The unknown Meter is a static singleton; fetch the very logger it reports to, rather than
           re-deriving the name from MeterConfig, which may have moved on since the singleton was built. */
        unknownLogger = (MockLogger) Meter.getCurrentInstance().getMessageLogger();
        unknownLogger.clearEvents();
    }

    @Test
    @DisplayName("returns the same instance on repeated calls with no active meter")
    void returnsSameInstanceOnRepeatedCalls() {
        final Meter first = Meter.getCurrentInstance();
        final Meter second = Meter.getCurrentInstance();
        assertSame(first, second, "getCurrentInstance() must not allocate a new dummy per call");
    }

    @Test
    @DisplayName("returns the same instance across threads")
    void returnsSameInstanceAcrossThreads() throws InterruptedException {
        final Meter mainThreadInstance = Meter.getCurrentInstance();
        final AtomicReference<Meter> otherThreadInstance = new AtomicReference<>();
        final Thread thread = new Thread(() -> otherThreadInstance.set(Meter.getCurrentInstance()));
        thread.start();
        thread.join();
        assertSame(mainThreadInstance, otherThreadInstance.get(),
                "the shared unknown instance must be the same object across threads");
    }

    @Test
    @DisplayName("reports the unknown category")
    void reportsUnknownCategory() {
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, Meter.getCurrentInstance().getCategory());
    }

    @Test
    @DisplayName("start() logs an invalid transition and does not become the current instance")
    void startLogsInvalidTransitionAndDoesNotBecomeCurrent() {
        final Meter unknown = Meter.getCurrentInstance();

        final Meter result = unknown.start();

        assertSame(unknown, result, "start() on the unknown meter must be a no-op returning itself");
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
        assertSame(unknown, Meter.getCurrentInstance(),
                "the unknown meter must not push itself onto the thread-local stack");
    }

    @Test
    @DisplayName("m(String) logs an invalid transition")
    void mWithMessageLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.m("should be ignored");
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("m(String, Object...) logs an invalid transition")
    void mWithFormatLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.m("value={}", 42);
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("mf(String, Object...) logs an invalid transition")
    void mfWithFormatLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.mf("value=%d", 42);
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("ctx(String, Object) logs an invalid transition and does not store context")
    void ctxLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        unknown.ctx("key", "value");
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("ok() logs an invalid transition and never records a stop time")
    void okLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.ok();
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
        assertEquals(0, unknown.getStopTime(), "the unknown meter must never record a stop time");
    }

    @Test
    @DisplayName("reject(Object) logs an invalid transition")
    void rejectLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.reject("cause");
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("fail(Object) logs an invalid transition")
    void failLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.fail("cause");
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("close() logs an invalid transition")
    void closeLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        unknown.close();
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("limitMilliseconds(long) logs an invalid transition")
    void limitMillisecondsLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.limitMilliseconds(100);
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("iterations(long) logs an invalid transition")
    void iterationsLogsInvalidTransition() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.iterations(10);
        assertSame(unknown, result);
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("sub(String) logs an invalid transition and returns itself, not a new sub-meter")
    void subLogsInvalidTransitionAndReturnsSelf() {
        final Meter unknown = Meter.getCurrentInstance();
        final Meter result = unknown.sub("child");
        assertSame(unknown, result, "sub() on the unknown meter must not allocate a new sub-meter");
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
    }

    @Test
    @DisplayName("reset() logs an invalid transition and does not clear the shared instance's state")
    void resetLogsInvalidTransitionAndDoesNotClearState() {
        final Meter unknown = Meter.getCurrentInstance();
        unknown.reset();
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, unknown.getCategory(),
                "reset() must not null out the shared instance's category");
    }

    @Test
    @DisplayName("readJson5(String) logs an invalid transition and does not repopulate the shared instance")
    void readJson5LogsInvalidTransitionAndDoesNotMutateState() {
        final Meter unknown = Meter.getCurrentInstance();
        unknown.readJson5("{category:'hijacked',startTime:123}");
        unknownLogger.assertEvent(0, ERROR, INVALID_TRANSITION);
        assertEquals(Meter.UNKNOWN_LOGGER_NAME, unknown.getCategory(),
                "readJson5() must not overwrite the shared instance's category");
        assertEquals(0, unknown.getStartTime(), "readJson5() must not populate the shared instance's startTime");
    }
}
