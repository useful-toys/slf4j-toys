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
package br.com.danielferber.slf4jtoys.slf4j.profiler.status;

import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageWriter;
import java.io.IOException;

/**
 *
 * @author Daniel Felix Ferber
 */
public abstract class LoggerMessageCodec<T extends SystemStatusData> extends br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageCodec<T> {

    protected static final String MEMORY = "m";
    protected static final String HEAP = "h";
    protected static final String NON_HEAP = "nh";
    protected static final String FINALIZATION_COUNT = "fc";
    protected static final String CLASS_LOADING = "cl";
    protected static final String COMPILATION_TIME = "ct";
    protected static final String GARBAGE_COLLECTOR = "gc";
    protected static final String SYSTEM_LOAD = "sl";

    protected LoggerMessageCodec(char messagePrefix) {
        super(messagePrefix);
    }
    
    protected void writeProperties(MessageWriter w, T e) {

        /* memory usage */
        if (e.runtime_usedMemory > 0 || e.runtime_totalMemory > 0 || e.runtime_maxMemory > 0) {
            w.property(MEMORY, e.runtime_usedMemory, e.runtime_totalMemory, e.runtime_maxMemory);
        }

        /* heap usage */
        if (e.heap_commited > 0 || e.heap_init > 0 || e.heap_max > 0 || e.heap_used > 0) {
            w.property(HEAP, e.heap_commited, e.heap_init, e.heap_max, e.heap_used);
        }

        /* non heap usage */
        if (e.nonHeap_commited > 0 || e.nonHeap_init > 0 || e.nonHeap_max > 0 || e.nonHeap_used > 0) {
            w.property(NON_HEAP, e.nonHeap_commited, e.nonHeap_init, e.nonHeap_max, e.nonHeap_used);
        }

        /* objectPendingFinalizationCount */
        if (e.objectPendingFinalizationCount > 0) {
            w.property(FINALIZATION_COUNT, e.objectPendingFinalizationCount);
        }

        /* class loading */
        if (e.classLoading_loaded > 0 || e.classLoading_total > 0 || e.classLoading_unloaded > 0) {
            w.property(CLASS_LOADING, e.classLoading_total, e.classLoading_loaded, e.classLoading_unloaded);
        }

        /* compiler */
        if (e.compilationTime > 0) {
            w.property(COMPILATION_TIME, e.compilationTime);
        }

        /* garbage collector. */
        if (e.garbageCollector_count > 0 || e.garbageCollector_time > 0) {
            w.property(GARBAGE_COLLECTOR, e.garbageCollector_count, e.garbageCollector_time);
        }

        /* system load */
        if (e.systemLoad > 0) {
            w.property(SYSTEM_LOAD, e.systemLoad);
        }

        w.closeData();
    }

    protected boolean readProperty(MessageReader p, String propertyName, T e) throws IOException {
        if (MEMORY.equals(propertyName)) {
            e.runtime_usedMemory = p.readLong();
            e.runtime_totalMemory = p.readLong();
            e.runtime_totalMemory = p.readLong();
            return true;
        } else if (HEAP.equals(propertyName)) {
            e.heap_commited = p.readLong();
            e.heap_init = p.readLong();
            e.heap_max = p.readLong();
            e.heap_used = p.readLong();
            return true;
        } else if (NON_HEAP.equals(propertyName)) {
            e.nonHeap_commited = p.readLong();
            e.nonHeap_init = p.readLong();
            e.nonHeap_max = p.readLong();
            e.nonHeap_used = p.readLong();
            return true;
        } else if (FINALIZATION_COUNT.equals(propertyName)) {
            e.objectPendingFinalizationCount = p.readLong();
            return true;
        } else if (CLASS_LOADING.equals(propertyName)) {
            e.classLoading_total = p.readLong();
            e.classLoading_loaded = p.readLong();
            e.classLoading_unloaded = p.readLong();
            return true;
        } else if (COMPILATION_TIME.equals(propertyName)) {
            e.compilationTime = p.readLong();
            return true;
        } else if (GARBAGE_COLLECTOR.equals(propertyName)) {
            e.garbageCollector_count = p.readLong();
            e.garbageCollector_time = p.readLong();
            return true;
        } 
        return false;
    }
}
