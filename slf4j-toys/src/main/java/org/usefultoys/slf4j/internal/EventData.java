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
package org.usefultoys.slf4j.internal;

import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Abstract class representing events collected by slf4j-toys.
 *
 * @author Daniel Felix Ferber
 */
public abstract class EventData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Session UUID of JVM where this event data was collected.
     */
    @Getter
    protected String sessionUuid = null;
    /**
     * Time ordered position for multiple occurrences of the same event.
     */
    @Getter
    protected long position = 0;
    /**
     * Timestamp when the event data was collected.
     */
    @Getter
    protected long time = 0;

    protected EventData() {
    }

    protected EventData(final String sessionUuid) {
        this.sessionUuid = sessionUuid;
        this.position = 0;
    }

    protected EventData(final String sessionUuid, final long position) {
        this.sessionUuid = sessionUuid;
        this.position = position;
    }

    protected EventData(final String sessionUuid, final long position, final long time) {
        this.sessionUuid = sessionUuid;
        this.position = position;
        this.time = time;
    }

    /** Increments {@link #position} and stores system time to {@link #time}. */
    protected final void nextPosition() {
        time = System.nanoTime();
        position++;
    }

    /**
     * Reverts all event attributes to their constructor initial value. Useful to reuse the event instance and avoid
     * creation of new objects.
     */
    protected final void reset() {
        sessionUuid = null;
        position = 0;
        time = 0;
        resetImpl();
    }

    protected void resetImpl() {
    }

    /**
     * Writes a concise, human-readable string representation of the event into the supplied StringBuilder.
     *
     * @param builder The StringBuilder that receives the string representation
     * @return The StringBuilder passed as argument to allow chained StringBuilder method calls.
     */
    protected abstract StringBuilder readableString(StringBuilder builder);

    public final String readableMessage() {
        return readableString(new StringBuilder(200)).toString();
    }

    public static final String SESSION_UUID = "s";
    public static final String EVENT_POSITION = "p";
    public static final String EVENT_TIME = "t";

    /**
     * Writes a JSON encoded representation of the event into the supplied StringBuilder.
     *
     * @param sb            The StringBuilder that receives the encoded representation.
     * @return The StringBuilder passed as argument to allow chained StringBuilder method calls.
     */
    protected final StringBuilder writeJson(final StringBuilder sb) {
        sb.append("{");
        sb.append(String.format(Locale.US, "%s:%s,%s:%d,%s:%d", SESSION_UUID, this.sessionUuid, EVENT_POSITION, this.position, EVENT_TIME, this.time));
        writeJsonImpl(sb);
        sb.append("}");
        return sb;
    }

    protected void writeJsonImpl(final StringBuilder sb) {
        // no-op
    }

    protected final String jsonMessage() {
        return writeJson(new StringBuilder(200)).toString();
    }
}
