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
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Reference measurements with <em>no</em> Meter involved. These provide the
 * denominators for interpreting Meter overhead:
 * <ul>
 *   <li>{@link #empty} &mdash; the JMH harness floor (method call + Blackhole),
 *       i.e. the noise level of the instrument itself.</li>
 *   <li>{@link #nanoTimeX2} &mdash; two {@code System.nanoTime()} reads, the
 *       unavoidable clock cost that Meter/Watcher also pay. Handy as a portable
 *       unit ("the meter costs N x two nanoTime").</li>
 *   <li>{@link #operationOnly} &mdash; a synthetic production operation of a
 *       controlled cost ({@code Blackhole.consumeCPU(load)}), <em>without</em> any
 *       Meter around it. Compared at the same {@code load} against the Meter
 *       scenarios ({@link MeterOperationBenchmark}), the difference is the honest
 *       relative overhead of instrumenting an operation of that size.</li>
 * </ul>
 * {@code consumeCPU} allocates nothing, so under {@code -prof gc} the
 * {@code gc.alloc.rate.norm} of {@code operationOnly} is ~0 and any bytes/op in the
 * Meter scenarios are attributable purely to slf4j-toys.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class OperationBaselineBenchmark {

    /**
     * Cost of the simulated production operation, in {@code Blackhole.consumeCPU}
     * tokens. {@code 0} recovers the pure-overhead case; the larger values sketch
     * the spectrum from a trivial in-memory call up to an I/O-bound operation.
     * Keep these values identical to {@link MeterOperationBenchmark} so the pairs
     * line up.
     */
    @Param({"0", "50", "500", "5000"})
    public long load;

    @Benchmark
    public void empty(final Blackhole bh) {
        bh.consume(load);
    }

    @Benchmark
    public long nanoTimeX2() {
        final long t0 = System.nanoTime();
        final long t1 = System.nanoTime();
        return t1 - t0;
    }

    @Benchmark
    public void operationOnly(final Blackhole bh) {
        Blackhole.consumeCPU(load);
        bh.consume(load);
    }
}
