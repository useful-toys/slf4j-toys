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

/**
 * Extends {@link EventData} to include status metrics collected from the Java Virtual Machine (JVM)
 * and the underlying operating system. This class provides detailed insights into runtime
 * performance and resource usage. It acts as a data transfer object (DTO) for system metrics,
 * with data collection delegated to the singleton instance of {@link SystemMetricsCollector}.
 *
 * @author Daniel Felix Ferber
 * @see EventData
 * @see SystemConfig
 * @see SystemMetricsCollector
 * @see SystemMetrics
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
     * Collects all enabled system metrics by delegating to the singleton {@link SystemMetricsCollector} instance.
     */
    protected void collect() {
        SystemMetrics.getInstance().collect(this);
    }

    /**
     * Collects memory usage statistics by delegating to the singleton {@link SystemMetricsCollector} instance.
     */
    protected void collectRuntimeStatus() {
        SystemMetrics.getInstance().collectRuntimeStatus(this);
    }

    /**
     * Collects operating system-level metrics by delegating to the singleton {@link SystemMetricsCollector} instance.
     */
    protected void collectPlatformStatus() {
        SystemMetrics.getInstance().collectPlatformStatus(this);
    }

    /**
     * Collects various JVM metrics by delegating to the singleton {@link SystemMetricsCollector} instance.
     */
    protected void collectManagedBeanStatus() {
        SystemMetrics.getInstance().collectManagedBeanStatus(this);
    }

    @Override
    protected void writeJson5(final StringBuilder sb) {
        super.writeJson5(sb);
        SystemDataJson5.write(this, sb);
    }

    @Override
    public void readJson5(final String json5) {
        super.readJson5(json5);
        SystemDataJson5.read(this, json5);
    }
}
