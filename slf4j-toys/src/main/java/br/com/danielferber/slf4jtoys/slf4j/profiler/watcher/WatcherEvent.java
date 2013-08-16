/*
 * Copyright 2012 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.LoggerMessageReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.LoggerMessageWriter;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class WatcherEvent {

    private static final String COUNTER = "c";
    private static final String UUID = "u";
    private static final String MEMORY = "m";
    private static final String TIME = "t";
    private static final String HEAP = "h";
    private static final String NON_HEAP = "nh";
    private static final String FINALIZATION_COUNT = "fc";
    private static final String CLASS_LOADING = "cl";
    private static final String COMPILATION_TIME = "ct";
    private static final String GARBAGE_COLLECTOR = "gc";
    private static final String SYSTEM_LOAD = "sl";
    private static final String[] MEMORY_UNITS = new String[]{"B", "kB", "MB", "GB"};
    private static final double[] MEMORY_FACTORS = new double[]{1000.0, 1000.0, 1000.0};
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
    /**
     * An arbitraty ID for the watcher.
     */
//    @Column(nullable = false, length = 300)
//    protected String name;
    /**
     * How many times the watch has executed.
     */
    protected long counter = 0;
    @Column(nullable = true, length = 50)
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

//    public String getName() {
//        return name;
//    }
    public long getCounter() {
        return counter;
    }

    public String getUuid() {
        return uuid;
    }

    public long getTime() {
        return time;
    }

    public long getHeap_commited() {
        return heap_commited;
    }

    public long getHeap_init() {
        return heap_init;
    }

    public long getHeap_max() {
        return heap_max;
    }

    public long getHeap_used() {
        return heap_used;
    }

    public long getNonHeap_commited() {
        return nonHeap_commited;
    }

    public long getNonHeap_init() {
        return nonHeap_init;
    }

    public long getNonHeap_max() {
        return nonHeap_max;
    }

    public long getNonHeap_used() {
        return nonHeap_used;
    }

    public long getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public long getClassLoading_loaded() {
        return classLoading_loaded;
    }

    public long getClassLoading_total() {
        return classLoading_total;
    }

    public long getClassLoading_unloaded() {
        return classLoading_unloaded;
    }

    public long getCompilationTime() {
        return compilationTime;
    }

    public long getGarbageCollector_count() {
        return garbageCollector_count;
    }

    public long getGarbageCollector_time() {
        return garbageCollector_time;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public long getRuntime_freeMemory() {
        return runtime_freeMemory;
    }

    public long getRuntime_maxMemory() {
        return runtime_maxMemory;
    }

    public long getRuntime_totalMemory() {
        return runtime_totalMemory;
    }

    public long getUsedMemory() {
        return runtime_totalMemory - runtime_freeMemory;
    }

    public static void readableString(WatcherEvent watcher, StringBuilder buffer) {
        if (watcher.runtime_freeMemory > 0 || watcher.runtime_maxMemory > 0 || watcher.runtime_totalMemory > 0) {
            buffer.append("Memory status: ");
            buffer.append(LoggerMessageReader.bestUnit(watcher.runtime_totalMemory - watcher.runtime_freeMemory, WatcherEvent.MEMORY_UNITS, WatcherEvent.MEMORY_FACTORS));
            buffer.append(' ');
            buffer.append(LoggerMessageReader.bestUnit(watcher.runtime_totalMemory, WatcherEvent.MEMORY_UNITS, WatcherEvent.MEMORY_FACTORS));
            buffer.append(' ');
            buffer.append(LoggerMessageReader.bestUnit(watcher.runtime_maxMemory, WatcherEvent.MEMORY_UNITS, WatcherEvent.MEMORY_FACTORS));
        } else {
            buffer.append("No memory status.");
        }
    }

    protected void collectData() {
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

    public static void writeToString(LoggerMessageWriter p, WatcherEvent e, StringBuilder buffer) {
        p.openData();

        /* counter */
        if (e.counter > 0) {
            p.property(WatcherEvent.COUNTER, e.counter);
        }

        /* time */
        if (e.time > 0) {
            p.property(WatcherEvent.TIME, e.time);
        }
        /* uuid */
        if (e.uuid != null) {
            p.property(WatcherEvent.UUID, e.uuid);
        }

        /* memory usage */
        if (e.runtime_freeMemory > 0 || e.runtime_totalMemory > 0 || e.runtime_maxMemory > 0) {
            p.property(WatcherEvent.MEMORY, e.runtime_totalMemory - e.runtime_freeMemory, e.runtime_totalMemory, e.runtime_maxMemory);
        }

        /* heap usage */
        if (e.heap_commited > 0 || e.heap_init > 0 || e.heap_max > 0 || e.heap_used > 0) {
            p.property(WatcherEvent.HEAP, e.heap_commited, e.heap_init, e.heap_max, e.heap_used);
        }

        /* non heap usage */
        if (e.nonHeap_commited > 0 || e.nonHeap_init > 0 || e.nonHeap_max > 0 || e.nonHeap_used > 0) {
            p.property(WatcherEvent.NON_HEAP, e.nonHeap_commited, e.nonHeap_init, e.nonHeap_max, e.nonHeap_used);
        }

        /* objectPendingFinalizationCount */
        if (e.objectPendingFinalizationCount > 0) {
            p.property(WatcherEvent.FINALIZATION_COUNT, e.objectPendingFinalizationCount);
        }

        /* class loading */
        if (e.classLoading_loaded > 0 || e.classLoading_total > 0 || e.classLoading_unloaded > 0) {
            p.property(WatcherEvent.CLASS_LOADING, e.classLoading_total, e.classLoading_loaded, e.classLoading_unloaded);
        }

        /* compiler */
        if (e.compilationTime > 0) {
            p.property(WatcherEvent.COMPILATION_TIME, e.compilationTime);
        }

        /* garbage collector. */
        if (e.garbageCollector_count > 0 || e.garbageCollector_time > 0) {
            p.property(WatcherEvent.GARBAGE_COLLECTOR, e.garbageCollector_count, e.garbageCollector_time);
        }

        /* system load */
        if (e.systemLoad > 0) {
            p.property(WatcherEvent.SYSTEM_LOAD, e.systemLoad);
        }

        buffer.append(p.syntax.DATA_CLOSE);
    }

    public static void readFromString(LoggerMessageReader p, WatcherEvent e, String encodedData) throws IOException {
        /* Reseta todos os atributos. */
//        e.name = null;
        e.counter = 0;

        e.time = 0;
        e.heap_commited = 0;
        e.heap_init = 0;
        e.heap_max = 0;
        e.heap_used = 0;
        e.nonHeap_commited = 0;
        e.nonHeap_init = 0;
        e.nonHeap_max = 0;
        e.nonHeap_used = 0;
        e.objectPendingFinalizationCount = 0;
        e.classLoading_loaded = 0;
        e.classLoading_total = 0;
        e.classLoading_unloaded = 0;
        e.compilationTime = 0;
        e.garbageCollector_count = 0;
        e.garbageCollector_time = 0;
        e.systemLoad = 0;

        p.reset(encodedData);

        /* O nome é obrigatório. */
//        e.name = p.readIdentifierString();

        if (!p.readOptionalOperator(';')) {
            return;
        }

        String propertyName = p.readIdentifierString();
        while (propertyName != null) {
            p.readOperator('=');
            if (WatcherEvent.COUNTER.equals(propertyName)) {
                e.counter = p.readLong();
            } else if (WatcherEvent.UUID.equals(propertyName)) {
                e.uuid = p.readUuid();
            } else if (WatcherEvent.TIME.equals(propertyName)) {
                e.time = p.readLong();
            } else if (WatcherEvent.MEMORY.equals(propertyName)) {
                e.runtime_freeMemory = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.runtime_totalMemory = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.runtime_totalMemory = p.readLong();
            } else if (WatcherEvent.HEAP.equals(propertyName)) {
                e.heap_commited = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.heap_init = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.heap_max = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.heap_used = p.readLong();
            } else if (WatcherEvent.NON_HEAP.equals(propertyName)) {
                e.nonHeap_commited = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.nonHeap_init = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.nonHeap_max = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.nonHeap_used = p.readLong();
            } else if (WatcherEvent.FINALIZATION_COUNT.equals(propertyName)) {
                e.objectPendingFinalizationCount = p.readInt();
            } else if (WatcherEvent.CLASS_LOADING.equals(propertyName)) {
                e.classLoading_total = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.classLoading_loaded = p.readInt();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.classLoading_unloaded = p.readLong();
            } else if (WatcherEvent.COMPILATION_TIME.equals(propertyName)) {
                e.compilationTime = p.readLong();
            } else if (WatcherEvent.GARBAGE_COLLECTOR.equals(propertyName)) {
                e.garbageCollector_count = p.readLong();
                p.readOperator(p.syntax.PROPERTY_DIV);
                e.garbageCollector_time = p.readLong();
            } else {
                // property desconhecida, ignora
            }

            if (p.readOptionalOperator(';')) {
                propertyName = p.readIdentifierString();
            } else {
                break;
            }
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (counter ^ (counter >>> 32));
//        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (classLoading_loaded != other.classLoading_loaded) {
            return false;
        }
        if (classLoading_total != other.classLoading_total) {
            return false;
        }
        if (classLoading_unloaded != other.classLoading_unloaded) {
            return false;
        }
        if (compilationTime != other.compilationTime) {
            return false;
        }
        if (counter != other.counter) {
            return false;
        }
        if (garbageCollector_count != other.garbageCollector_count) {
            return false;
        }
        if (garbageCollector_time != other.garbageCollector_time) {
            return false;
        }
        if (heap_commited != other.heap_commited) {
            return false;
        }
        if (heap_init != other.heap_init) {
            return false;
        }
        if (heap_max != other.heap_max) {
            return false;
        }
        if (heap_used != other.heap_used) {
            return false;
        }
//        if (name == null) {
//            if (other.name != null) {
//                return false;
//            }
//        } else if (!name.equals(other.name)) {
//            return false;
//        }
        if (nonHeap_commited != other.nonHeap_commited) {
            return false;
        }
        if (nonHeap_init != other.nonHeap_init) {
            return false;
        }
        if (nonHeap_max != other.nonHeap_max) {
            return false;
        }
        if (nonHeap_used != other.nonHeap_used) {
            return false;
        }
        if (objectPendingFinalizationCount != other.objectPendingFinalizationCount) {
            return false;
        }
        if (runtime_freeMemory != other.runtime_freeMemory) {
            return false;
        }
        if (runtime_maxMemory != other.runtime_maxMemory) {
            return false;
        }
        if (runtime_totalMemory != other.runtime_totalMemory) {
            return false;
        }
        if (Double.doubleToLongBits(systemLoad) != Double
                .doubleToLongBits(other.systemLoad)) {
            return false;
        }
        if (time != other.time) {
            return false;
        }
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
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
