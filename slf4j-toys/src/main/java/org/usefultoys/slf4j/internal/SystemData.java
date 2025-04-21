/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.IOException;
import java.lang.management.*;
import java.util.List;

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

    protected SystemData(final String sessionUuid, final long position) {
        super(sessionUuid, position);
    }

    // For tests
    protected SystemData(final String sessionUuid, final long position, final long time) {
        super(sessionUuid, position, time);
    }

    // For tests
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

    @Override
    protected boolean isCompletelyEqualsImpl(final EventData obj) {
        final SystemData other = (SystemData) obj;
        if (this.heap_commited != other.heap_commited) {
            return false;
        }
        if (this.heap_max != other.heap_max) {
            return false;
        }
        if (this.heap_used != other.heap_used) {
            return false;
        }
        if (this.nonHeap_commited != other.nonHeap_commited) {
            return false;
        }
        if (this.nonHeap_max != other.nonHeap_max) {
            return false;
        }
        if (this.nonHeap_used != other.nonHeap_used) {
            return false;
        }
        if (this.objectPendingFinalizationCount != other.objectPendingFinalizationCount) {
            return false;
        }
        if (this.classLoading_loaded != other.classLoading_loaded) {
            return false;
        }
        if (this.classLoading_total != other.classLoading_total) {
            return false;
        }
        if (this.classLoading_unloaded != other.classLoading_unloaded) {
            return false;
        }
        if (this.compilationTime != other.compilationTime) {
            return false;
        }
        if (this.garbageCollector_count != other.garbageCollector_count) {
            return false;
        }
        if (this.garbageCollector_time != other.garbageCollector_time) {
            return false;
        }
        if (this.runtime_usedMemory != other.runtime_usedMemory) {
            return false;
        }
        if (this.runtime_maxMemory != other.runtime_maxMemory) {
            return false;
        }
        if (this.runtime_totalMemory != other.runtime_totalMemory) {
            return false;
        }
        return Double.doubleToLongBits(this.systemLoad) == Double.doubleToLongBits(other.systemLoad);
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
        double loadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
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
    protected void writePropertiesImpl(final EventWriter w) {
        /* memory usage */
        if (this.runtime_usedMemory > 0 || this.runtime_totalMemory > 0 || this.runtime_maxMemory > 0) {
            w.property(PROP_MEMORY, this.runtime_usedMemory, this.runtime_totalMemory, this.runtime_maxMemory);
        }

        /* heap usage */
        if (this.heap_commited > 0 || this.heap_max > 0 || this.heap_used > 0) {
            w.property(PROP_HEAP, this.heap_used, this.heap_commited, this.heap_max);
        }

        /* non heap usage */
        if (this.nonHeap_commited > 0 || this.nonHeap_max > 0 || this.nonHeap_used > 0) {
            w.property(PROP_NON_HEAP, this.nonHeap_used, this.nonHeap_commited, this.nonHeap_max);
        }

        /* objectPendingFinalizationCount */
        if (this.objectPendingFinalizationCount > 0) {
            w.property(PROP_FINALIZATION_COUNT, this.objectPendingFinalizationCount);
        }

        /* class loading */
        if (this.classLoading_loaded > 0 || this.classLoading_total > 0 || this.classLoading_unloaded > 0) {
            w.property(PROP_CLASS_LOADING, this.classLoading_total, this.classLoading_loaded, this.classLoading_unloaded);
        }

        /* compiler */
        if (this.compilationTime > 0) {
            w.property(PROP_COMPILATION_TIME, this.compilationTime);
        }

        /* garbage collector. */
        if (this.garbageCollector_count > 0 || this.garbageCollector_time > 0) {
            w.property(PROP_GARBAGE_COLLECTOR, this.garbageCollector_count, this.garbageCollector_time);
        }

        /* system load */
        if (this.systemLoad > 0) {
            w.property(PROP_SYSTEM_LOAD, this.systemLoad);
        }
    }
}
