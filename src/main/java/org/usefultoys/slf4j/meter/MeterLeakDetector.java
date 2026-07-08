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
import java.util.concurrent.atomic.AtomicBoolean;

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
 * on the caller's own thread, from every meter lifecycle boundary — both {@link #register(Meter) start} and
 * {@link #deregister(MeterReference) termination} — so a forgotten meter is reported the next time any meter
 * is started <em>or</em> stopped anywhere in the application, with no library-owned daemon thread and no risk
 * of pinning a web application class loader in a servlet container. Lifecycle-triggered drains are bounded:
 * they report at most {@link #MAX_DRAIN} leaks per call and let at most one thread continue past the first
 * pending reference, so an application thread never absorbs unbounded reporting latency (or queue-lock pile-up)
 * on behalf of the detector. For an application that has gone quiet on meter activity,
 * {@link Meter#drainLeaks()} exposes the exhaustive {@link #drainAll()} publicly so a periodic driver (a scheduled
 * task, a health check, or a {@code Watcher} tick) can flush pending leaks on its own cadence. The one residual
 * gap — no meter activity and no external driver at all — leaves the last leaks unreported until activity
 * resumes. This is a narrow regression from the former {@code finalize()} path, which the GC drove without any
 * application activity; it is accepted because a fully idle state is rare and {@link Meter#drainLeaks()} covers it.
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
     * Claimed by the thread that continues draining beyond the first pending reference, so concurrent
     * lifecycle calls do not pile up on the queue's internal lock while a batch of references is pending.
     * Never touched while the queue is empty — the steady-state fast path performs no shared write.
     */
    private static final AtomicBoolean DRAINING = new AtomicBoolean();

    /**
     * Upper bound of leak reports emitted per opportunistic {@link #drain()} call. The dominant per-reference
     * cost is the ERROR logging in {@link MeterReference#reportLeak()} (appender I/O), so this bounds the
     * latency a lifecycle call ({@code start()} or a stop) can absorb on behalf of the detector. Remaining
     * references are picked up by subsequent lifecycle calls or by {@link #drainAll()}.
     * Package-private for tests.
     */
    static final int MAX_DRAIN = 8;

    /**
     * A {@link PhantomReference} to a started {@link Meter} that snapshots the data required to report
     * a leak after the referent has been collected.
     * <p>
     * The snapshot stores the {@code Meter}'s identifying <em>components</em> ({@code category},
     * {@code operation}, {@code position}) rather than the pre-rendered {@link Meter#getFullID()
     * fullID} string. Once the referent has been collected the meter is no longer queryable, so any
     * data needed to report the leak must be copied eagerly at registration; but the {@code fullID}
     * {@link String} concatenation itself is deferred to {@link #reportLeak()}, where it only runs
     * when a leak is actually confirmed. With {@link MeterConfig#detectLeaks} enabled by default,
     * {@link #register(Meter) register()} runs in the {@code start()} hot path of every meter whose
     * category is known, and the previous eager concatenation was paid there on every start, only
     * to be thrown away on {@link #deregister(MeterReference)} for every correctly-stopped meter.
     * Deferring it removes that string allocation from the start path entirely. The component
     * fields are the same {@code String}s the {@code Meter} already retains ({@code category},
     * {@code operation}) plus a primitive {@code long position}, so the retained footprint is
     * equivalent while the read cost at registration drops to two reference reads and one
     * primitive read. {@code category}, {@code operation} and {@code position} are stable from
     * construction onward, so the deferred concatenation still produces the same byte-for-byte
     * {@code fullID} the eager snapshot did.
     */
    static final class MeterReference extends PhantomReference<Meter> {
        private final String category;
        private final String operation;
        private final long position;
        private final Logger messageLogger;

        MeterReference(final Meter meter, final ReferenceQueue<Meter> queue) {
            super(meter, queue);
            this.category = meter.getCategory();
            this.operation = meter.getOperation();
            this.position = meter.getPosition();
            this.messageLogger = meter.getMessageLogger();
        }

        /**
         * Emits the forgotten-meter error, byte-for-byte equivalent to the former
         * {@code MeterValidator.validateFinalize} message. The {@code fullID} is assembled here
         * from the snapshotted components, using the same formula as
         * {@link MeterData#getFullID()}, so it only runs when a leak is actually surfaced —
         * never on the {@code start()} hot path and never for a correctly stopped meter.
         */
        void reportLeak() {
            final String fullID = operation == null
                    ? category + '#' + position
                    : category + '/' + operation + '#' + position;
            messageLogger.error(Markers.INVALID_ARGUMENT,
                    "{}; id={}",
                    "Meter never stopped, must remember to call ok/reject/fail/success() on each started one",
                    fullID);
        }
    }

    /**
     * Registers a started meter for leak detection when enabled and the meter's category is known.
     * Drains pending leaks first (opportunistic). The returned handle must be passed to
     * {@link #deregister(MeterReference)} when the meter is stopped.
     *
     * @param meter the meter that has just been started; must not be {@code null}.
     * @return the registration handle to retain and pass to {@link #deregister(MeterReference)},
     *         or {@code null} when leak detection is disabled or the category is unknown.
     */
    static MeterReference register(final Meter meter) {
        if (!MeterConfig.detectLeaks || Meter.UNKNOWN_LOGGER_NAME.equals(meter.getCategory())) {
            return null;
        }
        drain();
        final MeterReference ref = new MeterReference(meter, QUEUE);
        ANCHOR.add(ref);
        return ref;
    }

    /**
     * Deregisters a meter that was stopped explicitly, so its eventual collection is never reported as a leak.
     * Idempotent and {@code null}-safe. Removing the reference from {@link #ANCHOR} drops the independent
     * strong path, and {@link MeterReference#clear()} prevents the GC from enqueueing it in the first place.
     * <p>
     * A non-{@code null} handle also drains the queue (opportunistically), so a leak surfaces on the next
     * meter <em>termination</em>, not only on the next {@link #register(Meter) start}. This widens the drain
     * trigger to every lifecycle boundary and keeps {@link #ANCHOR} bounded whenever meters keep being stopped,
     * while adding no cost when leak detection is disabled (a disabled meter never registers, so {@code ref}
     * is {@code null} and this returns before draining).
     *
     * @param ref the handle returned by {@link #register(Meter)}, or {@code null}.
     */
    static void deregister(final MeterReference ref) {
        if (ref == null) {
            return;
        }
        drain();
        if (ANCHOR.remove(ref)) {
            ref.clear();
        }
    }

    /**
     * Opportunistically drains the reference queue, reporting at most {@link #MAX_DRAIN} forgotten meters,
     * and letting at most one thread continue past the first pending reference. Invoked by
     * {@link #register(Meter)} and {@link #deregister(MeterReference)}; safe to call from any thread.
     * <p>
     * <b>Steady-state cost (empty queue, the no-leak case):</b> a single volatile read inside
     * {@link ReferenceQueue#poll()} — no lock, no CAS, no shared write. This is the hot path and is
     * intentionally identical to a bare {@code poll()}.
     * <p>
     * <b>When references are pending</b> (only possible after the GC collected meters that were never
     * stopped): {@code poll()} briefly takes the JDK's internal per-queue lock per reference. The
     * {@link #DRAINING} guard keeps concurrent lifecycle calls from queueing up on that lock — losers of
     * the CAS return immediately and leave the remainder to the winning thread or a later call — and
     * {@link #MAX_DRAIN} bounds how much reporting latency a single application thread absorbs. Leaks may
     * therefore surface across a few lifecycle calls instead of one; {@link #drainAll()} remains exhaustive
     * for periodic drivers.
     */
    static void drain() {
        // The first poll doubles as the emptiness check: lock-free volatile read when empty.
        Reference<? extends Meter> r = QUEUE.poll();
        if (r == null) {
            return;
        }
        // This reference is already claimed off the queue; report it regardless of who is draining.
        reportIfAnchored(r);
        // Only one thread continues past the first reference; others yield instead of
        // piling up on the queue's internal lock.
        if (DRAINING.compareAndSet(false, true)) {
            try {
                for (int i = 1; i < MAX_DRAIN; i++) {
                    r = QUEUE.poll();
                    if (r == null) {
                        return;
                    }
                    reportIfAnchored(r);
                }
            } finally {
                DRAINING.set(false);
            }
        }
    }

    /**
     * Exhaustively drains the reference queue. Backing implementation of {@link Meter#drainLeaks()}:
     * meant for periodic drivers (a {@code Watcher} tick, a scheduled task, a health check) that
     * deliberately volunteer to do the full cleanup, so it is not capped by {@link #MAX_DRAIN}.
     */
    static void drainAll() {
        Reference<? extends Meter> r;
        while ((r = QUEUE.poll()) != null) {
            reportIfAnchored(r);
        }
    }

    /**
     * Discards all leak-detector state without reporting anything. Intended for test isolation only;
     * it clears the anchor set and drains the reference queue silently, and resets the draining guard.
     * <p>
     * Production code must never call this — it would hide real memory leaks from the diagnostic output.
     */
    static void clearForTests() {
        ANCHOR.clear();
        while (QUEUE.poll() != null) {
            // discard pending references without reporting
        }
        DRAINING.set(false);
    }

    /**
     * Reports the reference as a forgotten meter if it was still anchored (i.e. never deregistered).
     * The atomic {@link Set#remove(Object)} claims each reference exactly once across concurrent drains.
     */
    private static void reportIfAnchored(final Reference<? extends Meter> r) {
        final MeterReference ref = (MeterReference) r;
        if (ANCHOR.remove(ref)) {
            try {
                ref.reportLeak();
            } catch (final Exception ignored) {
                // Leak reporting is a diagnostic aid; a misbehaving logging backend must never
                // disturb the application thread that happened to trigger the drain.
            }
        }
    }
}
