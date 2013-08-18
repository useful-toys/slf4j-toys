/* 
 * Copyright 2013 Daniel Felix Ferber.
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
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.ReadableMessageWriter;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

/**
 *
 * @author Daniel Felix Ferber
 */
public class WatcherEvent {
    /**
     * How many times the watch has executed.
     */
    protected long counter = 0;
    protected String uuid;
    /**
     * When the watcher last executed.
     */
    protected long time = 0;

    /* MemoryMXBean */
    protected long heap_commited = 0;
    protected long heap_init = 0;
    protected long heap_max = 0;
    protected long heap_used = 0;
    protected long nonHeap_commited = 0;
    protected long nonHeap_init = 0;
    protected long nonHeap_max = 0;
    protected long nonHeap_used = 0;
    protected long objectPendingFinalizationCount = 0;
    protected long classLoading_loaded = 0;
    protected long classLoading_total = 0;
    protected long classLoading_unloaded = 0;
    protected long compilationTime = 0;
    protected long garbageCollector_count = 0;
    protected long garbageCollector_time = 0;
    protected long runtime_freeMemory = 0;
    protected long runtime_maxMemory = 0;
    protected long runtime_totalMemory = 0;
    protected double systemLoad = 0.0;

    public void collectData() {
        counter++;

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        time = System.nanoTime();

        Runtime runtime = Runtime.getRuntime();
        runtime_freeMemory = runtime.freeMemory();
        runtime_maxMemory = runtime.maxMemory();
        runtime_totalMemory = runtime.totalMemory();

        MemoryUsage heapUsage = memory.getHeapMemoryUsage();
        heap_commited = heapUsage.getCommitted();
        heap_init = heapUsage.getInit();
        heap_max = heapUsage.getMax();
        heap_used = heapUsage.getUsed();

        MemoryUsage nonHeapUsage = memory.getHeapMemoryUsage();
        nonHeap_commited = nonHeapUsage.getCommitted();
        nonHeap_init = nonHeapUsage.getInit();
        nonHeap_max = nonHeapUsage.getMax();
        nonHeap_used = nonHeapUsage.getUsed();

        objectPendingFinalizationCount = memory.getObjectPendingFinalizationCount();

        ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
        classLoading_loaded = classLoading.getLoadedClassCount();
        classLoading_total = classLoading.getTotalLoadedClassCount();
        classLoading_unloaded = classLoading.getUnloadedClassCount();

        CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
        compilationTime = compilation.getTotalCompilationTime();

        List<GarbageCollectorMXBean> garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();

        garbageCollector_count = 0;
        garbageCollector_time = 0;
        for (GarbageCollectorMXBean garbageCollector : garbageCollectors) {
            garbageCollector_count += garbageCollector.getCollectionCount();
            garbageCollector_time += garbageCollector.getCollectionTime();
        }

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        systemLoad = os.getSystemLoadAverage();
    }

    public void readableString(StringBuilder builder) {
        if (this.runtime_freeMemory > 0 || this.runtime_maxMemory > 0 || this.runtime_totalMemory > 0) {
            builder.append("Memory status: ");
            builder.append(ReadableMessageWriter.bestUnit(this.runtime_totalMemory - this.runtime_freeMemory, ReadableMessageWriter.MEMORY_UNITS, ReadableMessageWriter.MEMORY_FACTORS));
            builder.append(' ');
            builder.append(ReadableMessageWriter.bestUnit(this.runtime_totalMemory, ReadableMessageWriter.MEMORY_UNITS, ReadableMessageWriter.MEMORY_FACTORS));
            builder.append(' ');
            builder.append(ReadableMessageWriter.bestUnit(this.runtime_maxMemory, ReadableMessageWriter.MEMORY_UNITS, ReadableMessageWriter.MEMORY_FACTORS));
        } else {
            builder.append("No memory status.");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (counter ^ (counter >>> 32));
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WatcherEvent other = (WatcherEvent) obj;
        if (counter != other.counter) {
            return false;
        }
        if (uuid == null) {
            return false;
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.uuid + ":" + this.counter;
    }
}
