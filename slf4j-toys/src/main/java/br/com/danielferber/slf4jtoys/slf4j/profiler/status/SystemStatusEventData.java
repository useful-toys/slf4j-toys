/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.status;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventData;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.ReadableMessage;
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
 *
 * @author Daniel
 */
public abstract class SystemStatusEventData extends EventData {

    protected SystemStatusEventData(char messagePrefix) {
        super(messagePrefix);
    }

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
    protected long runtime_usedMemory = 0;
    protected long runtime_maxMemory = 0;
    protected long runtime_totalMemory = 0;
    protected double systemLoad = 0.0;

    @Override
    public StringBuilder readableString(StringBuilder builder) {
        if (this.runtime_usedMemory > 0 || this.runtime_maxMemory > 0 || this.runtime_totalMemory > 0) {
            builder.append("Memory: ");
            builder.append(ReadableMessage.bestUnit(this.runtime_usedMemory, ReadableMessage.MEMORY_UNITS, ReadableMessage.MEMORY_FACTORS));
            builder.append(' ');
            builder.append(ReadableMessage.bestUnit(this.runtime_totalMemory, ReadableMessage.MEMORY_UNITS, ReadableMessage.MEMORY_FACTORS));
            builder.append(' ');
            builder.append(ReadableMessage.bestUnit(this.runtime_maxMemory, ReadableMessage.MEMORY_UNITS, ReadableMessage.MEMORY_FACTORS));
        } else {
            builder.append("No memory status.");
        }
        return builder;
    }

    @Override
    public void reset() {
        this.heap_commited = 0;
        this.heap_init = 0;
        this.heap_max = 0;
        this.heap_used = 0;
        this.nonHeap_commited = 0;
        this.nonHeap_init = 0;
        this.nonHeap_max = 0;
        this.nonHeap_used = 0;
        this.objectPendingFinalizationCount = 0;
        this.classLoading_loaded = 0;
        this.classLoading_total = 0;
        this.classLoading_unloaded = 0;
        this.compilationTime = 0;
        this.garbageCollector_count = 0;
        this.garbageCollector_time = 0;
        this.systemLoad = 0;
    }

    protected void collectSystemStatus() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        Runtime runtime = Runtime.getRuntime();
        runtime_totalMemory = runtime.totalMemory();
        runtime_usedMemory = runtime_totalMemory - runtime.freeMemory();
        runtime_maxMemory = runtime.maxMemory();

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
    
    static final String MEMORY = "m";
    static final String HEAP = "h";
    static final String NON_HEAP = "nh";
    static final String FINALIZATION_COUNT = "fc";
    static final String CLASS_LOADING = "cl";
    static final String COMPILATION_TIME = "ct";
    static final String GARBAGE_COLLECTOR = "gc";
    static final String SYSTEM_LOAD = "sl";

    @Override
    protected void writeProperties(EventWriter w) {
        /* memory usage */
        if (this.runtime_usedMemory > 0 || this.runtime_totalMemory > 0 || this.runtime_maxMemory > 0) {
            w.property(MEMORY, this.runtime_usedMemory, this.runtime_totalMemory, this.runtime_maxMemory);
        }

        /* heap usage */
        if (this.heap_commited > 0 || this.heap_init > 0 || this.heap_max > 0 || this.heap_used > 0) {
            w.property(HEAP, this.heap_commited, this.heap_init, this.heap_max, this.heap_used);
        }

        /* non heap usage */
        if (this.nonHeap_commited > 0 || this.nonHeap_init > 0 || this.nonHeap_max > 0 || this.nonHeap_used > 0) {
            w.property(NON_HEAP, this.nonHeap_commited, this.nonHeap_init, this.nonHeap_max, this.nonHeap_used);
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
    protected boolean readProperty(EventReader r, String propertyName) throws IOException {
        if (MEMORY.equals(propertyName)) {
            this.runtime_usedMemory = r.readLong();
            this.runtime_totalMemory = r.readLong();
            this.runtime_totalMemory = r.readLong();
            return true;
        } else if (HEAP.equals(propertyName)) {
            this.heap_commited = r.readLong();
            this.heap_init = r.readLong();
            this.heap_max = r.readLong();
            this.heap_used = r.readLong();
            return true;
        } else if (NON_HEAP.equals(propertyName)) {
            this.nonHeap_commited = r.readLong();
            this.nonHeap_init = r.readLong();
            this.nonHeap_max = r.readLong();
            this.nonHeap_used = r.readLong();
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
        } 
        return false;
    }
}
