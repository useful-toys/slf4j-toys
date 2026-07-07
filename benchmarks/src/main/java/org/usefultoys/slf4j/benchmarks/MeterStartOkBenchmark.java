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
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.usefultoys.slf4j.meter.Meter;

import java.util.concurrent.TimeUnit;

/**
 * Skeleton benchmark: end-to-end cost of one {@code Meter.start().ok()} cycle
 * with all loggers OFF and all {@code SystemConfig.use*ManagedBean} switches at
 * their default (false). This isolates the irreducible instrumentation
 * overhead: {@code System.nanoTime()} x2, {@code ThreadLocal} get/set plus a
 * {@code WeakReference} allocation, and the pre/post-condition validations.
 *
 * The collected MXBean calls ({@code collectRuntimeStatus},
 * {@code collectPlatformStatus}) stay cold because the outer
 * {@code messageLogger.isDebugEnabled()} / {@code isWarnEnabled()} guards short
 * circuit when logback root level is OFF (see benchmarks logback.xml).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class MeterStartOkBenchmark {

    private Logger logger;
    private Meter meter;

    @org.openjdk.jmh.annotations.Setup(Level.Iteration)
    public void setup() {
        logger = org.slf4j.LoggerFactory.getLogger("benchmark.meter");
        meter = new Meter(logger);
    }

    @Benchmark
    public void startOk(Blackhole bh) {
        Meter m = meter.start().ok();
        bh.consume(m);
    }
}