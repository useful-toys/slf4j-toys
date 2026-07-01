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

import org.slf4j.Logger;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forward-compatible detector for {@link Meter} instances that were started but never explicitly stopped
 * (via {@code ok()}, {@code reject()}, {@code fail()} or {@code close()}).
 * <p>
 * This is the replacement for the deprecated {@link Object#finalize()} mechanism. It relies on
 * {@link PhantomReference} plus a {@link ReferenceQueue} — an API available on every Java version since Java 2 —
 * so it keeps working on Java 8 through future releases where finalization is removed.
 * <p>
 * <b>Why the anchor set is mandatory:</b> the garbage collector only enqueues a {@link PhantomReference} whose
 * <em>reference object</em> is itself reachable through a strong path that is independent of its referent. A
 * {@code MeterReference} held only by its own {@code Meter} (e.g. through a field on the meter) is collected
 * together with that meter and is <em>never</em> enqueued — so the leak would go unreported. {@link #ANCHOR}
 * keeps every live registration strongly reachable from a GC root, independently of the meter, until the meter
 * is either stopped (deregistered) or collected and drained.
 * <p>
 * <b>Design — no monitor on the hot path:</b> the anchor is a {@link ConcurrentHashMap#newKeySet() concurrent set},
 * so {@code register}/{@code deregister}/{@code drain} mutate it with CAS operations, never a library-owned
 * monitor lock. The atomic {@link Set#remove(Object)} also provides idempotency and de-duplication between
 * concurrent drains, so no separate {@code registered} flag is needed.
 * <p>
 * When a meter is stopped explicitly, {@link #deregister} both removes it from the anchor and calls
 * {@link MeterReference#clear()} — the latter prevents the GC from ever enqueueing a properly-stopped meter,
 * so {@link #drain()} never even sees it.
 * <p>
 * <b>Draining strategy — no background thread:</b> the {@link ReferenceQueue} is drained opportunistically
 * from {@link #register(Meter)}, i.e. on the thread that starts the next meter. A forgotten meter is
 * reported the next time any meter is started, with no library-owned daemon thread and no risk of pinning
 * a web application class loader in a servlet container.
 * <p>
 * <b>Predicate equivalence:</b> a meter is registered only from {@code start()} and is deregistered on
 * every explicit termination. A reference still anchored when it is enqueued is, by definition, a meter that
 * was started and never stopped — exactly the predicate the former {@code finalize()} path checked.
 *
 * @author Daniel Felix Ferber
 * @see Meter
 * @see MeterConfig#detectLeaks
 */
final class MeterLeakDetector {

    private MeterLeakDetector() {
        // Utility holder, not instantiable.
    }

    /** Queue onto which the garbage collector enqueues references whose meters became unreachable. */
    private static final ReferenceQueue<Meter> QUEUE = new ReferenceQueue<>();

    /**
     * Strongly holds every live registration, independently of the meter it points to. Without this anchor
     * a {@link MeterReference} reachable only through its own referent would be collected together with the
     * meter and never enqueued. Membership is the authoritative "still registered" state: {@link #deregister}
     * and {@link #drain} both use the atomic {@link Set#remove(Object)} to claim a reference exactly once.
     */
    private static final Set<MeterReference> ANCHOR = ConcurrentHashMap.newKeySet();

    /**
     * A {@link PhantomReference} to a started {@link Meter} that snapshots the data required to report
     * a leak after the referent has been collected.
     */
    static final class MeterReference extends PhantomReference<Meter> {
        private final String fullID;
        private final Logger messageLogger;

        MeterReference(final Meter meter, final ReferenceQueue<Meter> queue) {
            super(meter, queue);
            this.fullID = meter.getFullID();
            this.messageLogger = meter.getMessageLogger();
        }

        /**
         * Emits the forgotten-meter error, byte-for-byte equivalent to the former
         * {@code MeterValidator.validateFinalize}.
         */
        void reportLeak() {
            messageLogger.error(Markers.INVALID_ARGUMENT,
                    "{}; id={}",
                    "Meter never stopped, must remember to call ok/reject/fail/success() on each started one",
                    fullID);
        }
    }

    /**
     * Registers a started meter for leak detection. Drains pending leaks first (opportunistic).
     * The returned handle must be passed to {@link #deregister(MeterReference)} when the meter is stopped.
     *
     * @param meter the meter that has just been started; must not be {@code null}.
     * @return the registration handle to retain and pass to {@link #deregister(MeterReference)}.
     */
    static MeterReference register(final Meter meter) {
        drain();
        final MeterReference ref = new MeterReference(meter, QUEUE);
        ANCHOR.add(ref);
        return ref;
    }

    /**
     * Deregisters a meter that was stopped explicitly, so its eventual collection is never reported as a leak.
     * Idempotent and {@code null}-safe. Removing the reference from {@link #ANCHOR} drops the independent
     * strong path, and {@link MeterReference#clear()} prevents the GC from enqueueing it in the first place.
     *
     * @param ref the handle returned by {@link #register(Meter)}, or {@code null}.
     */
    static void deregister(final MeterReference ref) {
        if (ref == null) {
            return;
        }
        if (ANCHOR.remove(ref)) {
            ref.clear();
        }
    }

    /**
     * Drains the reference queue and reports every meter that was collected while still registered.
     * Lock-free; safe to call from any thread. Invoked opportunistically by {@link #register(Meter)}.
     */
    static void drain() {
        Reference<? extends Meter> r;
        while ((r = QUEUE.poll()) != null) {
            final MeterReference ref = (MeterReference) r;
            if (ANCHOR.remove(ref)) {
                ref.reportLeak();
            }
        }
    }
}
