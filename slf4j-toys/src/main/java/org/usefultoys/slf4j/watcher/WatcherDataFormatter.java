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

import lombok.experimental.UtilityClass;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 * Formats {@link WatcherData} into a human-readable string representation.
 * This class centralizes the logic for building log messages for {@link Watcher} events.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
class WatcherDataFormatter {
    /**
     * Appends a human-readable summary of the WatcherData to the provided StringBuilder.
     *
     * @param data    The WatcherData object to format.
     * @param builder The StringBuilder to which the readable message will be appended.
     */
    public static void readableStringBuilder(final WatcherData data, final StringBuilder builder) {
        boolean hasPrevious = false;
        if (data.getRuntime_usedMemory() > 0 || data.getRuntime_maxMemory() > 0 || data.getRuntime_totalMemory() > 0) {
            builder.append("Memory: ");
            builder.append(UnitFormatter.bytes(data.getRuntime_usedMemory()));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(data.getRuntime_totalMemory()));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(data.getRuntime_maxMemory()));
            hasPrevious = true;
        }
        if (data.getSystemLoad() > 0) {
            if (hasPrevious) {
                builder.append("; ");
            }
            builder.append("System load: ");
            builder.append(Math.round(data.getSystemLoad() * 100));
            builder.append("%");
        }
        if (data.getSessionUuid() != null) {
            if (hasPrevious) {
                builder.append("; ");
            }
            builder.append("UUID: ");
            builder.append(data.getSessionUuid());
        }

    }
}