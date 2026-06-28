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

/**
 * Forward-compatible detector for {@link Meter} instances that were started but never explicitly stopped
 * (via {@code ok()}, {@code reject()}, {@code fail()} or {@code close()}).
 * <p>
 * This is the replacement for the deprecated {@link Object#finalize()} mechanism. It relies on
 * {@link PhantomReference} plus a {@link ReferenceQueue} — an API available on every Java version since Java 2 —
 * so it keeps working on Java 8 through future releases where finalization is removed.
 * <p>
 * <b>Design — at most one extra object per started meter:</b> each started meter that opts in registers exactly one
 * {@link MeterReference}. That reference <em>is</em> the node of an intrusive doubly-linked list (it carries its own
 * {@code prev}/{@code next} pointers), so no separate container node is allocated — unlike a {@code Set} or
 * {@code Map} backed registry. This mirrors what {@code java.lang.ref.Cleaner} does internally.
 * <p>
 * <b>Draining strategy — no background thread:</b> the {@link ReferenceQueue} is drained opportunistically from
 * {@link #register(Meter)}, i.e. on the thread that starts the next meter. A forgotten meter is therefore reported
 * the next time any meter is started, with no library-owned daemon thread and no risk of pinning a web application
 * class loader in a servlet container.
 * <p>
 * <b>Predicate equivalence:</b> a meter is registered only from {@code start()} (so {@code startTime != 0} is implied)
 * and is deregistered on every explicit termination. Therefore a reference that the garbage collector enqueues
 * <em>while still registered</em> is, by definition, a meter that was started and never stopped — exactly the
 * predicate the former {@code finalize()} path checked. The {@code UNKNOWN_LOGGER_NAME} exclusion and the
 * {@link MeterConfig#detectLeaks} toggle are enforced by the caller, keeping this class a pure mechanism.
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

    /** Guards the intrusive linked list rooted at {@link #head}. */
    private static final Object LOCK = new Object();

    /** Head of the intrusive doubly-linked list of currently registered references; {@code null} when empty. */
    private static MeterReference head;

    /**
     * A {@link PhantomReference} to a started {@link Meter} that doubles as the node of the intrusive linked list.
     * It snapshots only the immutable data required to report a leak ({@code fullID}) plus the message logger
     * (an SLF4J singleton, safe to retain), because the referent {@link Meter} is already collected by the time the
     * leak is reported.
     */
    static final class MeterReference extends PhantomReference<Meter> {
        private final String fullID;
        private final Logger messageLogger;
        /** Intrusive list links; guarded by {@link MeterLeakDetector#LOCK}. */
        private MeterReference prev;
        private MeterReference next;
        /** {@code true} while this reference is linked in the list; guarded by {@link MeterLeakDetector#LOCK}. */
        private boolean registered;

        MeterReference(final Meter meter, final ReferenceQueue<Meter> queue) {
            super(meter, queue);
            this.fullID = meter.getFullID();
            this.messageLogger = meter.getMessageLogger();
        }

        /**
         * Emits the forgotten-meter error, byte-for-byte equivalent to the former {@code MeterValidator.validateFinalize}.
         */
        void reportLeak() {
            messageLogger.error(Markers.INVALID_ARGUMENT, "{}; id={}",
                    "Meter never stopped, must remember to call ok/reject/fail/success() on each started one",
                    fullID);
        }
    }

    /**
     * Registers a started meter for leak detection and drains any pending leaks first.
     * The returned reference must be handed back to {@link #deregister(MeterReference)} when the meter is stopped.
     *
     * @param meter the meter that has just started; must not be {@code null}.
     * @return the registration handle to keep on the meter and pass to {@link #deregister(MeterReference)}.
     */
    static MeterReference register(final Meter meter) {
        drain();
        final MeterReference ref = new MeterReference(meter, QUEUE);
        synchronized (LOCK) {
            ref.next = head;
            if (head != null) {
                head.prev = ref;
            }
            head = ref;
            ref.registered = true;
        }
        return ref;
    }

    /**
     * Deregisters a meter that was stopped explicitly, so its eventual collection is not reported as a leak.
     * Idempotent and {@code null}-safe. Clearing the reference also prevents the garbage collector from ever
     * enqueueing it, since at this point the referent meter is still strongly reachable.
     *
     * @param ref the handle returned by {@link #register(Meter)}, or {@code null}.
     */
    static void deregister(final MeterReference ref) {
        if (ref == null) {
            return;
        }
        synchronized (LOCK) {
            unlink(ref);
        }
        ref.clear();
    }

    /**
     * Drains the reference queue, reporting every meter that was collected while still registered.
     * Safe to call from any thread; invoked opportunistically by {@link #register(Meter)}.
     */
    static void drain() {
        Reference<? extends Meter> r;
        while ((r = QUEUE.poll()) != null) {
            final MeterReference ref = (MeterReference) r;
            final boolean wasRegistered;
            synchronized (LOCK) {
                wasRegistered = ref.registered;
                unlink(ref);
            }
            if (wasRegistered) {
                ref.reportLeak();
            }
        }
    }

    /**
     * Unlinks a reference from the intrusive list. Caller must hold {@link #LOCK}. Idempotent.
     */
    private static void unlink(final MeterReference ref) {
        if (!ref.registered) {
            return;
        }
        if (ref.prev != null) {
            ref.prev.next = ref.next;
        } else {
            head = ref.next;
        }
        if (ref.next != null) {
            ref.next.prev = ref.prev;
        }
        ref.prev = null;
        ref.next = null;
        ref.registered = false;
    }

    /**
     * Number of currently registered references. For tests only.
     */
    static int trackedCount() {
        int count = 0;
        synchronized (LOCK) {
            for (MeterReference ref = head; ref != null; ref = ref.next) {
                count++;
            }
        }
        return count;
    }
}
