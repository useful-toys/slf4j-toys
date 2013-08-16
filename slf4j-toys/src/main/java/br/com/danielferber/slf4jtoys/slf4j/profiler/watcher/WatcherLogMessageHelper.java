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
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.Syntax;
import java.io.IOException;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class WatcherLogMessageHelper {
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
    private static final char MESSAGE_PREFIX = 'W';
    

    public static void writeToString(LoggerMessageWriter w, WatcherEvent e) {
        w.openData(MESSAGE_PREFIX);

        /* uuid */
        if (e.uuid != null) {
            w.property(UUID, e.uuid);
        }

        /* counter */
        if (e.counter > 0) {
            w.property(COUNTER, e.counter);
        }

        /* time */
        if (e.time > 0) {
            w.property(TIME, e.time);
        }

        /* memory usage */
        if (e.runtime_freeMemory > 0 || e.runtime_totalMemory > 0 || e.runtime_maxMemory > 0) {
            w.property(MEMORY, e.runtime_totalMemory - e.runtime_freeMemory, e.runtime_totalMemory, e.runtime_maxMemory);
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

    public String extractPlausibleMessage(String s) {
        return LoggerMessageReader.extractPlausibleMessage(MESSAGE_PREFIX, s);
    }
    
    public void readFromString(LoggerMessageReader p, WatcherEvent e) throws IOException {
        /* Reseta todos os atributos. */
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

        while (p.hasMore()) {
            String propertyName = p.readIdentifier();
            if (COUNTER.equals(propertyName)) {
                e.counter = p.readLong();
            } else if (UUID.equals(propertyName)) {
                e.uuid = p.readString();
            } else if (TIME.equals(propertyName)) {
                e.time = p.readLong();
            } else if (MEMORY.equals(propertyName)) {
                e.runtime_freeMemory = p.readLong();
                e.runtime_totalMemory = p.readLong();
                e.runtime_totalMemory = p.readLong();
            } else if (HEAP.equals(propertyName)) {
                e.heap_commited = p.readLong();
                e.heap_init = p.readLong();
                e.heap_max = p.readLong();
                e.heap_used = p.readLong();
            } else if (NON_HEAP.equals(propertyName)) {
                e.nonHeap_commited = p.readLong();
                e.nonHeap_init = p.readLong();
                e.nonHeap_max = p.readLong();
                e.nonHeap_used = p.readLong();
            } else if (FINALIZATION_COUNT.equals(propertyName)) {
                e.objectPendingFinalizationCount = p.readLong();
            } else if (CLASS_LOADING.equals(propertyName)) {
                e.classLoading_total = p.readLong();
                e.classLoading_loaded = p.readLong();
                e.classLoading_unloaded = p.readLong();
            } else if (COMPILATION_TIME.equals(propertyName)) {
                e.compilationTime = p.readLong();
            } else if (GARBAGE_COLLECTOR.equals(propertyName)) {
                e.garbageCollector_count = p.readLong();
                e.garbageCollector_time = p.readLong();
            } else {
                // property desconhecida, ignora
            }
        }
    }
}
