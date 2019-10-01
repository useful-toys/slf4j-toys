/*
 * Copyright 2019 Daniel Felix Ferber
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

import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 * Augments the {@link SystemData} to semantics required by Watcher. Further, {@link Watcher} events are deserialized back to {@link WatcherData}.
 *
 * @author Daniel Felix Ferber
 */
public class WatcherData extends SystemData {

    private static final long serialVersionUID = 1L;
    public static final char DETAILED_MESSAGE_PREFIX = 'W';

    public WatcherData() {
    }

    @Override
    public StringBuilder readableString(final StringBuilder builder) {
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
            hasPrevious = true;
        }
        if (this.sessionUuid != null) {
            if (hasPrevious) {
                builder.append("; ");
            }
            builder.append("UUID: ");
            builder.append(this.sessionUuid);
        }
        return builder;
    }

    public final boolean read(final String message) {
        return this.read(message, DETAILED_MESSAGE_PREFIX);
    }

    public final String write() {
        return write(new StringBuilder(200), DETAILED_MESSAGE_PREFIX).toString();
    }

    public final String readableWrite() {
        return readableString(new StringBuilder(200)).toString();
    }

}
