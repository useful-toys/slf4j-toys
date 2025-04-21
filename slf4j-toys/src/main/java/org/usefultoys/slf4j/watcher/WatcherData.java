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
package org.usefultoys.slf4j.watcher;

import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 * Augments the {@link SystemData} to semantics required by Watcher. Further, {@link Watcher} events are deserialized
 * back to {@link WatcherData}.
 *
 * @author Daniel Felix Ferber
 */
public class WatcherData extends SystemData {

    private static final long serialVersionUID = 1L;
    public static final char DETAILED_MESSAGE_PREFIX = 'W';

    public WatcherData() {
    }

    protected WatcherData(final String uuid) {
        super(uuid);
    }

    public WatcherData(final String sessionUuid, final long position) {
        super(sessionUuid, position);
    }

    public WatcherData(final String sessionUuid, final long position, final long time) {
        super(sessionUuid, position, time);
    }

    public WatcherData(final String sessionUuid, final long position, final long time,
                       final long heap_commited, final long heap_max, final long heap_used,
                       final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                       final long objectPendingFinalizationCount,
                       final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded,
                       final long compilationTime, final long garbageCollector_count, final long garbageCollector_time,
                       final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory,
                       final double systemLoad) {
        super(sessionUuid, position, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used,
                objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime,
                garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad);
    }

    @Override
    public StringBuilder readableStringBuilder(final StringBuilder builder) {
        boolean hasPrevious = false;
        if (this.runtime_usedMemory > 0 || this.runtime_maxMemory > 0 || this.runtime_totalMemory > 0) {
            builder.append("Memory: ");
            builder.append(UnitFormatter.bytes(this.runtime_usedMemory));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(this.runtime_totalMemory));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(this.runtime_maxMemory));
            hasPrevious = true;
        }
        if (this.systemLoad > 0) {
            if (hasPrevious) {
                builder.append("; ");
            }
            builder.append("System load: ");
            builder.append(Math.round(this.systemLoad * 100));
            builder.append("%");
            hasPrevious = true;
        }
        if (SessionConfig.uuidSize != 0 && this.sessionUuid != null) {
            if (hasPrevious) {
                builder.append("; ");
            }
            builder.append("UUID: ");
            builder.append(this.sessionUuid.substring(SessionConfig.UUID_LENGTH - SessionConfig.uuidSize));
        }
        return builder;
    }
}
