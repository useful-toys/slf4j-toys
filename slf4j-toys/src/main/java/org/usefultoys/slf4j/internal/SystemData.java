/*
 * Copyright 2025 Daniel Felix Ferber
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
package org.usefultoys.slf4j.internal;

import lombok.Getter;
import org.usefultoys.slf4j.SystemConfig;

import java.lang.management.*;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Augments the {@link EventData} with status collected from the virtual machine.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("Since15")
public abstract class SystemData extends EventData {

    private static final long serialVersionUID = 1L;

    protected SystemData() {
    }

    protected SystemData(final String uuid) {
        super(uuid);
    }

    protected SystemData(final String sessionUuid, final long position, final long time) {
        super(sessionUuid, position, time);
    }

    // for tests only
    protected SystemData(final String sessionUuid, final long position, final long time,
                         final long heap_commited, final long heap_max, final long heap_used,
                         final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                         final long objectPendingFinalizationCount,
                         final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded,
                         final long compilationTime, final long garbageCollector_count, final long garbageCollector_time,
                         final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory,
                         final double systemLoad) {
        super(sessionUuid, position, time);
        this.heap_commited = heap_commited;
        this.heap_max = heap_max;
        this.heap_used = heap_used;
        this.nonHeap_commited = nonHeap_commited;
        this.nonHeap_max = nonHeap_max;
        this.nonHeap_used = nonHeap_used;
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
        this.classLoading_loaded = classLoading_loaded;
        this.classLoading_total = classLoading_total;
        this.classLoading_unloaded = classLoading_unloaded;
        this.compilationTime = compilationTime;
        this.garbageCollector_count = garbageCollector_count;
        this.garbageCollector_time = garbageCollector_time;
        this.runtime_usedMemory = runtime_usedMemory;
        this.runtime_maxMemory = runtime_maxMemory;
        this.runtime_totalMemory = runtime_totalMemory;
        this.systemLoad = systemLoad;
    }

    @Getter
    protected long heap_commited = 0;
    @Getter
    protected long heap_max = 0;
    @Getter
    protected long heap_used = 0;
    @Getter
    protected long nonHeap_commited = 0;
    @Getter
    protected long nonHeap_max = 0;
    @Getter
    protected long nonHeap_used = 0;
    @Getter
    protected long objectPendingFinalizationCount = 0;
    @Getter
    protected long classLoading_loaded = 0;
    @Getter
    protected long classLoading_total = 0;
    @Getter
    protected long classLoading_unloaded = 0;
    @Getter
    protected long compilationTime = 0;
    @Getter
    protected long garbageCollector_count = 0;
    @Getter
    protected long garbageCollector_time = 0;
    @Getter
    protected long runtime_usedMemory = 0;
    @Getter
    protected long runtime_maxMemory = 0;
    @Getter
    protected long runtime_totalMemory = 0;
    @Getter
    protected double systemLoad = 0.0;

    @Override
    protected void resetImpl() {
        super.resetImpl();
        heap_commited = 0;
        heap_max = 0;
        heap_used = 0;
        nonHeap_commited = 0;
        nonHeap_max = 0;
        nonHeap_used = 0;
        objectPendingFinalizationCount = 0;
        classLoading_loaded = 0;
        classLoading_total = 0;
        classLoading_unloaded = 0;
        compilationTime = 0;
        garbageCollector_count = 0;
        garbageCollector_time = 0;
        runtime_usedMemory = 0;
        runtime_maxMemory = 0;
        runtime_totalMemory = 0;
        systemLoad = 0;
    }

    protected void collectRuntimeStatus() {
        final Runtime runtime = Runtime.getRuntime();
        runtime_totalMemory = runtime.totalMemory();
        runtime_usedMemory = runtime_totalMemory - runtime.freeMemory();
        runtime_maxMemory = runtime.maxMemory();
    }

    protected void collectPlatformStatus() {
        if (!SystemConfig.usePlatformManagedBean) {
            return;
        }

        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            final com.sun.management.OperatingSystemMXBean sunOsBean =
                    (com.sun.management.OperatingSystemMXBean) osBean;
            final double cpuLoad = sunOsBean.getSystemCpuLoad();
            if (cpuLoad >= 0) {
                // may report negative values on some platforms, specifically Windows
                systemLoad = cpuLoad;
                return;
            }
        }

        // Fallback:
        final double loadAverage = osBean.getSystemLoadAverage();
        final int availableProcessors = osBean.getAvailableProcessors();
        if (loadAverage >= 0 && availableProcessors > 0) {
            // may report negative values on some platforms, specifically Windows
            systemLoad = loadAverage / availableProcessors;
        }
    }

    protected void collectManagedBeanStatus() {
        if (SystemConfig.useMemoryManagedBean) {
            final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            final MemoryUsage heapUsage = memory.getHeapMemoryUsage();
            heap_commited = heapUsage.getCommitted();
            heap_max = heapUsage.getMax();
            heap_used = heapUsage.getUsed();

            final MemoryUsage nonHeapUsage = memory.getHeapMemoryUsage();
            nonHeap_commited = nonHeapUsage.getCommitted();
            nonHeap_max = nonHeapUsage.getMax();
            nonHeap_used = nonHeapUsage.getUsed();
            objectPendingFinalizationCount = memory.getObjectPendingFinalizationCount();
        }

        if (SystemConfig.useClassLoadingManagedBean) {
            final ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
            classLoading_loaded = classLoading.getLoadedClassCount();
            classLoading_total = classLoading.getTotalLoadedClassCount();
            classLoading_unloaded = classLoading.getUnloadedClassCount();
        }

        if (SystemConfig.useCompilationManagedBean) {
            final CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
            compilationTime = compilation.getTotalCompilationTime();
        }

        if (SystemConfig.useGarbageCollectionManagedBean) {
            final List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
            garbageCollector_count = 0;
            garbageCollector_time = 0;
            for (final GarbageCollectorMXBean garbageCollector : garbageCollectors) {
                garbageCollector_count += garbageCollector.getCollectionCount();
                garbageCollector_time += garbageCollector.getCollectionTime();
            }
        }
    }

    public static final String PROP_MEMORY = "m";
    public static final String PROP_HEAP = "h";
    public static final String PROP_NON_HEAP = "nh";
    public static final String PROP_FINALIZATION_COUNT = "fc";
    public static final String PROP_CLASS_LOADING = "cl";
    public static final String PROP_COMPILATION_TIME = "ct";
    public static final String PROP_GARBAGE_COLLECTOR = "gc";
    public static final String PROP_SYSTEM_LOAD = "sl";

    @Override
    protected void writeJson5Impl(final StringBuilder sb) {
        super.writeJson5Impl(sb);
        /* memory usage */
        if (runtime_usedMemory > 0 || runtime_totalMemory > 0 || runtime_maxMemory > 0) {
            sb.append(String.format(Locale.US, ",%s:[%d,%d,%d]", PROP_MEMORY, runtime_usedMemory, runtime_totalMemory, runtime_maxMemory));
        }

        /* heap usage */
        if (heap_commited > 0 || heap_max > 0 || heap_used > 0) {
            sb.append(String.format(Locale.US, ",%s:[%d,%d,%d]", PROP_HEAP, heap_used, heap_commited, heap_max));
        }

        /* non heap usage */
        if (nonHeap_commited > 0 || nonHeap_max > 0 || nonHeap_used > 0) {
            sb.append(String.format(Locale.US, ",%s:[%d,%d,%d]", PROP_NON_HEAP, nonHeap_used, nonHeap_commited, nonHeap_max));
        }

        /* objectPendingFinalizationCount */
        if (objectPendingFinalizationCount > 0) {
            sb.append(String.format(Locale.US, ",%s:%d", PROP_FINALIZATION_COUNT, objectPendingFinalizationCount));
        }

        /* class loading */
        if (classLoading_loaded > 0 || classLoading_total > 0 || classLoading_unloaded > 0) {
            sb.append(String.format(Locale.US, ",%s:[%d,%d,%d]", PROP_CLASS_LOADING, classLoading_total, classLoading_loaded, classLoading_unloaded));
        }

        /* compiler */
        if (compilationTime > 0) {
            sb.append(String.format(Locale.US, ",%s:%d", PROP_COMPILATION_TIME, compilationTime));
        }

        /* garbage collector. */
        if (garbageCollector_count > 0 || garbageCollector_time > 0) {
            sb.append(String.format(Locale.US, ",%s:[%d,%d]", PROP_GARBAGE_COLLECTOR, garbageCollector_count, garbageCollector_time));
        }

        /* system load */
        if (systemLoad > 0) {
            sb.append(String.format(Locale.US, ",%s:%.1f", PROP_SYSTEM_LOAD, systemLoad));
        }
    }

    private static final String REGEX_3_TUPLE = "\\s*:\\s*\\[([^,}\\s]+),([^,}\\s]+),([^,}\\s]+)\\]";
    private static final String REGEX_2_TUPLE = "\\s*:\\s*\\[([^,}\\s]+),([^,}\\s]+)\\]";
    private static final Pattern patternMemory = Pattern.compile(REGEX_START+PROP_MEMORY + REGEX_3_TUPLE);
    private static final Pattern patternHeap = Pattern.compile(REGEX_START+PROP_HEAP + REGEX_3_TUPLE);
    private static final Pattern patternNonHeap = Pattern.compile(REGEX_START+PROP_NON_HEAP + REGEX_3_TUPLE);
    private static final Pattern patternFinalizationCount = Pattern.compile(REGEX_START+PROP_FINALIZATION_COUNT + REGEX_WORD_VALUE);
    private static final Pattern patternClassLoading = Pattern.compile(REGEX_START+PROP_CLASS_LOADING + REGEX_3_TUPLE);
    private static final Pattern patternCompilationTime = Pattern.compile(REGEX_START+PROP_COMPILATION_TIME + REGEX_WORD_VALUE);
    private static final Pattern patternGarbageCollector = Pattern.compile(REGEX_START+PROP_GARBAGE_COLLECTOR + REGEX_2_TUPLE);
    private static final Pattern patternSystemLoad = Pattern.compile(REGEX_START+PROP_SYSTEM_LOAD + REGEX_WORD_VALUE);

    @Override
    public void readJson5(final String json5) {
        super.readJson5(json5);

        final Matcher matcherMemory = patternMemory.matcher(json5);
        if (matcherMemory.find()) {
            runtime_usedMemory = Long.parseLong(matcherMemory.group(1));
            runtime_totalMemory = Long.parseLong(matcherMemory.group(2));
            runtime_maxMemory = Long.parseLong(matcherMemory.group(3));
        }

        final Matcher matcherHeap = patternHeap.matcher(json5);
        if (matcherHeap.find()) {
            heap_used = Long.parseLong(matcherHeap.group(1));
            heap_commited = Long.parseLong(matcherHeap.group(2));
            heap_max = Long.parseLong(matcherHeap.group(3));
        }

        final Matcher matcherNonHeap = patternNonHeap.matcher(json5);
        if (matcherNonHeap.find()) {
            nonHeap_used = Long.parseLong(matcherNonHeap.group(1));
            nonHeap_commited = Long.parseLong(matcherNonHeap.group(2));
            nonHeap_max = Long.parseLong(matcherNonHeap.group(3));
        }

        final Matcher matcherFinalizationCount = patternFinalizationCount.matcher(json5);
        if (matcherFinalizationCount.find()) {
            objectPendingFinalizationCount = Long.parseLong(matcherFinalizationCount.group(1));
        }

        final Matcher matcherClassLoading = patternClassLoading.matcher(json5);
        if (matcherClassLoading.find()) {
            classLoading_total = Long.parseLong(matcherClassLoading.group(1));
            classLoading_loaded = Long.parseLong(matcherClassLoading.group(2));
            classLoading_unloaded = Long.parseLong(matcherClassLoading.group(3));
        }

        final Matcher matcherCompilationTime = patternCompilationTime.matcher(json5);
        if (matcherCompilationTime.find()) {
            compilationTime = Long.parseLong(matcherCompilationTime.group(1));
        }

        final Matcher matcherGarbageCollector = patternGarbageCollector.matcher(json5);
        if (matcherGarbageCollector.find()) {
            garbageCollector_count = Long.parseLong(matcherGarbageCollector.group(1));
            garbageCollector_time = Long.parseLong(matcherGarbageCollector.group(2));
        }

        final Matcher matcherSystemLoad = patternSystemLoad.matcher(json5);
        if (matcherSystemLoad.find()) {
            systemLoad = Double.parseDouble(matcherSystemLoad.group(1));
        }
    }
}
