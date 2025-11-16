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
package org.usefultoys.slf4j.watcher;

import org.usefultoys.slf4j.internal.SystemData;

/**
 * Extends {@link SystemData} with semantics specific to the {@link Watcher}.
 * This class is used for both collecting and reporting runtime metrics, and for deserializing
 * {@link Watcher} events.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 * @see SystemData
 */
public class WatcherData extends SystemData {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new WatcherData instance with the given session UUID.
     *
     * @param uuid The unique identifier for the current session.
     */
    protected WatcherData(final String uuid) {
        super(uuid);
    }

    /**
     * Constructs a new WatcherData instance with detailed system metrics.
     * This constructor is primarily intended for testing purposes.
     *
     * @param sessionUuid The unique identifier for the current session.
     * @param position The sequential position of this Watcher event.
     * @param lastCurrentTime The timestamp when the data was collected.
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
     * @param runtime_usedMemory The used memory reported by Runtime.
     * @param runtime_maxMemory The maximum memory reported by Runtime.
     * @param runtime_totalMemory The total memory reported by Runtime.
     * @param systemLoad The system CPU load.
     */
    protected WatcherData(final String sessionUuid, final long position, final long lastCurrentTime,
                       final long heap_commited, final long heap_max, final long heap_used,
                       final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                       final long objectPendingFinalizationCount,
                       final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded,
                       final long compilationTime, final long garbageCollector_count, final long garbageCollector_time,
                       final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory,
                       final double systemLoad) {
        super(sessionUuid, position, lastCurrentTime, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used,
                objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime,
                garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad);
    }

    /**
     * Appends a human-readable summary of the WatcherData to the provided StringBuilder.
     * This method customizes the output from {@link SystemData#readableStringBuilder(StringBuilder)}
     * to include memory usage, system load, and the session UUID.
     *
     * @param builder The StringBuilder to which the readable message will be appended.
     * @return The StringBuilder with the appended readable message.
     */
    @Override
    public StringBuilder readableStringBuilder(final StringBuilder builder) {
        WatcherDataFormatter.readableStringBuilder(this, builder);
        return builder;
    }
}
