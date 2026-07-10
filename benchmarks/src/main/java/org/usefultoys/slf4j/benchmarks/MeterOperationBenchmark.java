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
 * Every scenario has the same shape used in production: a fresh {@code Meter} wrapping
 * a synthetic operation of controlled cost ({@code Blackhole.consumeCPU(load)}). A new
 * Meter is built inside each {@code @Benchmark} method (never reused across invocations)
 * so the measured path is the real one-shot lifecycle and never the "already stopped"
 * validation branch.
 * <p>
 * Two axes cross the scenarios:
 * <ul>
 *   <li>{@code load} &mdash; the operation cost. {@code 0} recovers the pure Meter
 *       overhead; larger values place the Meter around progressively heavier operations.
 *       Compare against {@link OperationBaselineBenchmark#operationOnly} at the same
 *       {@code load} to read off the relative overhead.</li>
 *   <li>{@code logging} &mdash; the {@link LoggingScenario}: {@code OFF} / {@code MESSAGE} /
 *       {@code DATA_ONLY} / {@code MESSAGE_DATA}. An enabled logger fires at every lifecycle
 *       point (start and ok/reject/fail). {@code DATA_ONLY} equals {@code OFF} for the Meter
 *       (data is nested inside the message guard).</li>
 * </ul>
 * Run with {@code -prof gc} to also capture {@code gc.alloc.rate.norm} (bytes/op), the
 * low-variance signal for spotting allocation regressions or wins.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class MeterOperationBenchmark {

    private static final String CATEGORY = "benchmark.meter";
    private static final String OPERATION = "op";
    private static final String CAUSE = "cause";

    /** Operation cost in {@code Blackhole.consumeCPU} tokens; keep aligned with {@link OperationBaselineBenchmark}. */
    @Param({"0", "50", "500", "5000"})
    public long load;

    /**
     * Logging regime: {@code OFF} (nothing), {@code MESSAGE} (human message only),
     * {@code DATA_ONLY} (JSON data only) or {@code MESSAGE_DATA} (both). An enabled logger
     * fires at every lifecycle point (start and ok/reject/fail). Note {@code DATA_ONLY}
     * equals {@code OFF} for the Meter, whose data line is nested inside the message guard.
     */
    @Param({"OFF", "MESSAGE", "DATA_ONLY", "MESSAGE_DATA"})
    public LoggingScenario logging;

    private Logger logger;

    @Setup(Level.Trial)
    public void setup() {
        /* Split message and data onto distinct loggers, then apply the levels for this
         * trial's regime. Suffixes must be set before the Meter resolves its loggers. */
        LoggingScenario.configureSuffixes();
        logger = LoggerFactory.getLogger(CATEGORY);
        logging.apply(CATEGORY);
    }

    @Benchmark
    public void startOk(final Blackhole bh) {
        final Meter m = MeterFactory.getMeter(logger, OPERATION).start();
        Blackhole.consumeCPU(load);
        bh.consume(m.ok());
    }

    @Benchmark
    public void startFail(final Blackhole bh) {
        final Meter m = MeterFactory.getMeter(logger, OPERATION).start();
        Blackhole.consumeCPU(load);
        bh.consume(m.fail(CAUSE));
    }

    @Benchmark
    public void startReject(final Blackhole bh) {
        final Meter m = MeterFactory.getMeter(logger, OPERATION).start();
        Blackhole.consumeCPU(load);
        bh.consume(m.reject(CAUSE));
    }
}
