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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Forward-compatible detector for {@link Meter} instances that were started but never explicitly stopped
 * (via {@code ok()}, {@code reject()}, {@code fail()} or {@code close()}).
 * <p>
 * This is the replacement for the deprecated {@link Object#finalize()} mechanism. It relies on
 * {@link WeakReference} plus a {@link ReferenceQueue} — an API available on every Java version since Java 2 —
 * so it keeps working on Java 8 through future releases where finalization is removed.
 * <p>
 * <b>Design — a single reference object per started meter:</b> {@link MeterReference} is, at the same time, the
 * node of the {@link Meter} thread-local stack (its {@link WeakReference#get()} returns the current meter, or
 * {@code null} once it is collected) and, when leak detection is enabled, the handle the garbage collector enqueues
 * on collection. It also <em>is</em> the node of the intrusive doubly-linked registry (it carries its own
 * {@code prev}/{@code next} pointers), so no separate container node is allocated. Unifying the stack reference and
 * the leak-detection reference keeps allocation at one auxiliary object per started meter — the same cost the
 * thread-local stack already paid before leak detection existed.
 * <p>
 * A {@link WeakReference} (rather than a {@code PhantomReference}) is required because the stack must be able to
 * retrieve the live meter; it is also a better fit, since the referent is reclaimed as soon as it becomes weakly
 * reachable and {@code reportLeak()} works from an immutable snapshot rather than the (already cleared) referent.
 * <p>
 * <b>Draining strategy — no background thread:</b> the {@link ReferenceQueue} is drained opportunistically from
 * {@link #track(Meter, MeterReference)}, i.e. on the thread that starts the next meter. A forgotten meter is therefore
 * reported the next time any meter is started, with no library-owned daemon thread and no risk of pinning a web
 * application class loader in a servlet container.
 * <p>
 * <b>Predicate equivalence:</b> a meter is leak-tracked only from {@code start()} (so {@code startTime != 0} is
 * implied) and is untracked on every explicit termination. Therefore a reference that the garbage collector enqueues
 * <em>while still registered</em> is, by definition, a meter that was started and never stopped — exactly the
 * predicate the former {@code finalize()} path checked. The {@code UNKNOWN_LOGGER_NAME} exclusion and the
 * {@link MeterConfig#detectLeaks} toggle are applied in {@link #track(Meter, MeterReference)}; when leak detection is
 * off, the reference is still created to serve as the stack node, but is neither queued nor registered.
 * <p>
 * <b>{@code detectLeaks} also gates emission at report time:</b> {@link #drain()} and {@link #reportRemainingLeaks()}
 * re-read {@link MeterConfig#detectLeaks} and stay silent when it is {@code false}, still cleaning up the registry.
 * So turning the toggle off at runtime suppresses pending leak reports for meters that were already registered, not
 * just future ones.
 * <p>
 * <b>Tail draining on shutdown:</b> the opportunistic drain only reports meters the garbage collector has already
 * reclaimed. Meters that are forgotten but never collected during the application's life would otherwise go
 * unreported. When {@link MeterConfig#reportLeaksOnShutdown} is enabled, a JVM shutdown hook is installed lazily (on
 * the first tracked meter) that runs {@link #reportRemainingLeaks()}, reporting every meter still registered at
 * shutdown.
 *
 * @author Daniel Felix Ferber
 * @see Meter
 * @see MeterConfig#detectLeaks
 * @see MeterConfig#reportLeaksOnShutdown
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

    /** Ensures the shutdown hook is installed at most once. */
    private static final AtomicBoolean shutdownHookInstalled = new AtomicBoolean(false);

    /** The installed shutdown hook thread, retained so tests can remove it. */
    private static volatile Thread shutdownHook;

    /**
     * A {@link WeakReference} to a started {@link Meter} that doubles as both the node of the thread-local stack and,
     * when leak detection is enabled, the node of the intrusive leak registry. When leak-detecting it snapshots the
     * immutable data required to report a leak ({@code fullID}) plus the message logger (an SLF4J singleton, safe to
     * retain), because the referent {@link Meter} is already collected by the time the leak is reported.
     */
    static final class MeterReference extends WeakReference<Meter> {
        /** Leak-report snapshot; {@code null} when this reference is a plain (non-detecting) stack node. */
        private final String fullID;
        /** Leak-report logger; {@code null} when this reference is a plain (non-detecting) stack node. */
        private final Logger messageLogger;
        /** Previous reference on the per-thread stack; accessed only by the owning thread, so no lock is needed. */
        private MeterReference previousInstance;
        /** Intrusive registry links; guarded by {@link MeterLeakDetector#LOCK}. */
        private MeterReference prev;
        private MeterReference next;
        /** {@code true} while this reference is linked in the registry; guarded by {@link MeterLeakDetector#LOCK}. */
        private boolean registered;

        /** Creates a plain stack node that is not leak-detecting (no queue, no snapshot). */
        MeterReference(final Meter meter) {
            super(meter);
            this.fullID = null;
            this.messageLogger = null;
        }

        /** Creates a leak-detecting stack node: enqueued on collection and carrying a leak snapshot. */
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
     * Creates the stack-node reference for a meter that has just started, chaining it onto {@code previous}, and — when
     * {@link MeterConfig#detectLeaks} is enabled and the category is known — also registers it for leak detection and
     * drains any pending leaks first.
     *
     * @param meter    the meter that has just started; must not be {@code null}.
     * @param previous the reference currently on top of the thread-local stack, or {@code null}.
     * @return the new reference to push onto the stack and keep on the meter for {@link #untrack(MeterReference)}.
     */
    static MeterReference track(final Meter meter, final MeterReference previous) {
        final boolean detect = MeterConfig.detectLeaks
                && !Meter.UNKNOWN_LOGGER_NAME.equals(meter.getCategory());
        if (!detect) {
            final MeterReference ref = new MeterReference(meter);
            ref.previousInstance = previous;
            return ref;
        }
        drain();
        if (MeterConfig.reportLeaksOnShutdown) {
            installShutdownHookOnce();
        }
        final MeterReference ref = new MeterReference(meter, QUEUE);
        ref.previousInstance = previous;
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
     * Untracks a meter that was stopped explicitly, so its eventual collection is not reported as a leak, and returns
     * the reference to restore as the new top of the thread-local stack. Idempotent and {@code null}-safe.
     * <p>
     * The reference is intentionally <em>not</em> cleared: a stopped reference may still be reachable through the
     * {@code previousInstance} of a meter that was (mistakenly) started on top of it, and clearing would corrupt that
     * chain. A stopped-then-collected reference that is later enqueued is harmlessly ignored by {@link #drain()},
     * because {@link #unlink(MeterReference)} has already marked it unregistered.
     *
     * @param ref the reference returned by {@link #track(Meter, MeterReference)}, or {@code null}.
     * @return the previous reference to restore as the stack top, or {@code null}.
     */
    static MeterReference untrack(final MeterReference ref) {
        if (ref == null) {
            return null;
        }
        synchronized (LOCK) {
            unlink(ref);
        }
        return ref.previousInstance;
    }

    /**
     * Drains the reference queue, reporting every meter that was collected while still registered.
     * Safe to call from any thread; invoked opportunistically by {@link #track(Meter, MeterReference)}.
     */
    static void drain() {
        /* Read the toggle once: it gates emission at report time, so turning detectLeaks off at runtime suppresses
           pending leaks. The queue is still drained and unlinked to keep the registry from growing unbounded. */
        final boolean report = MeterConfig.detectLeaks;
        Reference<? extends Meter> r;
        while ((r = QUEUE.poll()) != null) {
            final MeterReference ref = (MeterReference) r;
            final boolean wasRegistered;
            synchronized (LOCK) {
                wasRegistered = ref.registered;
                unlink(ref);
            }
            if (wasRegistered && report) {
                ref.reportLeak();
            }
        }
    }

    /**
     * Reports every meter still registered (started and never stopped) and empties the registry. This is the tail
     * drain run on JVM shutdown: unlike {@link #drain()}, which only reports meters the garbage collector has already
     * reclaimed, this reports the complete set of outstanding leaks regardless of collection.
     * <p>
     * The list is detached and the references marked unregistered under the lock — so a concurrent {@link #drain()}
     * will not double-report — while the actual logging happens outside the lock, since {@link MeterReference#reportLeak()}
     * may be slow or reentrant.
     */
    static void reportRemainingLeaks() {
        /* Like drain(), detectLeaks gates emission: the sweep always empties the registry, but reports only when
           leak detection is enabled at the moment it runs. */
        final boolean report = MeterConfig.detectLeaks;
        final MeterReference chain;
        synchronized (LOCK) {
            chain = head;
            head = null;
            for (MeterReference ref = chain; ref != null; ref = ref.next) {
                ref.prev = null;
                ref.registered = false;
            }
        }
        for (MeterReference ref = chain; ref != null; ) {
            final MeterReference next = ref.next;
            ref.next = null;
            if (report) {
                ref.reportLeak();
            }
            ref = next;
        }
    }

    /**
     * Installs the JVM shutdown hook that runs {@link #reportRemainingLeaks()}, at most once per JVM.
     */
    private static void installShutdownHookOnce() {
        if (shutdownHookInstalled.compareAndSet(false, true)) {
            final Thread hook = new Thread(MeterLeakDetector::runShutdownReport, "slf4j-toys-MeterLeakDetector-shutdown");
            shutdownHook = hook;
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    /**
     * Body of the shutdown hook: reports remaining leaks if still enabled, swallowing any error so shutdown is never
     * disrupted.
     */
    private static void runShutdownReport() {
        try {
            if (MeterConfig.reportLeaksOnShutdown) {
                reportRemainingLeaks();
            }
        } catch (final Exception ignored) {
            // A shutdown hook must never throw; a failed leak report is not worth disrupting shutdown.
        }
    }

    /**
     * Unlinks a reference from the intrusive registry. Caller must hold {@link #LOCK}. Idempotent. Does not touch the
     * {@code previousInstance} stack link.
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

    /**
     * The installed shutdown hook thread, or {@code null} if none was installed. For tests only.
     */
    static Thread shutdownHookForTests() {
        return shutdownHook;
    }

    /**
     * Removes and forgets the installed shutdown hook, allowing it to be reinstalled. For tests only, to avoid
     * leaking a real JVM shutdown hook across the test suite.
     */
    static void resetShutdownHookForTests() {
        final Thread hook = shutdownHook;
        if (hook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (final IllegalStateException ignored) {
                // JVM already shutting down; nothing to remove.
            }
            shutdownHook = null;
        }
        shutdownHookInstalled.set(false);
    }
}
