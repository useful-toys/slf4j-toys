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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Base class for events collected by `slf4j-toys`.
 * It provides common attributes for event identification and timestamping.
 * The responsibility for serialization is delegated to a corresponding Json5 serializer class.
 *
 * @author Daniel Felix Ferber
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Setter(AccessLevel.PROTECTED) // To support unit test cases
    long lastCurrentTime = 0; // Changed from protected to package-private

    /**
     * The time source used for collecting timestamps.
     * <p>
     * Defaults to {@link SystemTimeSource#INSTANCE}, which uses {@link System#nanoTime()}.
     * Can be replaced with a custom implementation for deterministic testing.
     * <p>
     * This field is transient because time sources are not meant to be serialized.
     * After deserialization, the default system time source will be used.
     * <p>
     * For more details on the design rationale, see TDR-0032: Clock Abstraction Pattern
     * for Deterministic Time-Based Testing.
     *
     * @see TimeSource
     * @see SystemTimeSource
     */
    @Setter(AccessLevel.PROTECTED) // To support unit test cases
    protected transient TimeSource timeSource = SystemTimeSource.INSTANCE;

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
     * Constructs an EventData instance with all fields specified (for testing).
     *
     * @param sessionUuid The unique identifier for the JVM session.
     * @param position The time-ordered sequential position of the event.
     * @param lastCurrentTime The timestamp when the event was collected.
     */
    protected EventData(final String sessionUuid, final long position, final long lastCurrentTime) {
        this.sessionUuid = sessionUuid;
        this.position = position;
        this.lastCurrentTime = lastCurrentTime;
    }

    /**
     * Updates the {@code lastCurrentTime} with the current time from the configured time source.
     * <p>
     * By default, this delegates to {@link SystemTimeSource#INSTANCE}, which uses {@link System#nanoTime()}.
     * In tests, a custom {@link TimeSource} can be set via {@link #setTimeSource(TimeSource)}
     * to enable deterministic time-based testing.
     *
     * @return The updated {@code lastCurrentTime} in nanoseconds.
     * @see TimeSource#nanoTime()
     */
    protected final long collectCurrentTime() {
        return lastCurrentTime = timeSource.nanoTime();
    }


    /**
     * Increments the event's position. If the position reaches {@code Long.MAX_VALUE},
     * it wraps around to 0 to prevent overflow.
     */
    protected final void nextPosition() {
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
