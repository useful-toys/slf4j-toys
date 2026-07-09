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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.benchmarks.support.LoggingScenario;
import org.usefultoys.slf4j.meter.MeterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Cost of using a Meter category/operation for the <em>first</em> time versus every
 * subsequent time, measured in {@link Mode#SingleShotTime}.
 * <p>
 * The first-use cost is paid inside the Meter constructor (see {@code Meter}): resolving
 * the message and data loggers via {@code LoggerFactory.getLogger(...)} and registering the
 * category/operation in {@code EVENT_COUNTER} ({@code putIfAbsent(new AtomicLong(0))}, called
 * from the constructor). All of that is cold the first time a given category is seen and warm
 * (cache hit) afterwards.
 * <p>
 * Why {@code SingleShotTime} rather than {@code AverageTime}: the steady-state modes warm the
 * per-category caches during warmup, which would erase exactly the cost we want to see. Here the
 * {@code first*} methods build a brand-new, never-seen category on <em>every</em> measured
 * invocation (a monotonic counter), so each shot pays the cold registration. Warmup iterations
 * are still used, but they only warm the JIT &mdash; every measured shot remains cold at the
 * category level. This models "first time this category is used on an already-running service",
 * which is the actionable question; true JVM-cold class loading is a one-off startup cost that
 * is not meaningfully repeatable.
 * <p>
 * The {@code warm*} methods reuse a single fixed category, so their caches are hot; the
 * difference {@code first* - warm*} is the first-use registration overhead. Run with
 * {@code -prof gc} to also see the extra bytes/op a first use allocates (loggers, AtomicLong,
 * map nodes). The small cost of building the unique category string is included in the
 * {@code first*} numbers but is negligible next to a cold {@code getLogger}.
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(5)
@Warmup(iterations = 10, batchSize = 1)
@Measurement(iterations = 50, batchSize = 1)
public class MeterColdStartBenchmark {

    private static final String OPERATION = "op";
    private static final String WARM_CATEGORY = "benchmark.warm";

    /** Monotonic source of never-before-seen categories for the cold-path methods. */
    private long counter;
    /** Pre-resolved warm base logger so the {@code warm*} paths hit only cache. */
    private Logger warmLogger;

    @Setup(Level.Trial)
    public void setup() {
        /* Split message/data loggers as elsewhere; cold categories inherit the logback
         * root level (OFF), so this isolates registration cost, not logging cost. */
        LoggingScenario.configureSuffixes();
        LoggingScenario.OFF.apply(WARM_CATEGORY);
        warmLogger = LoggerFactory.getLogger(WARM_CATEGORY);
        counter = 0;
    }

    @Benchmark
    public void firstCreate(final Blackhole bh) {
        /* Resolving the base logger for a brand-new category is itself part of the
         * cold cost, alongside the message/data loggers resolved in the constructor. */
        final Logger logger = LoggerFactory.getLogger("benchmark.cold.c." + counter++);
        bh.consume(MeterFactory.getMeter(logger, OPERATION));
    }

    @Benchmark
    public void warmCreate(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(warmLogger, OPERATION));
    }

    @Benchmark
    public void firstStartOk(final Blackhole bh) {
        final Logger logger = LoggerFactory.getLogger("benchmark.cold.s." + counter++);
        bh.consume(MeterFactory.getMeter(logger, OPERATION).start().ok());
    }

    @Benchmark
    public void warmStartOk(final Blackhole bh) {
        bh.consume(MeterFactory.getMeter(warmLogger, OPERATION).start().ok());
    }
}
