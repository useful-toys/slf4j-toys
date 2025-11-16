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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.usefultoys.slf4j.SystemConfig;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends {@link EventData} to include status metrics collected from the Java Virtual Machine (JVM)
 * and the underlying operating system. This class provides detailed insights into runtime
 * performance and resource usage. It acts as a data transfer object (DTO) for system metrics,
 * with data collection handled by {@link SystemMetricsCollector}.
 *
 * @author Daniel Felix Ferber
 * @see EventData
 * @see SystemConfig
 * @see SystemMetricsCollector
 */
@SuppressWarnings("Since15")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SystemData extends EventData {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a SystemData instance with a specified session UUID.
     *
     * @param uuid The unique identifier for the JVM session.
     */
    protected SystemData(final String uuid) {
        super(uuid);
    }

    /**
     * Constructs a SystemData instance with a specified session UUID and timestamp.
     *
     * @param uuid The unique identifier for the JVM session.
     * @param timestamp The timestamp (in nanoseconds) when the data was collected.
     */
    protected SystemData(final String uuid, final long timestamp) {
        super(uuid, timestamp);
    }

    /**
     * Constructs a SystemData instance with detailed system metrics.
     * This constructor is primarily intended for testing purposes.
     *
     * @param sessionUuid The unique identifier for the JVM session.
     * @param position The time-ordered sequential position of the event.
     * @param lastCurrentTime The timestamp (in nanoseconds) when the data was collected.
     * @param heap_commited The committed heap memory in bytes.
     * @param heap_max The maximum heap memory in bytes.
     * @param heap_used The used heap memory in bytes.
     * @param nonHeap_commited The committed non-heap memory in bytes.
     * @param nonHeap_max The maximum non-heap memory in bytes.
     * @param nonHeap_used The used non-heap memory in bytes.
     * @param objectPendingFinalizationCount The number of objects pending finalization.
     * @param classLoading_loaded The number of classes currently loaded.
     * @param classLoading_total The total number of classes loaded since JVM start.
     * @param classLoading_unloaded The total number of classes unloaded since JVM start.
     * @param compilationTime The total time spent in compilation.
     * @param garbageCollector_count The total number of garbage collections.
     * @param garbageCollector_time The total time spent in garbage collection.
     * @param runtime_usedMemory The used memory reported by {@link Runtime}.
     * @param runtime_maxMemory The maximum memory reported by {@link Runtime}.
     * @param runtime_totalMemory The total memory reported by {@link Runtime}.
     * @param systemLoad The system CPU load average.
     */
    protected SystemData(final String sessionUuid, final long position, final long lastCurrentTime,
                         final long heap_commited, final long heap_max, final long heap_used,
                         final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                         final long objectPendingFinalizationCount,
                         final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded,
                         final long compilationTime, final long garbageCollector_count, final long garbageCollector_time,
                         final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory,
                         final double systemLoad) {
        super(sessionUuid, position, lastCurrentTime);
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

    /** The committed heap memory in bytes. */
    @Getter
    long heap_commited = 0;
    /** The maximum heap memory in bytes. */
    @Getter
    long heap_max = 0;
    /** The used heap memory in bytes. */
    @Getter
    long heap_used = 0;
    /** The committed non-heap memory in bytes. */
    @Getter
    long nonHeap_commited = 0;
    /** The maximum non-heap memory in bytes. */
    @Getter
    long nonHeap_max = 0;
    /** The used non-heap memory in bytes. */
    @Getter
    long nonHeap_used = 0;
    /** The number of objects pending finalization. */
    @Getter
    long objectPendingFinalizationCount = 0;
    /** The number of classes currently loaded. */
    @Getter
    long classLoading_loaded = 0;
    /** The total number of classes loaded since JVM start. */
    @Getter
    long classLoading_total = 0;
    /** The total number of classes unloaded since JVM start. */
    @Getter
    long classLoading_unloaded = 0;
    /** The total time spent in compilation by the JIT compiler. */
    @Getter
    long compilationTime = 0;
    /** The total number of garbage collections. */
    @Getter
    long garbageCollector_count = 0;
    /** The total time spent in garbage collection. */
    @Getter
    long garbageCollector_time = 0;
    /** The used memory reported by {@link Runtime}. */
    @Getter
    long runtime_usedMemory = 0;
    /** The maximum memory reported by {@link Runtime}. */
    @Getter
    long runtime_maxMemory = 0;
    /** The total memory reported by {@link Runtime}. */
    @Getter
    long runtime_totalMemory = 0;
    /** The system CPU load average. */
    @Getter
    double systemLoad = 0.0;

    @Override
    public void reset() {
        super.reset();
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

    /**
     * Collects all enabled system metrics by delegating to {@link SystemMetricsCollector}.
     */
    protected void collect() {
        SystemMetricsCollector.collect(this);
    }

    /**
     * Collects memory usage statistics by delegating to {@link SystemMetricsCollector}.
     * Use {@link #collect()} instead.
     */
    protected void collectRuntimeStatus() {
        SystemMetricsCollector.collectRuntimeStatus(this);
    }

    /**
     * Collects operating system-level metrics by delegating to {@link SystemMetricsCollector}.
     * Use {@link #collect()} instead.
     */
    protected void collectPlatformStatus() {
        SystemMetricsCollector.collectPlatformStatus(this);
    }

    /**
     * Collects various JVM metrics by delegating to {@link SystemMetricsCollector}.
     * Use {@link #collect()} instead.
     */
    protected void collectManagedBeanStatus() {
        SystemMetricsCollector.collectManagedBeanStatus(this);
    }

    /** JSON5 key for memory usage (used, total, max) from {@link Runtime}. */
    public static final String PROP_MEMORY = "m";
    /** JSON5 key for heap memory usage (used, committed, max) from {@link MemoryMXBean}. */
    public static final String PROP_HEAP = "h";
    /** JSON5 key for non-heap memory usage (used, committed, max) from {@link MemoryMXBean}. */
    public static final String PROP_NON_HEAP = "nh";
    /** JSON5 key for the number of objects pending finalization. */
    public static final String PROP_FINALIZATION_COUNT = "fc";
    /** JSON5 key for class loading statistics (total, loaded, unloaded). */
    public static final String PROP_CLASS_LOADING = "cl";
    /** JSON5 key for total compilation time. */
    public static final String PROP_COMPILATION_TIME = "ct";
    /** JSON5 key for garbage collection statistics (count, time). */
    public static final String PROP_GARBAGE_COLLECTOR = "gc";
    /** JSON5 key for system CPU load average. */
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

    /** Regular expression component for matching a 3-tuple value in JSON5 (e.g., `:[v1,v2,v3]`). */
    private static final String REGEX_3_TUPLE = "\\s*:\\s*\\[([^,}\\s]+),([^,}\\s]+),([^,}\\s]+)\\]";
    /** Regular expression component for matching a 2-tuple value in JSON5 (e.g., `:[v1,v2]`). */
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
