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

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for events collected by `slf4j-toys`.
 * It provides common attributes and methods for event identification, timestamping,
 * and serialization to human-readable and machine-parsable formats.
 *
 * @author Daniel Felix Ferber
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EventData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The unique identifier for the JVM session where this event data was collected.
     * This allows correlation of logs from the same JVM instance.
     */
    @Getter
    protected String sessionUuid = null;
    /**
     * A time-ordered sequential position for multiple occurrences of the same event within a session.
     */
    @Getter
    protected long position = 0;
    /**
     * The timestamp (in nanoseconds) when the event data was collected.
     */
    @Getter
    protected long lastCurrentTime = 0;

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
     * Constructs an EventData instance with a specified session UUID, position, and timestamp.
     * This constructor is primarily intended for testing purposes.
     *
     * @param sessionUuid The unique identifier for the JVM session.
     * @param position The time-ordered sequential position of the event.
     * @param lastCurrentTime The timestamp (in nanoseconds) when the data was collected.
     */
    protected EventData(final String sessionUuid, final long position, final long lastCurrentTime) {
        this.sessionUuid = sessionUuid;
        this.position = position;
        this.lastCurrentTime = lastCurrentTime;
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
     * Reverts all event attributes to their initial values, as if newly constructed.
     * This is useful for reusing event instances to avoid object creation overhead.
     */
    public void reset() {
        sessionUuid = null;
        position = 0;
        lastCurrentTime = 0;
    }

    /**
     * JSON5 key for the session UUID.
     */
    public static final String SESSION_UUID = "_";
    /**
     * JSON5 key for the event's sequential position.
     */
    public static final String EVENT_POSITION = "$";
    /**
     * JSON5 key for the event's timestamp.
     */
    public static final String EVENT_TIME = "t";

    /**
     * Subclasses must implement this method to append their specific properties
     * to the JSON5-encoded string. This method is called by {@link #json5Message()}.
     *
     * @param sb The StringBuilder to which the JSON5 properties are appended.
     */
    protected void writeJson5Impl(final StringBuilder sb) {
        sb.append(String.format(Locale.US, "%s:%s,%s:%d,%s:%d", SESSION_UUID, sessionUuid, EVENT_POSITION, position, EVENT_TIME, lastCurrentTime));
    }

    /**
     * Returns the machine-parsable, JSON5-encoded representation of the event.
     * This is an alias for {@link #json5Message()}.
     *
     * @return A string containing the JSON5-encoded message.
     */
    public final String json5Message() {
        final StringBuilder sb = new StringBuilder(200);
        sb.append("{");
        writeJson5Impl(sb);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Regular expression component for matching the start of a JSON5 object or a new field.
     */
    protected static final String REGEX_START = "[{,]";
    /**
     * Regular expression component for matching a string value in JSON5 (e.g., `: 'value'`).
     */
    protected static final String REGEX_STRING_VALUE = "\\s*:\\s*'([^']*)'";
    /**
     * Regular expression component for matching a word (non-string) value in JSON5 (e.g., `: value`).
     */
    protected static final String REGEX_WORD_VALUE = "\\s*:\\s*([^,}\\s]+)";

    private static final Pattern patternSession = Pattern.compile(REGEX_START+SESSION_UUID+REGEX_WORD_VALUE);
    private static final Pattern patternPosition = Pattern.compile(REGEX_START+"\\"+EVENT_POSITION+REGEX_WORD_VALUE);
    private static final Pattern patternTime = Pattern.compile(REGEX_START+EVENT_TIME+REGEX_WORD_VALUE);

    /**
     * Reads and parses event data from a JSON5-encoded string, populating the object's fields.
     *
     * @param json5 The JSON5-encoded string containing event data.
     */
    public void readJson5(final String json5) {
        final Matcher matcherSession = patternSession.matcher(json5);
        if (matcherSession.find()) {
            sessionUuid = matcherSession.group(1);
        }

        final Matcher matcherPosition = patternPosition.matcher(json5);
        if (matcherPosition.find()) {
            position = Long.parseLong(matcherPosition.group(1));
        }

        final Matcher matcherTime = patternTime.matcher(json5);
        if (matcherTime.find()) {
            lastCurrentTime = Long.parseLong(matcherTime.group(1));
        }
    }
}
