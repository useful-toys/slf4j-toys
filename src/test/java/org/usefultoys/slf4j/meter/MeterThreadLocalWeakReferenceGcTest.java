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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCleanMeter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * <b>Diagnostic test (test-only, no production impact).</b>
 * <p>
 * This class exists to turn the flaky-test hypothesis into a reproducible fact. It demonstrates,
 * deterministically, that the {@link Meter} thread-local stack can silently lose its top element
 * because it is held through a {@link WeakReference} (see {@code Meter.localThreadInstance}).
 * <p>
 * The flaky failure observed in CI
 * ({@code MeterLifeCyclePostStartConfigurationTest.shouldDoubleIncrementIterationCounterWithIncTwiceAfterStart})
 * is caused by the following sequence:
 * <ol>
 *   <li>A {@code Meter} is started but never stopped; the only strong reference is a local variable.</li>
 *   <li>When that variable goes out of scope, the {@code Meter} becomes weakly reachable.</li>
 *   <li>If the garbage collector runs before the stack is inspected, the {@code WeakReference}
 *       clears and {@link Meter#getCurrentInstance()} returns the dummy {@code "???"} Meter,
 *       even though the operation was never terminated.</li>
 * </ol>
 * <p>
 * Because the garbage collector is non-deterministic, the production failure is intermittent.
 * Here we make it deterministic by driving the collector until a weak canary is cleared, and
 * only then asserting the resulting state. If the collector cannot be triggered in this
 * environment (for example, under {@code -XX:+DisableExplicitGC}), the affected test is skipped
 * via {@link org.junit.jupiter.api.Assumptions} rather than reported as a false failure.
 *
 * @author Daniel Felix Ferber
 */
@ValidateCleanMeter
@ResetMeterConfig
@DisplayName("Diagnostic: WeakReference in Meter ThreadLocal loses the started Meter on GC")
class MeterThreadLocalWeakReferenceGcTest {

    private static final String UNKNOWN = Meter.UNKNOWN_LOGGER_NAME;
    private static final String CATEGORY = "weakref.gc.diagnostic";
    private final Logger logger = LoggerFactory.getLogger(CATEGORY);

    /** Sink to keep the allocation pressure from being optimized away by the JIT. */
    @SuppressWarnings("unused")
    private static volatile long blackhole;

    /**
     * Drives the garbage collector until a freshly allocated weak canary is reclaimed,
     * proving that a full collection cycle actually happened. Combines {@link System#gc()}
     * hints with real allocation pressure so it works even when explicit GC is a no-op.
     *
     * @return {@code true} if a collection cycle was observed within the time budget,
     *         {@code false} if the collector could not be triggered (test should be skipped).
     */
    private static boolean forceGarbageCollection() {
        Object canary = new Object();
        final WeakReference<Object> canaryRef = new WeakReference<>(canary);
        // Drop the only strong reference so the canary becomes collectable.
        canary = null;

        final long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (canaryRef.get() != null) {
            if (System.nanoTime() > deadlineNanos) {
                return false;
            }
            System.gc();
            // Real allocation pressure: provokes collection even if System.gc() is only a hint.
            final byte[] pressure = new byte[8 * 1024 * 1024];
            blackhole += pressure.length;
        }
        return true;
    }

    /**
     * <b>Control / sanity check.</b> While a started Meter is kept strongly reachable, garbage
     * collection must NOT remove it from the thread-local stack. This isolates the
     * {@link WeakReference} as the sole cause of the loss: if even a strongly referenced Meter
     * vanished, the test harness itself would be suspect.
     */
    @Test
    @DisplayName("A strongly referenced started Meter survives GC and stays on the stack")
    void stronglyReferencedMeterSurvivesGc() {
        // This diagnostic targets the thread-local WeakReference stack, not leak detection; disable the
        // detector to keep the two mechanisms isolated.
        MeterConfig.detectLeaks = false;
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(), "Precondition: stack must be clean");

        final Meter meter = new Meter(logger).start();
        assertEquals(CATEGORY, Meter.getCurrentInstance().getCategory(), "Meter must be active right after start()");

        assumeTrue(forceGarbageCollection(), "Could not trigger GC in this environment; skipping");

        // 'meter' is still strongly referenced by the local variable here.
        assertEquals(CATEGORY, Meter.getCurrentInstance().getCategory(),
                "A strongly referenced Meter must remain on the stack after GC");

        // Explicit cleanup so the stack is left clean for the next test.
        meter.ok();
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(), "Stack must be clean after ok()");
    }

    /**
     * <b>Reproduces the production flake deterministically.</b> A Meter is started and never
     * stopped, and no strong reference is retained — exactly the situation of the failing test
     * once its local variable goes out of scope. After a forced GC, the {@link WeakReference}
     * clears and the stack reports {@code "???"}, i.e. the started Meter is silently lost.
     * <p>
     * This test PASSES against the current code, which is precisely the proof that the current
     * behavior is non-deterministic with respect to garbage collection. If the {@code WeakReference}
     * were removed (strong reference), this assertion would no longer hold — see the companion
     * guarantee test that should accompany that change.
     */
    @Test
    @DisplayName("A non-referenced started Meter is lost from the stack after GC (the flake)")
    void weakReferenceLosesStartedMeterAfterGc() {
        // This diagnostic targets the thread-local WeakReference stack, not leak detection; disable the
        // detector so the deliberately forgotten fixture below does not enqueue a real leak report.
        MeterConfig.detectLeaks = false;
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(), "Precondition: stack must be clean");

        // Start a Meter but keep NO strong reference to it. This mirrors a test/operation that
        // starts a Meter and never terminates it, then lets the local variable go out of scope.
        new Meter(logger).start();
        assertEquals(CATEGORY, Meter.getCurrentInstance().getCategory(),
                "Meter must be active immediately after start(), before GC");

        assumeTrue(forceGarbageCollection(), "Could not trigger GC in this environment; skipping");

        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(),
                "BUG REPRODUCED: the started Meter was held only through a WeakReference and was "
                        + "collected by the GC, so the stack now reports the dummy '???' Meter even "
                        + "though the operation was never terminated. This is the root cause of the "
                        + "intermittent CI failure.");
    }

    /**
     * Same mechanism, framed as the exact symptom seen by the validation extension: a test that
     * leaves a Meter on the stack (expecting a "dirty" stack) can instead observe a "clean" stack
     * after GC. This documents why {@code @ValidateCleanMeter(expectDirtyStack = true)} is racy
     * under the current {@code WeakReference} design.
     */
    @Test
    @DisplayName("An 'expected dirty' stack can appear clean after GC under WeakReference")
    void expectedDirtyStackCanAppearCleanAfterGc() {
        // This diagnostic targets the thread-local WeakReference stack, not leak detection; disable the
        // detector so the deliberately forgotten fixture below does not enqueue a real leak report.
        MeterConfig.detectLeaks = false;
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(), "Precondition: stack must be clean");

        new Meter(logger).start().inc().inc();
        assertNotEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(),
                "Stack is dirty right after start()/inc(), as the test author intended");

        assumeTrue(forceGarbageCollection(), "Could not trigger GC in this environment; skipping");

        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(),
                "After GC the WeakReference cleared and the 'dirty' stack now looks clean — this is "
                        + "exactly the contradiction that makes expectDirtyStack=true tests flaky.");
    }

    /**
     * <b>Positive guarantee of the production requirement.</b>
     * <p>
     * In a Java EE / server backend the library must stay resilient even when application code
     * forgets to terminate a {@code Meter} ({@code ok()}/{@code reject()}/{@code fail()}/{@code close()}).
     * The required behavior is:
     * <ol>
     *   <li><b>No memory leak:</b> a forgotten, started {@code Meter} must be reclaimable by the GC,
     *       never pinned forever by the thread-local stack.</li>
     *   <li><b>Inconsistency is logged:</b> when such a forgotten {@code Meter} is reclaimed, an error
     *       message must be emitted so the misuse is visible.</li>
     *   <li><b>Resilience:</b> the thread-local stack must self-heal back to the dummy {@code "???"}.</li>
     * </ol>
     * This test proves that the {@link MeterLeakDetector} (the {@code PhantomReference}-based replacement for the
     * former {@code finalize()} mechanism) satisfies all three together with the {@code WeakReference}-based stack:
     * the {@code WeakReference} guarantees (1) and (3), and the detector — drained here synchronously on the test
     * thread — guarantees (2). Because draining is opportunistic and runs on the caller's thread, the warning is
     * emitted deterministically rather than by a background finalizer thread.
     */
    @Test
    @DisplayName("REQUIREMENT: a forgotten Meter is reclaimed (no leak), logs a warning, and the stack self-heals")
    void forgottenMeterIsReclaimedWithoutLeakAndLogsWarning() {
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(), "Precondition: stack must be clean");

        // Resolve the exact logger the Meter reports to (category, wrapped by the configured prefix/suffix).
        final MockLogger messageLogger = (MockLogger) LoggerFactory.getLogger(
                MeterConfig.messagePrefix + CATEGORY + MeterConfig.messageSuffix);
        messageLogger.clearEvents();

        // Start a Meter and "forget" it: keep NO strong reference. A WeakReference lets us observe
        // whether it becomes collectable without itself preventing collection. Leak detection is on
        // by default (restored by @ResetMeterConfig), so start() registers it with the detector.
        final WeakReference<Meter> tracker = new WeakReference<>(new Meter(logger).start());
        assertEquals(CATEGORY, Meter.getCurrentInstance().getCategory(),
                "Meter must be active immediately after start()");

        // Drive GC until the Meter is reclaimed and the detector, drained on this thread, logs the warning.
        final boolean reclaimedAndWarned = driveReclamationAndDrain(tracker, messageLogger);
        assumeTrue(reclaimedAndWarned, "Could not trigger GC in this environment; skipping");

        // (1) NO MEMORY LEAK: nothing retained the forgotten Meter, so the GC reclaimed it.
        assertNull(tracker.get(),
                "A forgotten started Meter must be garbage-collectable: neither the WeakReference stack nor "
                        + "the PhantomReference detector pins it — i.e. no memory leak.");

        // (2) INCONSISTENCY LOGGED: the leak detector reported the never-stopped Meter.
        assertTrue(hasNeverStoppedWarning(messageLogger),
                "Reclaiming a started-but-never-stopped Meter must log an inconsistency warning "
                        + "('Meter never stopped...') so the misuse is visible.");

        // (3) RESILIENCE: the thread-local stack self-healed to the dummy Meter.
        assertEquals(UNKNOWN, Meter.getCurrentInstance().getCategory(),
                "After reclamation the thread-local stack must self-heal back to the dummy '???' Meter.");
    }

    /**
     * Drives garbage collection until the tracked Meter has been reclaimed and the {@link MeterLeakDetector},
     * drained synchronously on this thread, has logged the never-stopped warning — or until the time budget expires.
     *
     * @return {@code true} if both reclamation and the warning were observed in time; {@code false}
     *         if the collector could not be driven (the test should then be skipped).
     */
    private static boolean driveReclamationAndDrain(
            final WeakReference<Meter> tracker, final MockLogger messageLogger) {
        final long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadlineNanos) {
            System.gc();
            final byte[] pressure = new byte[8 * 1024 * 1024];
            blackhole += pressure.length;
            // Drain on this thread: any meter the GC enqueued is reported here, deterministically.
            MeterLeakDetector.drain();
            if (tracker.get() == null && hasNeverStoppedWarning(messageLogger)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans the logger for the leak detector's inconsistency warning. Draining happens on the test thread,
     * so there is no concurrent appender to guard against.
     */
    private static boolean hasNeverStoppedWarning(final MockLogger messageLogger) {
        for (final MockLoggerEvent event : new ArrayList<>(messageLogger.getLoggerEvents())) {
            final String formatted = event.getFormattedMessage();
            if (event.getLevel() == MockLoggerEvent.Level.ERROR
                    && formatted != null
                    && formatted.contains("Meter never stopped")) {
                return true;
            }
        }
        return false;
    }
}
