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
 *   <li><b>List mechanics:</b> {@code register}/{@code deregister} keep the intrusive list consistent and idempotent.</li>
 *   <li><b>Null safety:</b> {@code deregister(null)} and draining an empty queue are no-ops.</li>
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
        final MeterReference ref = MeterLeakDetector.register(meter);
        try {
            assertNotNull(ref, "register should return a non-null handle");
            assertEquals(1, MeterLeakDetector.trackedCount(), "exactly one reference should be tracked after register");
            assertNoEvents(logger);
        } finally {
            MeterLeakDetector.deregister(ref);
        }
    }

    @Test
    @DisplayName("deregister should stop tracking the reference")
    void deregisterStopsTracking() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        assertEquals(0, MeterLeakDetector.trackedCount(), "no references should be tracked after deregister");
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should be idempotent")
    void deregisterIsIdempotent() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        MeterLeakDetector.deregister(ref);
        assertEquals(0, MeterLeakDetector.trackedCount(), "double deregister should not corrupt the list");
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should tolerate a null handle")
    void deregisterToleratesNull() {
        MeterLeakDetector.deregister(null);
        assertEquals(0, MeterLeakDetector.trackedCount(), "deregister(null) must be a no-op");
    }

    @Test
    @DisplayName("multiple registrations should be tracked and individually deregistered")
    void multipleRegistrationsAreTrackedIndependently() {
        final MeterReference ref1 = MeterLeakDetector.register(meter);
        final MeterReference ref2 = MeterLeakDetector.register(meter);
        final MeterReference ref3 = MeterLeakDetector.register(meter);
        try {
            assertEquals(3, MeterLeakDetector.trackedCount(), "three references should be tracked");
            MeterLeakDetector.deregister(ref2);
            assertEquals(2, MeterLeakDetector.trackedCount(), "deregistering a middle node keeps the list consistent");
            MeterLeakDetector.deregister(ref1);
            assertEquals(1, MeterLeakDetector.trackedCount(), "deregistering the tail node keeps the list consistent");
            MeterLeakDetector.deregister(ref3);
            assertEquals(0, MeterLeakDetector.trackedCount(), "deregistering the head node empties the list");
        } finally {
            MeterLeakDetector.deregister(ref1);
            MeterLeakDetector.deregister(ref2);
            MeterLeakDetector.deregister(ref3);
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
        MeterLeakDetector.register(meter);
        MeterLeakDetector.register(meter);
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
        MeterLeakDetector.register(meter);
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
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        MeterLeakDetector.reportRemainingLeaks();
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("enabling reportLeaksOnShutdown installs a shutdown hook at most once")
    void enablingReportLeaksOnShutdownInstallsHookOnce() {
        MeterConfig.reportLeaksOnShutdown = true;
        final MeterReference ref1 = MeterLeakDetector.register(meter);
        try {
            final Thread hook = MeterLeakDetector.shutdownHookForTests();
            assertNotNull(hook, "a shutdown hook must be installed when reportLeaksOnShutdown is enabled");
            final MeterReference ref2 = MeterLeakDetector.register(meter);
            assertSame(hook, MeterLeakDetector.shutdownHookForTests(), "the hook must be installed at most once");
            MeterLeakDetector.deregister(ref2);
        } finally {
            MeterLeakDetector.deregister(ref1);
            MeterLeakDetector.resetShutdownHookForTests();
        }
    }

    @Test
    @DisplayName("no shutdown hook is installed when reportLeaksOnShutdown is disabled")
    void disabledReportLeaksOnShutdownInstallsNoHook() {
        // reportLeaksOnShutdown defaults to false (restored by @ResetMeterConfig)
        final MeterReference ref = MeterLeakDetector.register(meter);
        try {
            assertNull(MeterLeakDetector.shutdownHookForTests(),
                    "no shutdown hook should be installed when the option is disabled");
        } finally {
            MeterLeakDetector.deregister(ref);
        }
    }
}
