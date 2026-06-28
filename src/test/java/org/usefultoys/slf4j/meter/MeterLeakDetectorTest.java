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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.MeterLeakDetector.MeterReference;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;

import java.lang.ref.ReferenceQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvents;

/**
 * Unit tests for {@link MeterLeakDetector}.
 * <p>
 * These tests exercise the detector's logic deterministically, without relying on garbage collection:
 * <ul>
 *   <li><b>List mechanics:</b> {@code track}/{@code untrack} keep the intrusive registry consistent and idempotent.</li>
 *   <li><b>Null safety:</b> {@code untrack(null)} and draining an empty queue are no-ops.</li>
 *   <li><b>Stack-node unification:</b> {@code track} chains {@code previousInstance} and, when leak detection is off
 *       or the category is the dummy {@code "???"}, still returns a usable stack node without registering it.</li>
 *   <li><b>Report contract:</b> {@link MeterReference#reportLeak()} emits the exact message and marker formerly
 *       produced by {@code MeterValidator.validateFinalize}, from the snapshot captured at registration.</li>
 * </ul>
 * The end-to-end garbage-collection path (collection → enqueue → {@code drain} → report) is covered by
 * {@code MeterThreadLocalWeakReferenceGcTest}.
 */
@ValidateCharset
@ResetMeterConfig
@WithMockLogger
@ValidateCleanMeter
class MeterLeakDetectorTest {

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

    @Test
    @DisplayName("register should return a handle and track exactly one reference")
    void registerTracksOneReference() {
        assertEquals(0, MeterLeakDetector.trackedCount(), "no references should be tracked initially");
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        try {
            assertNotNull(ref, "register should return a non-null handle");
            assertEquals(1, MeterLeakDetector.trackedCount(), "exactly one reference should be tracked after register");
            assertNoEvents(logger);
        } finally {
            MeterLeakDetector.untrack(ref);
        }
    }

    @Test
    @DisplayName("deregister should stop tracking the reference")
    void deregisterStopsTracking() {
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        MeterLeakDetector.untrack(ref);
        assertEquals(0, MeterLeakDetector.trackedCount(), "no references should be tracked after deregister");
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should be idempotent")
    void deregisterIsIdempotent() {
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        MeterLeakDetector.untrack(ref);
        MeterLeakDetector.untrack(ref);
        assertEquals(0, MeterLeakDetector.trackedCount(), "double deregister should not corrupt the list");
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should tolerate a null handle")
    void deregisterToleratesNull() {
        MeterLeakDetector.untrack(null);
        assertEquals(0, MeterLeakDetector.trackedCount(), "deregister(null) must be a no-op");
    }

    @Test
    @DisplayName("multiple registrations should be tracked and individually deregistered")
    void multipleRegistrationsAreTrackedIndependently() {
        final MeterReference ref1 = MeterLeakDetector.track(meter, null);
        final MeterReference ref2 = MeterLeakDetector.track(meter, null);
        final MeterReference ref3 = MeterLeakDetector.track(meter, null);
        try {
            assertEquals(3, MeterLeakDetector.trackedCount(), "three references should be tracked");
            MeterLeakDetector.untrack(ref2);
            assertEquals(2, MeterLeakDetector.trackedCount(), "deregistering a middle node keeps the list consistent");
            MeterLeakDetector.untrack(ref1);
            assertEquals(1, MeterLeakDetector.trackedCount(), "deregistering the tail node keeps the list consistent");
            MeterLeakDetector.untrack(ref3);
            assertEquals(0, MeterLeakDetector.trackedCount(), "deregistering the head node empties the list");
        } finally {
            MeterLeakDetector.untrack(ref1);
            MeterLeakDetector.untrack(ref2);
            MeterLeakDetector.untrack(ref3);
        }
    }

    @Test
    @DisplayName("drain on an empty queue should be a no-op")
    void drainEmptyQueueIsNoOp() {
        MeterLeakDetector.drain();
        assertEquals(0, MeterLeakDetector.trackedCount(), "draining an empty queue should not change tracking");
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("reportLeak should emit the forgotten-meter error from the captured snapshot")
    void reportLeakEmitsForgottenMeterError() {
        final MeterReference ref = new MeterReference(meter, new ReferenceQueue<Meter>());
        ref.reportLeak();
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("reportRemainingLeaks should report every still-registered meter and empty the registry")
    void reportRemainingLeaksReportsAllRegistered() {
        MeterLeakDetector.track(meter, null);
        MeterLeakDetector.track(meter, null);
        assertEquals(2, MeterLeakDetector.trackedCount(), "both meters should be tracked before the sweep");

        MeterLeakDetector.reportRemainingLeaks();

        assertEquals(0, MeterLeakDetector.trackedCount(), "the registry must be empty after the sweep");
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
        assertEvent(logger, 1, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("reportRemainingLeaks should be idempotent and not double-report")
    void reportRemainingLeaksIsIdempotent() {
        MeterLeakDetector.track(meter, null);
        MeterLeakDetector.reportRemainingLeaks();
        MeterLeakDetector.reportRemainingLeaks();
        assertEquals(0, MeterLeakDetector.trackedCount(), "registry must stay empty");
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
        assertEquals(1, ((MockLogger) logger).getLoggerEvents().size(),
                "a forgotten meter must be reported exactly once across repeated sweeps");
    }

    @Test
    @DisplayName("reportRemainingLeaks on an empty registry should be a no-op")
    void reportRemainingLeaksEmptyIsNoOp() {
        MeterLeakDetector.reportRemainingLeaks();
        assertEquals(0, MeterLeakDetector.trackedCount());
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("a deregistered meter is not reported by the shutdown sweep")
    void deregisteredMeterIsNotReportedOnShutdownSweep() {
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        MeterLeakDetector.untrack(ref);
        MeterLeakDetector.reportRemainingLeaks();
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("enabling reportLeaksOnShutdown installs a shutdown hook at most once")
    void enablingReportLeaksOnShutdownInstallsHookOnce() {
        MeterConfig.reportLeaksOnShutdown = true;
        final MeterReference ref1 = MeterLeakDetector.track(meter, null);
        try {
            final Thread hook = MeterLeakDetector.shutdownHookForTests();
            assertNotNull(hook, "a shutdown hook must be installed when reportLeaksOnShutdown is enabled");
            final MeterReference ref2 = MeterLeakDetector.track(meter, null);
            assertSame(hook, MeterLeakDetector.shutdownHookForTests(), "the hook must be installed at most once");
            MeterLeakDetector.untrack(ref2);
        } finally {
            MeterLeakDetector.untrack(ref1);
            MeterLeakDetector.resetShutdownHookForTests();
        }
    }

    @Test
    @DisplayName("no shutdown hook is installed when reportLeaksOnShutdown is disabled")
    void disabledReportLeaksOnShutdownInstallsNoHook() {
        // reportLeaksOnShutdown defaults to false (restored by @ResetMeterConfig)
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        try {
            assertNull(MeterLeakDetector.shutdownHookForTests(),
                    "no shutdown hook should be installed when the option is disabled");
        } finally {
            MeterLeakDetector.untrack(ref);
        }
    }

    @Test
    @DisplayName("track chains previousInstance and untrack returns it as the new stack top")
    void trackChainsPreviousInstanceAndUntrackRestoresIt() {
        final MeterReference first = MeterLeakDetector.track(meter, null);
        final MeterReference second = MeterLeakDetector.track(meter, first);
        try {
            assertSame(first, MeterLeakDetector.untrack(second),
                    "untrack must return the previous reference so the caller can restore the stack top");
            assertNull(MeterLeakDetector.untrack(first),
                    "untracking the bottom reference must return null (empty stack)");
        } finally {
            MeterLeakDetector.untrack(first);
            MeterLeakDetector.untrack(second);
        }
    }

    @Test
    @DisplayName("track does not register or report when detectLeaks is disabled, but still yields a stack node")
    void trackWithDetectLeaksDisabledCreatesPlainNode() {
        MeterConfig.detectLeaks = false;
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        assertNotNull(ref, "a stack node must be returned even when leak detection is off");
        assertSame(meter, ref.get(), "the stack node must weakly reference the meter so the stack can resolve it");
        assertEquals(0, MeterLeakDetector.trackedCount(), "a non-detecting node must not be registered");
        MeterLeakDetector.reportRemainingLeaks();
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("track does not register the dummy '???' meter")
    void trackExcludesUnknownCategoryMeter() {
        lenient().when(meter.getCategory()).thenReturn(Meter.UNKNOWN_LOGGER_NAME);
        final MeterReference ref = MeterLeakDetector.track(meter, null);
        assertNotNull(ref, "a stack node must still be returned for the dummy meter");
        assertEquals(0, MeterLeakDetector.trackedCount(), "the dummy '???' meter must not be registered for leaks");
        MeterLeakDetector.reportRemainingLeaks();
        assertNoEvents(logger);
    }
}
