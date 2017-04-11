/*
 * Copyright 2017 Daniel Felix Ferber
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

import java.io.IOException;
import java.io.Serializable;

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
    protected String sessionUuid = null;
    /**
     * Time ordered position for multiple occurrences of the same event.
     */
    protected long eventPosition = 0;
    /**
     * Timestamp when the event data was collected.
     */
    protected long time = 0;

    protected EventData() {
    }

    /**
     * @return Session UUID of JVM where this event data was collected.
     */
    public String getSessionUuid() {
        return sessionUuid;
    }

    /**
     * @return Time ordered position for multiple occurrences of the same event.
     */
    public long getEventPosition() {
        return eventPosition;
    }

    public void setEventPosition(long eventPosition) {
        this.eventPosition = eventPosition;
    }

    public void setSessionUuid(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return Timestamp when the event data was collected.
     */
    public long getTime() {
        return time;
    }

    /**
     * Reverts all event attributes to their constructor initial value. Useful
     * to reuse the event instance and avoid creation of new objects.
     */
    protected final void reset() {
        this.sessionUuid = null;
        this.eventPosition = 0;
        this.time = 0;
        this.resetImpl();
    }

    /**
     * Subclasses shall provide an implementation that resets its specific
     * properties to their constructor initial value. This method is called once
     * and shall compare all specific properties.
     */
    protected abstract void resetImpl();

    /**
     * Indicates weather all properties are equal to the respective properties
     * of the other instance given as argument. Used only in unit tests.
     *
     * @param other the other EventData instance been compared to.
     * @return true if all properties are equal on the other instance, false
     * otherwise.
     */
    public final boolean isCompletelyEqualsTo(final EventData other) {
        if (other == null) {
            throw new IllegalArgumentException("other == null");
        }
        if (eventPosition != other.eventPosition) {
            return false;
        }
        if (sessionUuid == null) {
            if (other.sessionUuid != null) {
                return false;
            }
        } else if (!sessionUuid.equals(other.sessionUuid)) {
            return false;
        }
        if (time != other.time) {
            return false;
        }
        return isCompletelyEqualsImpl(other);
    }

    /**
     * Subclasses shall provide an implementation that compares its specific
     * properties to the respective properties of the other instance given as
     * argument. This method is called once and shall compare all specific
     * properties.
     *
     * @param other the other EventData instance been compared to.
     * @return true if all specific properties are equal on the other instance,
     * false otherwise.
     */
    protected abstract boolean isCompletelyEqualsImpl(EventData other);

    public static final String SESSION_UUID = "s";
    public static final String EVENT_POSITION = "#";
    public static final String EVENT_TIME = "t";

    /**
     * Writes a concise, human readable string representation of the event into
     * the supplied StringBuilder.
     *
     * @param builder The StringBuilder that receives the string representation
     * @return The StringBuilder passed as argument to allow chained
     * StringBuilder method calls.
     */
    public abstract StringBuilder readableString(StringBuilder builder);

    /**
     * Writes an encoded string representation of the event into the supplied
     * StringBuilder.
     *
     * @param sb            The StringBuilder that receives the encoded representation.
     * @param messagePrefix A prefix character used by an parser to recognize
     *                      the encoded message.
     * @return The StringBuilder passed as argument to allow chained
     * StringBuilder method calls.
     */
    public final StringBuilder write(final StringBuilder sb, final char messagePrefix) {
        final EventWriter w = new EventWriter(sb);
        w.open(messagePrefix);
        writeKeyProperties(w);
        writePropertiesImpl(w);
        w.close();
        return sb;
    }

    private void writeKeyProperties(final EventWriter w) {
        /* Session UUID */
        if (this.sessionUuid != null) {
            w.property(SESSION_UUID, this.sessionUuid);
        }
        /* Event position */
        if (this.eventPosition > 0) {
            w.property(EVENT_POSITION, this.eventPosition);
        }

        /* Event time */
        if (this.time > 0) {
            w.property(EVENT_TIME, this.time);
        }
    }

    /**
     * Subclasses shall provide an implementation that appends its specific
     * properties to the encoded string representation. This method is called
     * once and shall append all specific properties using the EventWriter.
     *
     * @param w The EventWriter that encodes the properties.
     */
    protected abstract void writePropertiesImpl(EventWriter w);

    /**
     * Reads an event from the encoded string representation. If string is not
     * well formed, returns {@code false}, but some properties may already have
     * been assigned.
     *
     * @param message       The string that is supposed to contain an encoded string
     *                      representation of the event.
     * @param messagePrefix message prefix
     * @return {@code true} if an event was successfully read;
     * {@code false} otherwise.
     */
    protected final boolean read(final String message, final char messagePrefix) {
        final String plausibleMessage = PatternDefinition.extractPlausibleMessage(messagePrefix, message);
        if (plausibleMessage == null) {
            return false;
        }
        reset();
        final EventReader eventReader = new EventReader();
        eventReader.reset(plausibleMessage);

        try {
            while (eventReader.hasMore()) {
                final String key = eventReader.readPropertyName();
                if (!readKeyProperties(eventReader, key)) {
                    if (!readPropertyImpl(eventReader, key)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (final IOException ignored) {
            return false;
        }
    }

    private boolean readKeyProperties(final EventReader eventReader, final String key) throws IOException {
        if (SESSION_UUID.equals(key)) {
            this.sessionUuid = eventReader.readString();
            return true;
        } else if (EVENT_POSITION.equals(key)) {
            this.eventPosition = eventReader.readLong();
            return true;
        } else if (EVENT_TIME.equals(key)) {
            this.time = eventReader.readLong();
            return true;
        }
        return false;
    }

    /**
     * Subclasses shall provide an implementation that reads its specific
     * properties from the encoded string representation. This method is called
     * for each encoded property.
     *
     * @param reader The EventReader that is parsing the message. Use this parser to
     *               retrieve the property value.
     * @param key    The property key.
     * @return true if the property key was recognized, false otherwise.
     * @throws IOException the EventReader failed to parse the encoded property
     *                     value.
     */
    protected abstract boolean readPropertyImpl(EventReader reader, String key) throws IOException;
}
