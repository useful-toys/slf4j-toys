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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.benchmarks.support.LoggingScenario;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Attributes the Meter cost to each lifecycle phase by <em>difference</em>. Every
 * method builds a fresh Meter (never reused) and wraps no synthetic work, so these
 * measurements isolate the Meter's own internals; the {@code load}-parameterized
 * relative overhead lives in {@link MeterOperationBenchmark}. Read a phase's cost as
 * the delta between two scenarios that differ by exactly that phase, e.g.:
 * <ul>
 *   <li>{@link #createStart} &minus; {@link #create} &rarr; cost of {@code start()}</li>
 *   <li>{@link #createStartOk} &minus; {@link #createStart} &rarr; cost of {@code ok()}</li>
 *   <li>{@link #okWithMessage} &minus; {@link #createStartOk} &rarr; cost of {@code m(..)}</li>
 *   <li>{@link #okWithContext} &minus; {@link #createStartOk} &rarr; cost of {@code ctx(..)}</li>
 *   <li>{@link #okWithIterations} &minus; {@link #createStartOk} &rarr; cost of {@code iterations()/inc()}</li>
 * </ul>
 * Note: {@code start()} logs at DEBUG, which is off in the {@code MESSAGE} (INFO) regime,
 * so its message cost only appears at a DEBUG-enabled level (not exercised here on
 * purpose &mdash; start is rarely logged in production).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class MeterLifecycleBenchmark {

    private static final String CATEGORY = "benchmark.meter.lifecycle";
    private static final String OPERATION = "op";
    private static final String MESSAGE = "processing operation";
    private static final String PATH = "SUCCESS";
    private static final String CTX_KEY = "id";

    /**
     * Logging regime: {@code OFF} / {@code MESSAGE} / {@code DATA_ONLY} / {@code MESSAGE_DATA}.
     * An enabled logger fires at every lifecycle point (start and ok/reject/fail).
     * {@code DATA_ONLY} equals {@code OFF} for the Meter (data nested inside the message guard).
     */
    @Param({"OFF", "MESSAGE", "DATA_ONLY", "MESSAGE_DATA"})
    public LoggingScenario logging;

    private Logger logger;

    @Setup(Level.Trial)
    public void setup() {
        LoggingScenario.configureSuffixes();
        logger = LoggerFactory.getLogger(CATEGORY);
        logging.apply(CATEGORY);
    }

    @Benchmark
    public void create(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION));
    }

    @Benchmark
    public void createStart(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start());
    }

    @Benchmark
    public void createStartOk(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().ok());
    }

    @Benchmark
    public void okWithMessage(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().m(MESSAGE).ok());
    }

    @Benchmark
    public void okWithContext(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().ctx(CTX_KEY, 1L).ok());
    }

    @Benchmark
    public void okWithPath(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().ok(PATH));
    }

    @Benchmark
    public void okWithIterations(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().iterations(1).inc().ok());
    }

    @Benchmark
    public void startClose(final Blackhole bh) {
        /* start() then close() without a terminal: the try-with-resources abandonment
         * path, treated as FAIL (see Meter.close()). */
        final Meter m = MeterFactory.getMeter(logger, OPERATION).start();
        m.close();
        bh.consume(m);
    }

    @Benchmark
    public void sub(final Blackhole bh) {
        /* Cost of deriving a sub-meter from a running parent. */
        final Meter parent = MeterFactory.getMeter(logger, OPERATION).start();
        bh.consume(parent.sub("child"));
    }
}
