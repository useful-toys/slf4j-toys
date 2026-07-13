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
import org.usefultoys.test.ResetMeterConfig;
import org.usefultoys.test.ValidateCleanMeter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Guards the invariant documented on {@code UnknownMeter} (see TDR-0042): every public, state-mutating
 * method declared on {@link Meter} must either be overridden by {@code UnknownMeter} to deny the call, or
 * be provably safe as-is because its own precondition already rejects a never-started meter.
 * <p>
 * This invariant is currently unenforced by the compiler: a new method added to {@code Meter} that writes
 * a field directly (as {@link Meter#mf(String, Object...)} does, which writes {@code description} while
 * its only guard, {@code validateMPrecondition}, checks {@code stopTime} rather than identity) would
 * silently start mutating the shared, process-wide {@code UNKNOWN_INSTANCE} unless {@code UnknownMeter}
 * is updated to override it too — and unless this test's {@code INVOKERS} map is updated alongside it,
 * as happened here: {@code mf(String, Object...)} was overridden on {@code UnknownMeter} but the map
 * lagged behind, leaving the override itself unexercised by {@link #everyRegisteredMethodIsANoOpOnUnknownInstance()}.
 * <p>
 * Rather than hand-listing "the methods we remembered to check" (which cannot catch a method nobody
 * remembered to add), this test reflectively enumerates every qualifying method on {@link Meter} and
 * cross-checks it against a hand-maintained invoker map: a mismatch fails loudly, forcing whoever adds a
 * new state-mutating method to consciously add an invoker here (and, if needed, an {@code UnknownMeter}
 * override) rather than discovering the gap later as silent shared-state corruption.
 *
 * @author Daniel Felix Ferber
 */
@ResetMeterConfig
@ValidateCleanMeter
@DisplayName("UnknownMeter override invariant (reflective safety net)")
class MeterUnknownInstanceOverrideInvariantTest {

    /**
     * One invoker per qualifying method, keyed by the same signature format produced by
     * {@link #signatureOf(Method)}, so {@link #everyQualifyingMethodHasAnInvoker()} can assert the two
     * sets are identical.
     */
    private static final Map<String, Function<Meter, Object>> INVOKERS = new LinkedHashMap<>();

    static {
        INVOKERS.put("sub(java.lang.String)", m -> m.sub("child"));
        INVOKERS.put("m(java.lang.String)", m -> m.m("message"));
        INVOKERS.put("m(java.lang.String,[Ljava.lang.Object;)", m -> m.m("message {}", 1));
        INVOKERS.put("mf(java.lang.String,[Ljava.lang.Object;)", m -> m.mf("value=%d", 42));
        INVOKERS.put("limitMilliseconds(long)", m -> m.limitMilliseconds(1000L));
        INVOKERS.put("iterations(long)", m -> m.iterations(10L));
        INVOKERS.put("putContext(java.lang.String,java.lang.Object)", m -> {
            m.putContext("key", "value");
            return null;
        });
        INVOKERS.put("putContext(java.lang.String)", m -> {
            m.putContext("flag");
            return null;
        });
        INVOKERS.put("start()", Meter::start);
        INVOKERS.put("inc()", Meter::inc);
        INVOKERS.put("incBy(long)", m -> m.incBy(1L));
        INVOKERS.put("incTo(long)", m -> m.incTo(1L));
        INVOKERS.put("progress()", Meter::progress);
        INVOKERS.put("path(java.lang.Object)", m -> m.path("somePath"));
        INVOKERS.put("ok()", Meter::ok);
        INVOKERS.put("ok(java.lang.Object)", m -> m.ok("somePath"));
        INVOKERS.put("success()", Meter::success);
        INVOKERS.put("success(java.lang.Object)", m -> m.success("somePath"));
        INVOKERS.put("reject(java.lang.Object)", m -> m.reject("someCause"));
        INVOKERS.put("fail(java.lang.Object)", m -> m.fail("someCause"));
        INVOKERS.put("close()", m -> {
            m.close();
            return null;
        });
    }

    @Test
    @DisplayName("every public, non-static, Meter/void-returning method declared on Meter has a registered invoker")
    void everyQualifyingMethodHasAnInvoker() {
        final Set<String> declared = qualifyingMethodSignatures();
        assertEquals(declared, INVOKERS.keySet(),
                "Meter gained or lost a state-mutating method not reflected in this test's INVOKERS map. "
                        + "Add (or remove) an invoker here, and make sure UnknownMeter overrides any new "
                        + "method with denied() unless its own precondition already rejects a never-started "
                        + "meter (see TDR-0042 and TDR-0041).");
    }

    @Test
    @DisplayName("every registered method leaves UNKNOWN_INSTANCE's observable state unchanged")
    void everyRegisteredMethodIsANoOpOnUnknownInstance() {
        for (final Map.Entry<String, Function<Meter, Object>> entry : INVOKERS.entrySet()) {
            final String signature = entry.getKey();
            final Meter unknown = Meter.getCurrentInstance();
            final Snapshot before = Snapshot.of(unknown);

            entry.getValue().apply(unknown);

            final Snapshot after = Snapshot.of(unknown);
            assertEquals(before, after, () -> signature + " mutated observable state of the shared UNKNOWN_INSTANCE");
            assertSame(unknown, Meter.getCurrentInstance(),
                    signature + " must not push a new current instance onto the thread-local stack");
        }
    }

    /**
     * Enumerates every method declared directly on {@link Meter} (not inherited from {@code MeterData} or
     * the {@code MeterContext}/{@code MeterExecutor} interfaces) that is public, non-static, and returns
     * either {@code Meter} (the fluent chaining idiom used throughout this API) or {@code void} — i.e. the
     * exact shape every state-mutating operation in this class follows.
     */
    private static Set<String> qualifyingMethodSignatures() {
        final Set<String> result = new TreeSet<>();
        for (final Method method : Meter.class.getDeclaredMethods()) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }
            final Class<?> returnType = method.getReturnType();
            if (returnType != Meter.class && returnType != void.class) {
                continue;
            }
            result.add(signatureOf(method));
        }
        return result;
    }

    private static String signatureOf(final Method method) {
        final StringBuilder sb = new StringBuilder(method.getName()).append('(');
        final Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(params[i].getName());
        }
        return sb.append(')').toString();
    }

    /** Immutable capture of every field the task requires to remain unchanged, for before/after comparison. */
    private static final class Snapshot {
        private final String description;
        private final Map<String, String> context;
        private final long startTime;
        private final long stopTime;
        private final long currentIteration;
        private final long expectedIterations;
        private final long timeLimit;
        private final String okPath;
        private final String rejectPath;
        private final String failPath;
        private final String failMessage;

        private Snapshot(final Meter m) {
            description = m.getDescription();
            context = new HashMap<>(m.getContext());
            startTime = m.getStartTime();
            stopTime = m.getStopTime();
            currentIteration = m.getCurrentIteration();
            expectedIterations = m.getExpectedIterations();
            timeLimit = m.getTimeLimit();
            okPath = m.getOkPath();
            rejectPath = m.getRejectPath();
            failPath = m.getFailPath();
            failMessage = m.getFailMessage();
        }

        static Snapshot of(final Meter m) {
            return new Snapshot(m);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Snapshot)) {
                return false;
            }
            final Snapshot other = (Snapshot) o;
            return startTime == other.startTime
                    && stopTime == other.stopTime
                    && currentIteration == other.currentIteration
                    && expectedIterations == other.expectedIterations
                    && timeLimit == other.timeLimit
                    && java.util.Objects.equals(description, other.description)
                    && java.util.Objects.equals(context, other.context)
                    && java.util.Objects.equals(okPath, other.okPath)
                    && java.util.Objects.equals(rejectPath, other.rejectPath)
                    && java.util.Objects.equals(failPath, other.failPath)
                    && java.util.Objects.equals(failMessage, other.failMessage);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(description, context, startTime, stopTime, currentIteration,
                    expectedIterations, timeLimit, okPath, rejectPath, failPath, failMessage);
        }

        @Override
        public String toString() {
            return "Snapshot{description=" + description + ", context=" + context + ", startTime=" + startTime
                    + ", stopTime=" + stopTime + ", currentIteration=" + currentIteration
                    + ", expectedIterations=" + expectedIterations + ", timeLimit=" + timeLimit
                    + ", okPath=" + okPath + ", rejectPath=" + rejectPath + ", failPath=" + failPath
                    + ", failMessage=" + failMessage + '}';
        }
    }
}
