/* 
 * Copyright 2015 Daniel Felix Ferber.
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
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

/**
 * Augments the EventData with status information collected from the virtual
 * machine.
 *
 * @author Daniel Felix Ferber
 */
public abstract class SystemData extends EventData {

    private static final long serialVersionUID = 1L;

    protected SystemData() {
        super();
    }
    protected long heap_commited = 0;
    protected long heap_max = 0;
    protected long heap_used = 0;
    protected long nonHeap_commited = 0;
    protected long nonHeap_max = 0;
    protected long nonHeap_used = 0;
    protected long objectPendingFinalizationCount = 0;
    protected long classLoading_loaded = 0;
    protected long classLoading_total = 0;
    protected long classLoading_unloaded = 0;
    protected long compilationTime = 0;
    protected long garbageCollector_count = 0;
    protected long garbageCollector_time = 0;
    protected long runtime_usedMemory = 0;
    protected long runtime_maxMemory = 0;
    protected long runtime_totalMemory = 0;
    protected double systemLoad = 0.0;

    @Override
    protected void resetImpl() {
        this.heap_commited = 0;
//        this.heap_init = 0;
        this.heap_max = 0;
        this.heap_used = 0;
        this.nonHeap_commited = 0;
//        this.nonHeap_init = 0;
        this.nonHeap_max = 0;
        this.nonHeap_used = 0;
        this.objectPendingFinalizationCount = 0;
        this.classLoading_loaded = 0;
        this.classLoading_total = 0;
        this.classLoading_unloaded = 0;
        this.compilationTime = 0;
        this.garbageCollector_count = 0;
        this.garbageCollector_time = 0;
        this.runtime_usedMemory = 0;
        this.runtime_maxMemory = 0;
        this.runtime_totalMemory = 0;
        this.systemLoad = 0;
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
        if (Double.doubleToLongBits(this.systemLoad) != Double.doubleToLongBits(other.systemLoad)) {
            return false;
        }
        return true;
    }

    protected void collectSystemStatus() {
        final Runtime runtime = Runtime.getRuntime();
        runtime_totalMemory = runtime.totalMemory();
        runtime_usedMemory = runtime_totalMemory - runtime.freeMemory();
        runtime_maxMemory = runtime.maxMemory();

        if (ProfilingSession.useManagementFactory) {
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

            final ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
            classLoading_loaded = classLoading.getLoadedClassCount();
            classLoading_total = classLoading.getTotalLoadedClassCount();
            classLoading_unloaded = classLoading.getUnloadedClassCount();

            final CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
            compilationTime = compilation.getTotalCompilationTime();

            final List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();

            garbageCollector_count = 0;
            garbageCollector_time = 0;
            for (final GarbageCollectorMXBean garbageCollector : garbageCollectors) {
                garbageCollector_count += garbageCollector.getCollectionCount();
                garbageCollector_time += garbageCollector.getCollectionTime();
            }

            final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            systemLoad = os.getSystemLoadAverage();
        }
    }
    static final String MEMORY = "m";
    static final String HEAP = "h";
    static final String NON_HEAP = "nh";
    static final String FINALIZATION_COUNT = "fc";
    static final String CLASS_LOADING = "cl";
    static final String COMPILATION_TIME = "ct";
    static final String GARBAGE_COLLECTOR = "gc";
    static final String SYSTEM_LOAD = "sl";

    @Override
    protected void writePropertiesImpl(final EventWriter w) {
        /* memory usage */
        if (this.runtime_usedMemory > 0 || this.runtime_totalMemory > 0 || this.runtime_maxMemory > 0) {
            w.property(MEMORY, this.runtime_usedMemory, this.runtime_totalMemory, this.runtime_maxMemory);
        }

        /* heap usage */
        if (this.heap_commited > 0 || this.heap_max > 0 || this.heap_used > 0) {
            w.property(HEAP, this.heap_used, this.heap_commited, this.heap_max);
        }

        /* non heap usage */
        if (this.nonHeap_commited > 0 || this.nonHeap_max > 0 || this.nonHeap_used > 0) {
            w.property(NON_HEAP, this.nonHeap_used, this.nonHeap_commited, this.nonHeap_max);
        }

        /* objectPendingFinalizationCount */
        if (this.objectPendingFinalizationCount > 0) {
            w.property(FINALIZATION_COUNT, this.objectPendingFinalizationCount);
        }

        /* class loading */
        if (this.classLoading_loaded > 0 || this.classLoading_total > 0 || this.classLoading_unloaded > 0) {
            w.property(CLASS_LOADING, this.classLoading_total, this.classLoading_loaded, this.classLoading_unloaded);
        }

        /* compiler */
        if (this.compilationTime > 0) {
            w.property(COMPILATION_TIME, this.compilationTime);
        }

        /* garbage collector. */
        if (this.garbageCollector_count > 0 || this.garbageCollector_time > 0) {
            w.property(GARBAGE_COLLECTOR, this.garbageCollector_count, this.garbageCollector_time);
        }

        /* system load */
        if (this.systemLoad > 0) {
            w.property(SYSTEM_LOAD, this.systemLoad);
        }

    }

    @Override
    protected boolean readPropertyImpl(final EventReader r, final String propertyName) throws IOException {
        if (MEMORY.equals(propertyName)) {
            this.runtime_usedMemory = r.readLong();
            this.runtime_totalMemory = r.readLong();
            this.runtime_maxMemory = r.readLong();
            return true;
        } else if (HEAP.equals(propertyName)) {
            this.heap_used = r.readLong();
            this.heap_commited = r.readLong();
            this.heap_max = r.readLong();
            return true;
        } else if (NON_HEAP.equals(propertyName)) {
            this.nonHeap_used = r.readLong();
            this.nonHeap_commited = r.readLong();
            this.nonHeap_max = r.readLong();
            return true;
        } else if (FINALIZATION_COUNT.equals(propertyName)) {
            this.objectPendingFinalizationCount = r.readLong();
            return true;
        } else if (CLASS_LOADING.equals(propertyName)) {
            this.classLoading_total = r.readLong();
            this.classLoading_loaded = r.readLong();
            this.classLoading_unloaded = r.readLong();
            return true;
        } else if (COMPILATION_TIME.equals(propertyName)) {
            this.compilationTime = r.readLong();
            return true;
        } else if (GARBAGE_COLLECTOR.equals(propertyName)) {
            this.garbageCollector_count = r.readLong();
            this.garbageCollector_time = r.readLong();
            return true;
        } else if (SYSTEM_LOAD.equals(propertyName)) {
            this.systemLoad = r.readDouble();
            return true;
        }
        return false;
    }
}
