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

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.SystemConfig;

import java.lang.management.*;
import java.util.List;

/**
 * A utility class responsible for collecting status metrics from the Java Virtual Machine (JVM)
 * and the underlying operating system. This class centralizes the logic for gathering performance
 * and resource usage data, which can then be used to populate {@link SystemData} objects.
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("Since15")
@UtilityClass
class SystemMetricsCollector {

    /**
     * Collects all enabled system metrics and populates the provided {@link SystemData} object.
     * This method orchestrates the collection of various metrics based on the settings in
     * {@link SystemConfig}.
     *
     * @param data The {@link SystemData} object to be populated with metrics.
     */
    void collect(final SystemData data) {
        collectRuntimeStatus(data);
        collectPlatformStatus(data);
        collectManagedBeanStatus(data);
    }

    /**
     * Collects memory usage statistics from the JVM's {@link Runtime} object.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    void collectRuntimeStatus(final SystemData data) {
        final Runtime runtime = Runtime.getRuntime();
        data.runtime_totalMemory = runtime.totalMemory();
        data.runtime_usedMemory = data.runtime_totalMemory - runtime.freeMemory();
        data.runtime_maxMemory = runtime.maxMemory();
    }

    /**
     * Collects operating system-level metrics, specifically the system CPU load.
     * It attempts to use {@code com.sun.management.OperatingSystemMXBean} for more precise
     * CPU load, falling back to {@link OperatingSystemMXBean#getSystemLoadAverage()} if necessary.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    void collectPlatformStatus(final SystemData data) {
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
                data.systemLoad = cpuLoad;
                return;
            }
        }

        // Fallback:
        final double loadAverage = osBean.getSystemLoadAverage();
        final int availableProcessors = osBean.getAvailableProcessors();
        if (loadAverage >= 0 && availableProcessors > 0) {
            // may report negative values on some platforms, specifically Windows
            data.systemLoad = loadAverage / availableProcessors;
        }
    }

    /**
     * Collects various JVM metrics using Java Management Extensions (JMX) MXBeans,
     * based on the configuration in {@link SystemConfig}.
     *
     * @param data The {@link SystemData} object to be populated.
     */
    void collectManagedBeanStatus(final SystemData data) {
        if (SystemConfig.useMemoryManagedBean) {
            final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            final MemoryUsage heapUsage = memory.getHeapMemoryUsage();
            data.heap_commited = heapUsage.getCommitted();
            data.heap_max = heapUsage.getMax();
            data.heap_used = heapUsage.getUsed();

            final MemoryUsage nonHeapUsage = memory.getNonHeapMemoryUsage();
            data.nonHeap_commited = nonHeapUsage.getCommitted();
            data.nonHeap_max = nonHeapUsage.getMax();
            data.nonHeap_used = nonHeapUsage.getUsed();
            data.objectPendingFinalizationCount = memory.getObjectPendingFinalizationCount();
        }

        if (SystemConfig.useClassLoadingManagedBean) {
            final ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
            data.classLoading_loaded = classLoading.getLoadedClassCount();
            data.classLoading_total = classLoading.getTotalLoadedClassCount();
            data.classLoading_unloaded = classLoading.getUnloadedClassCount();
        }

        if (SystemConfig.useCompilationManagedBean) {
            final CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
            data.compilationTime = compilation.getTotalCompilationTime();
        }

        if (SystemConfig.useGarbageCollectionManagedBean) {
            final List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
            long count = 0;
            long time = 0;
            for (final GarbageCollectorMXBean garbageCollector : garbageCollectors) {
                count += garbageCollector.getCollectionCount();
                time += garbageCollector.getCollectionTime();
            }
            data.garbageCollector_count = count;
            data.garbageCollector_time = time;
        }
    }
}
