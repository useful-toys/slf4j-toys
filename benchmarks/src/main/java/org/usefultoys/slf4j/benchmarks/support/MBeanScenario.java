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
package org.usefultoys.slf4j.benchmarks.support;

import org.usefultoys.slf4j.SystemConfig;

/**
 * The JMX MXBean collection regime for a benchmark trial, controlling the five
 * {@code SystemConfig.use*ManagedBean} switches that gate how much of the system
 * snapshot is read from {@link java.lang.management.ManagementFactory} beans.
 * <p>
 * All switches default to {@code false} in production, so {@link #NONE} reproduces the
 * out-of-the-box cost. The other regimes turn beans on so the measurement is not blind
 * to the (potentially expensive, native-backed) bean reads a user may enable.
 * <p>
 * The shared {@code SystemMetricsCollector} singleton always holds live bean references
 * (fetched unconditionally at first use); these flags are re-read on every collect, so a
 * trial can switch regime in {@code @Setup(Level.Trial)} without rebuilding anything.
 * {@link #apply()} always writes all five fields, so consecutive trials in one fork never
 * inherit a previous regime's leftovers.
 * <p>
 * <b>Component asymmetry &mdash; which beans each component actually reads:</b>
 * <ul>
 *   <li><b>Meter</b> calls only {@code collectRuntimeStatus} (no bean) and
 *       {@code collectPlatformStatus}, so the <em>only</em> switch that changes a Meter's
 *       cost is {@link SystemConfig#usePlatformManagedBean} (the OS CPU-load bean, whose
 *       {@code getSystemCpuLoad()} is native and comparatively costly). For a Meter,
 *       {@link #ALL} is therefore indistinguishable from {@link #PLATFORM}; only
 *       {@link #NONE} and {@link #PLATFORM} are worth crossing.</li>
 *   <li><b>Watcher</b> additionally calls {@code collectManagedBeanStatus}, which reads the
 *       memory, class-loading, compilation and GC beans, so all five switches matter and all
 *       three regimes are distinct.</li>
 * </ul>
 * Bean collection only runs inside the message-level guard (Meter) or when
 * {@code isInfoEnabled() || isTraceEnabled()} (Watcher), so this axis only moves the number
 * when the corresponding {@link LoggingScenario} has logging enabled; crossed with
 * {@link LoggingScenario#OFF} every regime collapses to the same cost.
 */
public enum MBeanScenario {
    /** All bean switches off: the production default and the current baseline. */
    NONE(false, false, false, false, false),
    /**
     * Only the OS/platform bean on ({@code usePlatformManagedBean}). This is the single
     * switch that affects a Meter, and it isolates the native {@code getSystemCpuLoad()}
     * cost from the other, cheaper beans.
     */
    PLATFORM(true, false, false, false, false),
    /**
     * All five beans on &mdash; a fully instrumented, production-like snapshot. Only the
     * Watcher exercises the memory/class-loading/compilation/GC beans; for a Meter this is
     * equivalent to {@link #PLATFORM}.
     */
    ALL(true, true, true, true, true);

    private final boolean platform;
    private final boolean memory;
    private final boolean classLoading;
    private final boolean compilation;
    private final boolean garbageCollection;

    MBeanScenario(final boolean platform, final boolean memory, final boolean classLoading,
                  final boolean compilation, final boolean garbageCollection) {
        this.platform = platform;
        this.memory = memory;
        this.classLoading = classLoading;
        this.compilation = compilation;
        this.garbageCollection = garbageCollection;
    }

    /**
     * Writes all five {@code SystemConfig.use*ManagedBean} switches for this regime. Call
     * once per trial in {@code @Setup(Level.Trial)}; every field is set explicitly so a
     * later trial in the same fork cannot inherit a previous regime's values.
     */
    public void apply() {
        SystemConfig.usePlatformManagedBean = platform;
        SystemConfig.useMemoryManagedBean = memory;
        SystemConfig.useClassLoadingManagedBean = classLoading;
        SystemConfig.useCompilationManagedBean = compilation;
        SystemConfig.useGarbageCollectionManagedBean = garbageCollection;
    }
}
