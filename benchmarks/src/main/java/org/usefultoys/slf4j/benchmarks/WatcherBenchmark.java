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
package org.usefultoys.slf4j.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.usefultoys.slf4j.benchmarks.support.LoggingScenario;
import org.usefultoys.slf4j.benchmarks.support.MBeanScenario;
import org.usefultoys.slf4j.watcher.Watcher;

import java.util.concurrent.TimeUnit;

/**
 * Decomposes the Watcher cost into its two distinct phases:
 * <ul>
 *   <li>{@link #construct} &mdash; building and preparing a {@code Watcher}
 *       ({@code new Watcher(name)} resolves the message and data loggers). This is a
 *       once-per-Watcher setup cost, measured fresh each invocation.</li>
 *   <li>{@link #run} &mdash; a single {@code Watcher.run()} snapshot on an
 *       already-prepared instance. Unlike a Meter, a Watcher is designed to run
 *       repeatedly, so one shared instance is reused across invocations.</li>
 * </ul>
 * The {@code logging} axis uses all four {@link LoggingScenario} regimes because the
 * Watcher, unlike the Meter, emits its human-readable and JSON5 data lines independently
 * ({@code run()} collects when {@code isInfoEnabled() || isTraceEnabled()}), so
 * {@link LoggingScenario#DATA_ONLY} is a genuinely distinct, reachable case here.
 * <p>
 * Enabled output is routed to a discarding appender, so the measured cost is the
 * metric collection and string construction inside slf4j-toys, not logback I/O. Run
 * with {@code -prof gc} for {@code gc.alloc.rate.norm} (bytes/op).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class WatcherBenchmark {

    private static final String NAME = "benchmark.watcher";

    /**
     * Logging regime: {@code OFF} / {@code MESSAGE} / {@code DATA_ONLY} / {@code MESSAGE_DATA}.
     * All four are meaningful for the Watcher, which emits its message and data lines
     * independently, so {@code DATA_ONLY} is a genuinely distinct, reachable case here.
     */
    @Param({"OFF", "MESSAGE", "DATA_ONLY", "MESSAGE_DATA"})
    public LoggingScenario logging;

    /**
     * JMX MXBean collection regime. All three are distinct for the Watcher, whose
     * {@code run()} calls {@code collectManagedBeanStatus} and therefore reads the memory,
     * class-loading, compilation and GC beans in addition to the OS/platform bean. Beans are
     * only read when logging is enabled, so crossed with {@link LoggingScenario#OFF} every
     * regime collapses to the same cost.
     */
    @Param({"NONE", "PLATFORM", "ALL"})
    public MBeanScenario mbeans;

    private Watcher watcher;

    @Setup(Level.Trial)
    public void setup() {
        LoggingScenario.configureSuffixes();
        logging.apply(NAME);
        mbeans.apply();
        /* Prepared instance for the run() benchmark; construct() builds its own. */
        watcher = new Watcher(NAME);
    }

    @Benchmark
    public void construct(final Blackhole bh) {
        bh.consume(new Watcher(NAME));
    }

    @Benchmark
    public void run(final Blackhole bh) {
        watcher.run();
        bh.consume(watcher);
    }
}
