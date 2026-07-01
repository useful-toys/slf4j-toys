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
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.MeterLeakDetector.MeterReference;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;

import java.lang.ref.ReferenceQueue;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvents;

/**
 * Unit tests for {@link MeterLeakDetector}.
 * <p>
 * These tests exercise the detector's logic deterministically, without relying on garbage collection.
 * Instead of a counter-based test seam (which would add overhead to production code), behavior is verified
 * through the observable output of {@link MeterReference#reportLeak()} and the deterministic
 * {@link java.lang.ref.Reference#enqueue()} API.
 * <ul>
 *   <li><b>Registration:</b> {@code register} returns a non-null handle; no side-effects are emitted.</li>
 *   <li><b>Deregistration:</b> after {@code deregister}, an explicitly enqueued reference is silently
 *       skipped by {@code drain()}: {@code deregister} removes it from the detector's anchor set (and calls
 *       {@link MeterReference#clear()}), so {@code drain} no longer claims it.</li>
 *   <li><b>Null safety:</b> {@code deregister(null)} is a no-op.</li>
 *   <li><b>Idempotency:</b> double {@code deregister} does not cause extra reports.</li>
 *   <li><b>Report contract:</b> {@link MeterReference#reportLeak()} emits the exact message and marker
 *       formerly produced by {@code MeterValidator.validateFinalize}.</li>
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
    @DisplayName("register should return a non-null handle without emitting events")
    void registerReturnsNonNullHandle() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        assertNotNull(ref, "register should return a non-null handle");
        assertNoEvents(logger);
        MeterLeakDetector.deregister(ref);
    }

    @Test
    @DisplayName("deregister should prevent the reference from being reported by drain")
    void deregisterStopsReporting() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        ref.enqueue();
        MeterLeakDetector.drain();
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should be idempotent: double deregister emits no extra reports")
    void deregisterIsIdempotent() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        MeterLeakDetector.deregister(ref);
        ref.enqueue();
        MeterLeakDetector.drain();
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("deregister should tolerate a null handle")
    void deregisterToleratesNull() {
        MeterLeakDetector.deregister(null);
        assertNoEvents(logger);
    }

    @Test
    @DisplayName("drain on an empty queue should be a no-op")
    void drainEmptyQueueIsNoOp() {
        MeterLeakDetector.drain();
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
    @DisplayName("drain should report a registered reference that was manually enqueued")
    void drainReportsEnqueuedRegisteredMeter() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        ref.enqueue();
        MeterLeakDetector.drain();
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("drain should silently skip a reference that was already deregistered")
    void drainIgnoresEnqueuedDeregisteredMeter() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        MeterLeakDetector.deregister(ref);
        ref.enqueue();
        MeterLeakDetector.drain();
        assertNoEvents(logger);
    }

}
