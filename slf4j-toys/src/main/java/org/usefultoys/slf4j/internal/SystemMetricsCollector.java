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

import org.usefultoys.slf4j.SystemConfig;

import java.lang.management.*;
import java.util.List;

/**
 * A class responsible for collecting status metrics from the Java Virtual Machine (JVM)
 * and the underlying operating system. An instance of this class is configured with the
 * necessary MXBeans and can then be used to populate {@link SystemData} objects.
 * This design allows for dependency injection, making the class easily testable.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("Since15")
public class SystemMetricsCollector {

    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ClassLoadingMXBean classLoadingBean;
    private final CompilationMXBean compilationBean;
    private final List<GarbageCollectorMXBean> garbageCollectorBeans;

    /**
     * Constructs a new collector with the provided MXBean dependencies.
     *
     * @param osBean                The {@link OperatingSystemMXBean} to collect OS metrics from.
     * @param memoryBean            The {@link MemoryMXBean} to collect memory metrics from.
     * @param classLoadingBean      The {@link ClassLoadingMXBean} to collect class loading metrics from.
     * @param compilationBean       The {@link CompilationMXBean} to collect compilation metrics from.
     * @param garbageCollectorBeans A list of {@link GarbageCollectorMXBean}s to collect GC metrics from.
     */
    public SystemMetricsCollector(
            final OperatingSystemMXBean osBean,
            final MemoryMXBean memoryBean,
            final ClassLoadingMXBean classLoadingBean,
            final CompilationMXBean compilationBean,
            final List<GarbageCollectorMXBean> garbageCollectorBeans) {
        this.osBean = osBean;
        this.memoryBean = memoryBean;
        this.classLoadingBean = classLoadingBean;
        this.compilationBean = compilationBean;
        this.garbageCollectorBeans = garbageCollectorBeans;
    }

    /**
     * Collects all enabled system metrics and populates the provided {@link SystemData} object.
     *
     * @param data The {@link SystemData} object to be populated with metrics.
     */
    public void collect(final SystemData data) {
        collectRuntimeStatus(data);
        collectPlatformStatus(data);
        collectManagedBeanStatus(data);
    }

    /**
     * Protected method to allow test subclasses to override where the Runtime instance comes from.
     *
     * @return The {@link Runtime} instance.
     */
    protected Runtime getRuntime() {
        return Runtime.getRuntime();
    }

    /**
     * Collects memory usage statistics from the JVM's {@link Runtime} object.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    public void collectRuntimeStatus(final SystemData data) {
        final Runtime runtime = getRuntime();
        data.runtime_totalMemory = runtime.totalMemory();
        data.runtime_usedMemory = data.runtime_totalMemory - runtime.freeMemory();
        data.runtime_maxMemory = runtime.maxMemory();
    }

    /**
     * Collects operating system-level metrics, specifically the system CPU load.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    public void collectPlatformStatus(final SystemData data) {
        if (!SystemConfig.usePlatformManagedBean || this.osBean == null) {
            return;
        }

        if (this.osBean instanceof com.sun.management.OperatingSystemMXBean) {
            final com.sun.management.OperatingSystemMXBean sunOsBean =
                    (com.sun.management.OperatingSystemMXBean) this.osBean;
            final double cpuLoad = sunOsBean.getSystemCpuLoad();
            if (cpuLoad >= 0) {
                data.systemLoad = cpuLoad;
                return;
            }
        }

        final double loadAverage = this.osBean.getSystemLoadAverage();
        final int availableProcessors = this.osBean.getAvailableProcessors();
        if (loadAverage >= 0 && availableProcessors > 0) {
            data.systemLoad = loadAverage / availableProcessors;
        }
    }

    /**
     * Collects various JVM metrics using Java Management Extensions (JMX) MXBeans.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    public void collectManagedBeanStatus(final SystemData data) {
        if (SystemConfig.useMemoryManagedBean && this.memoryBean != null) {
            final MemoryUsage heapUsage = this.memoryBean.getHeapMemoryUsage();
            data.heap_commited = heapUsage.getCommitted();
            data.heap_max = heapUsage.getMax();
            data.heap_used = heapUsage.getUsed();

            final MemoryUsage nonHeapUsage = this.memoryBean.getNonHeapMemoryUsage();
            data.nonHeap_commited = nonHeapUsage.getCommitted();
            data.nonHeap_max = nonHeapUsage.getMax();
            data.nonHeap_used = nonHeapUsage.getUsed();
            data.objectPendingFinalizationCount = this.memoryBean.getObjectPendingFinalizationCount();
        }

        if (SystemConfig.useClassLoadingManagedBean && this.classLoadingBean != null) {
            data.classLoading_loaded = this.classLoadingBean.getLoadedClassCount();
            data.classLoading_total = this.classLoadingBean.getTotalLoadedClassCount();
            data.classLoading_unloaded = this.classLoadingBean.getUnloadedClassCount();
        }

        if (SystemConfig.useCompilationManagedBean && this.compilationBean != null) {
            data.compilationTime = this.compilationBean.getTotalCompilationTime();
        }

        if (SystemConfig.useGarbageCollectionManagedBean && this.garbageCollectorBeans != null) {
            long count = 0;
            long time = 0;
            for (final GarbageCollectorMXBean garbageCollector : this.garbageCollectorBeans) {
                count += garbageCollector.getCollectionCount();
                time += garbageCollector.getCollectionTime();
            }
            data.garbageCollector_count = count;
            data.garbageCollector_time = time;
        }
    }
}
