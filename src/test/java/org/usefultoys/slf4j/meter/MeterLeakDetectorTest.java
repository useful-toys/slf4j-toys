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
import org.slf4j.Marker;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.meter.MeterLeakDetector.MeterReference;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.ValidateCleanMeter;

import java.lang.ref.ReferenceQueue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

    @Test
    @DisplayName("deregister should drain, reporting a pending leak from another meter")
    void deregisterDrainsPendingLeaks() {
        // Given: one leaked meter (enqueued, still anchored) and another that is about to be stopped
        final MeterReference leaked = MeterLeakDetector.register(meter);
        final MeterReference other = MeterLeakDetector.register(meter);
        leaked.enqueue();

        // When: the other meter is deregistered (stopped) — no new meter is ever started
        MeterLeakDetector.deregister(other);

        // Then: deregister's drain reports the leaked one, even though start() was not called again
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("Meter.drainLeaks should drain and report a pending leak (public entry point)")
    void publicDrainLeaksReportsEnqueuedRegisteredMeter() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        ref.enqueue();

        Meter.drainLeaks();

        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("Watcher.run should drain and report a pending leak (periodic-driver wiring)")
    void watcherRunDrainsPendingLeaks() {
        final MeterReference ref = MeterLeakDetector.register(meter);
        ref.enqueue();

        new org.usefultoys.slf4j.watcher.Watcher("leak-drain-test").run();

        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

    @Test
    @DisplayName("drain should report at most MAX_DRAIN leaks per call, leaving the rest for later calls")
    void drainCapsReportsAtMaxDrain() {
        // Given: MAX_DRAIN + 2 leaked meters, all pending in the queue. Register everything before
        // enqueueing anything: register() itself drains, so interleaving would report early.
        final int pending = MeterLeakDetector.MAX_DRAIN + 2;
        final MeterReference[] refs = new MeterReference[pending];
        for (int i = 0; i < pending; i++) {
            refs[i] = MeterLeakDetector.register(meter);
        }
        for (final MeterReference ref : refs) {
            ref.enqueue();
        }

        // When: a single lifecycle-triggered drain runs
        MeterLeakDetector.drain();

        // Then: it reports exactly MAX_DRAIN leaks; a later drain picks up the remainder
        assertEquals(MeterLeakDetector.MAX_DRAIN, ((MockLogger) logger).getLoggerEvents().size(),
                "a lifecycle drain must report at most MAX_DRAIN leaks");
        MeterLeakDetector.drain();
        assertEquals(pending, ((MockLogger) logger).getLoggerEvents().size(),
                "the remaining leaks must survive in the queue and surface on the next drain");
    }

    @Test
    @DisplayName("drainAll (Meter.drainLeaks) should be exhaustive, ignoring the MAX_DRAIN cap")
    void drainAllIsExhaustiveBeyondCap() {
        final int pending = MeterLeakDetector.MAX_DRAIN + 2;
        final MeterReference[] refs = new MeterReference[pending];
        for (int i = 0; i < pending; i++) {
            refs[i] = MeterLeakDetector.register(meter);
        }
        for (final MeterReference ref : refs) {
            ref.enqueue();
        }

        Meter.drainLeaks();

        assertEquals(pending, ((MockLogger) logger).getLoggerEvents().size(),
                "the public periodic-driver entry point must flush every pending leak in one call");
    }

    @Test
    @DisplayName("drain should swallow a throwing logger and keep processing the remaining leaks")
    void drainSwallowsThrowingLoggerAndContinues() {
        // Given: one leaked meter whose snapshotted logger throws on error(), and one healthy leaked meter
        final Logger throwingLogger = mock(Logger.class);
        doThrow(new RuntimeException("boom")).when(throwingLogger)
                .error(any(Marker.class), anyString(), any(), any());
        final Meter poisoned = mock(Meter.class);
        lenient().when(poisoned.getMessageLogger()).thenReturn(throwingLogger);
        lenient().when(poisoned.getFullID()).thenReturn("poisoned-id");

        final MeterReference poisonedRef = MeterLeakDetector.register(poisoned);
        final MeterReference healthyRef = MeterLeakDetector.register(meter);
        poisonedRef.enqueue();
        healthyRef.enqueue();

        // When/Then: draining neither propagates the exception nor loses the healthy report
        assertDoesNotThrow(MeterLeakDetector::drain,
                "a misbehaving logging backend must never disturb the thread that triggered the drain");
        assertEvent(logger, 0, MockLoggerEvent.Level.ERROR, Markers.INVALID_ARGUMENT,
                "Meter never stopped, must remember to call ok/reject/fail/success() on each started one; id=test-id");
    }

}
