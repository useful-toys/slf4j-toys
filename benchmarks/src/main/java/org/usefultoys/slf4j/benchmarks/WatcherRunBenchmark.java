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
import org.usefultoys.slf4j.watcher.Watcher;

import java.util.concurrent.TimeUnit;

/**
 * Skeleton benchmark: cost of one {@code Watcher.run()} cycle with all
 * loggers OFF and all {@code SystemConfig.use*ManagedBean} switches at their
 * default (false). With {@code messageLogger.isInfoEnabled()} and
 * {@code dataLogger.isTraceEnabled()} both false (logback root level OFF, see
 * benchmarks logback.xml), the entire metric-collection block short circuits:
 * only {@code collectCurrentTime()} (one {@code System.nanoTime()}) and
 * {@code nextPosition()} (long increment) execute. This is the absolute
 * floor cost of the Watcher instrumentation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class WatcherRunBenchmark {

    private Watcher watcher;

    @org.openjdk.jmh.annotations.Setup(Level.Iteration)
    public void setup() {
        watcher = new Watcher("benchmark.watcher");
    }

    @Benchmark
    public void run(Blackhole bh) {
        watcher.run();
        bh.consume(watcher);
    }
}