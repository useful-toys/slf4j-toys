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

import lombok.Getter;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        position = 0;
    }

    protected EventData(final String sessionUuid, final long position) {
        this.sessionUuid = sessionUuid;
        this.position = position;
    }

    // For tests
    protected EventData(final String sessionUuid, final long position, final long time) {
        this.sessionUuid = sessionUuid;
        this.position = position;
        this.time = time;
    }

    /**
     * Reverts all event attributes to their constructor initial value. Useful
     * to reuse the event instance and avoid creation of new objects.
     */
    public final void reset() {
        sessionUuid = null;
        position = 0;
        time = 0;
        resetImpl();
    }

    /**
     * Subclasses shall provide an implementation that resets its specific
     * properties to their constructor initial value. This method is called once
     * and shall compare all specific properties.
     */
    protected void resetImpl() {
        // no-op
    }

    public static final String SESSION_UUID = "_";
    public static final String EVENT_POSITION = "$";
    public static final String EVENT_TIME = "t";

    /**
     * Writes a concise, human readable string representation of the event into
     * the supplied StringBuilder.
     *
     * @param builder The StringBuilder that receives the string representation
     * @return The StringBuilder passed as argument to allow chained
     * StringBuilder method calls.
     */
    protected abstract StringBuilder readableStringBuilder(StringBuilder builder);

    public final String readableMessage() {
        return readableStringBuilder(new StringBuilder(200)).toString();
    }

    /**
     * Writes a JSON encoded representation of the event into the supplied StringBuilder.
     *
     * @param sb            The StringBuilder that receives the encoded representation.
     * @return The StringBuilder passed as argument to allow chained StringBuilder method calls.
     */
    protected final StringBuilder writeJson5(final StringBuilder sb) {
        sb.append("{");
        sb.append(String.format(Locale.US, "%s:%s,%s:%d,%s:%d", SESSION_UUID, sessionUuid, EVENT_POSITION, position, EVENT_TIME, time));
        writeJson5Impl(sb);
        sb.append("}");
        return sb;
    }

    protected void writeJson5Impl(final StringBuilder sb) {
        // no-op
    }

    public final String json5Message() {
        return writeJson5(new StringBuilder(200)).toString();
    }
    protected static final String REGEX_START = "[{,]";
    protected static final String REGEX_STRING_VALUE = "\\s*:\\s*'([^']*)'";
    protected static final String REGEX_WORD_VALUE = "\\s*:\\s*([^,}\\s]+)";

    private static final Pattern patternSession = Pattern.compile(REGEX_START+SESSION_UUID+REGEX_WORD_VALUE);
    private static final Pattern patternPosition = Pattern.compile(REGEX_START+"\\"+EVENT_POSITION+REGEX_WORD_VALUE);
    private static final Pattern patternTime = Pattern.compile(REGEX_START+EVENT_TIME+REGEX_WORD_VALUE);


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
            time = Long.parseLong(matcherTime.group(1));
        }
    }

    public final String encodedMessage() {
        return json5Message();
    }
}
