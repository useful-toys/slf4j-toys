/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Daniel
 */
public abstract class EventData implements Serializable {
	private static final long serialVersionUID = 1L;
    /**
     * Unique ProfilingSession UUID.
     */
    protected String sessionUuid = null;
    /**
     * Identifier that categorizes events into groups.
     */
    protected String eventCategory = null;
    /**
     * The event position within the category.
     */
    protected long eventPosition = 0;
    /**
     * Timestamp when the event data was collected.
     */
    protected long time = 0;

    protected EventData() {
        super();
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public long getEventPosition() {
        return eventPosition;
    }

    public long getTime() {
        return time;
    }
    
    

    /**
     * Reverts all event attributes to their constructor initial value. Useful
     * to reuse the event instance and avoid creation of new objects.
     */
    protected final void reset() {
        this.sessionUuid = null;
        this.eventCategory = null;
        this.eventPosition = 0;
        this.time = 0;
        this.resetImpl();
    }

    protected abstract void resetImpl();

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sessionUuid == null) ? 0 : sessionUuid.hashCode());
        result = prime * result + ((eventCategory == null) ? 0 : eventCategory.hashCode());
        result = prime * result + (int) (eventPosition ^ (eventPosition >>> 32));
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventData other = (EventData) obj;
        return this.isSameAs(other);
    }

    @Override
    public final String toString() {
        if (sessionUuid != null) {
            return this.sessionUuid + ":" + this.eventPosition;
        } else {
            return Long.toString(this.eventPosition);
        }
    }

    /**
     * Indicates whether this event represents the same as the given argument,
     * based on session uuid, category and position.
     *
     * @param other the event with which to compare
     * @return <code>true</code> if this event represents the same * * * * * *
     * as <code>other</code> argument; <code>false</code> otherwise.
     */
    public final boolean isSameAs(EventData other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        if (eventPosition != other.eventPosition) {
            return false;
        }
        if (eventCategory == null) {
            if (other.eventCategory != null) {
                return false;
            }
        } else if (!eventCategory.equals(other.eventCategory)) {
            return false;
        }
        if (sessionUuid == null) {
            if (other.sessionUuid != null) {
                return false;
            }
        } else if (!sessionUuid.equals(other.sessionUuid)) {
            return false;
        }
        return true;
    }

    /**
     * Indicates weather all event collected attributes are equal than the
     * attributes collected by the event given as argument. Used only in unit
     * tests.
     *
     * @param other the event with which to compare
     * @return <code>true</code> if all attributes are equal between the *
     * events; <code>false</code> otherwise.
     */
    public final boolean isCompletelyEqualsTo(EventData other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        if (eventPosition != other.eventPosition) {
            return false;
        }
        if (eventCategory == null) {
            if (other.eventCategory != null) {
                return false;
            }
        } else if (!eventCategory.equals(other.eventCategory)) {
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

    protected abstract boolean isCompletelyEqualsImpl(EventData other);

    protected static final String SESSION_UUID = "s";
    protected static final String EVENT_POSITION = "p";
    protected static final String EVENT_CATEGORY = "c";
    private static final String EVENT_TIME = "t";

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
     * @param builder The StringBuilder that receives the encoded
     * representation.
     * @param messagePrefix A prefix character used by an parser to recognize
     * the encoded message.
     * @return The StringBuilder passed as argument to allow chained
     * StringBuilder method calls.
     */
    public final StringBuilder write(StringBuilder sb, char messagePrefix) {
        EventWriter w = new EventWriter(sb);
        w.open(messagePrefix);
        writeKeyProperties(w);
        writePropertiesImpl(w);
        w.close();
        return sb;
    }

    private void writeKeyProperties(EventWriter w) {
        /* Session UUID */
        if (this.sessionUuid != null) {
            w.property(SESSION_UUID, this.sessionUuid);
        }

        /* Event category */
        if (this.eventCategory != null) {
            w.property(EVENT_CATEGORY, this.eventCategory);
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

    protected abstract void writePropertiesImpl(EventWriter w);

    /**
     * Reads an events from the encoded string representation. If no event data
     * is recognized, then inconsistent data might have been loaded.
     *
     * @param message The string that is supposed to contain an encoded string
     * representation of the event.
     * @return <code>true</code> if an event was successfully read;
     * <code>false</code> otherwise.
     */
    public final boolean read(String message, char messagePrefix) {
        String plausibleMessage = PatternDefinition.extractPlausibleMessage(messagePrefix, message);
        if (plausibleMessage == null) {
            return false;
        }
        reset();
        EventReader eventReader = new EventReader();
        eventReader.reset(plausibleMessage);

        String key = null;
        try {
            while (eventReader.hasMore()) {
                key = eventReader.readPropertyName();
                if (! readKeyProperties(eventReader, key)) {
                    if (!readPropertyImpl(eventReader, key)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean readKeyProperties(EventReader eventReader, String key) throws IOException {
        if (SESSION_UUID.equals(key)) {
            this.sessionUuid = eventReader.readString();
            return true;
        } else if (EVENT_POSITION.equals(key)) {
            this.eventPosition = eventReader.readLong();
            return true;
        } else if (EVENT_CATEGORY.equals(key)) {
            this.eventCategory = eventReader.readString();
            return true;
        } else if (EVENT_TIME.equals(key)) {
            this.time = eventReader.readLong();
            return true;
        }
        return false;
    }

    /**
     *
     * Implementation shall provide an implementation that reads one or more
     * values (via given EventReader) from the encoded string and assign them to
     * the property represented by the given key.
     *
     * @param r The EventReader that is parsing the message.
     * @param key The key that represents the property.
     * @return true if the key was recognized, false otherwise.
     * @throws IOException the EventReader failed to parse the encoded string.
     */
    protected abstract boolean readPropertyImpl(EventReader r, String key) throws IOException;
}
