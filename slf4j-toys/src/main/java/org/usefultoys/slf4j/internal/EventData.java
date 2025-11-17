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
package org.usefultoys.slf4j.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Base class for events collected by `slf4j-toys`.
 * It provides common attributes for event identification and timestamping.
 * The responsibility for serialization is delegated to a corresponding Json5 serializer class.
 *
 * @author Daniel Felix Ferber
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED) // for tests only
@Getter
public class EventData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The unique identifier for the JVM session where this event data was collected.
     * This allows correlation of logs from the same JVM instance.
     */
    String sessionUuid = null; // Changed from protected to package-private
    /**
     * A time-ordered sequential position for multiple occurrences of the same event within a session.
     */
    long position = 0; // Changed from protected to package-private
    /**
     * The timestamp (in nanoseconds) when the event data was collected.
     */
    long lastCurrentTime = 0; // Changed from protected to package-private

    /**
     * Constructs an EventData instance with a specified session UUID.
     *
     * @param sessionUuid The unique identifier for the JVM session.
     */
    protected EventData(final String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    /**
     * Constructs an EventData instance with a specified session UUID and position.
     *
     * @param sessionUuid The unique identifier for the JVM session.
     * @param position The time-ordered sequential position of the event.
     */
    protected EventData(final String sessionUuid, final long position) {
        this.sessionUuid = sessionUuid;
        this.position = position;
    }

    /**
     * Updates the {@code lastCurrentTime} with the current system's high-resolution time.
     *
     * @return The updated {@code lastCurrentTime} in nanoseconds.
     */
    protected long collectCurrentTime() {
        return lastCurrentTime = System.nanoTime();
    }

    /**
     * Increments the event's position. If the position reaches {@code Long.MAX_VALUE},
     * it wraps around to 0 to prevent overflow.
     */
    protected void nextPosition() {
        if (position == Long.MAX_VALUE) {
            position = 0;
        } else {
            position++;
        }
    }

    /**
     * Reverts all event attributes to their initial values, as if newly constructed.
     * This is useful for reusing event instances to avoid object creation overhead.
     */
    public void reset() {
        sessionUuid = null;
        position = 0;
        lastCurrentTime = 0;
    }

    /**
     * Appends the object's properties to the JSON5-encoded string by delegating
     * to the appropriate Json5 serializer. Subclasses should override this method
     * to delegate to their own specific serializer.
     *
     * @param sb The StringBuilder to which the JSON5 properties are appended.
     */
    protected void writeJson5(final StringBuilder sb) {
        EventDataJson5.write(this, sb);
    }

    /**
     * Reads and parses event data from a JSON5-encoded string by delegating
     * to the appropriate Json5 serializer, populating the object's fields.
     *
     * @param json5 The JSON5-encoded string containing event data.
     */
    public void readJson5(final String json5) {
        EventDataJson5.read(this, json5);
    }
}
