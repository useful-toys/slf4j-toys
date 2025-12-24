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

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A package-private utility class responsible for serializing and deserializing
 * {@link EventData} objects to and from a JSON5-like string format.
 * This isolates the serialization logic from the data-holding responsibilities
 * of the {@link EventData} class.
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
class EventDataJson5 {

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

    public final String REGEX_START = "[{,]\\s*";
    public final String REGEX_WORD_VALUE = "\\s*:\\s*([^,}\\s]+)";
    public final String REGEX_3_TUPLE = "\\s*:\\s*\\[([^,}\\s]+),([^,}\\s]+),([^,}\\s]+)\\]";
    public final String REGEX_2_TUPLE = "\\s*:\\s*\\[([^,}\\s]+),([^,}\\s]+)\\]";

    private final Pattern patternSession = Pattern.compile(REGEX_START + SESSION_UUID + REGEX_WORD_VALUE);
    private final Pattern patternPosition = Pattern.compile(REGEX_START + "\\" + EVENT_POSITION + REGEX_WORD_VALUE);
    private final Pattern patternTime = Pattern.compile(REGEX_START + EVENT_TIME + REGEX_WORD_VALUE);

    /**
     * Appends the JSON5 representation of the {@link EventData} object's fields
     * to the provided {@link StringBuilder}.
     *
     * @param data The {@link EventData} object to serialize.
     * @param sb   The {@link StringBuilder} to append to.
     */
    void write(final EventData data, final StringBuilder sb) {
        sb.append(String.format(Locale.US, "%s:%s,%s:%d,%s:%d",
                SESSION_UUID, data.getSessionUuid(),
                EVENT_POSITION, data.getPosition(),
                EVENT_TIME, data.getLastCurrentTime()));
    }

    /**
     * Reads and parses event data from a JSON5-encoded string, populating the
     * fields of the provided {@link EventData} object.
     *
     * @param data  The {@link EventData} object to populate.
     * @param json5 The JSON5-encoded string.
     */
    void read(final EventData data, final String json5) {
        final Matcher matcherSession = patternSession.matcher(json5);
        if (matcherSession.find()) {
            data.sessionUuid = matcherSession.group(1);
        }

        final Matcher matcherPosition = patternPosition.matcher(json5);
        if (matcherPosition.find()) {
            data.position = Long.parseLong(matcherPosition.group(1));
        }

        final Matcher matcherTime = patternTime.matcher(json5);
        if (matcherTime.find()) {
            data.lastCurrentTime = Long.parseLong(matcherTime.group(1));
        }
    }
}
