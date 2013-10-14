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
public abstract class EventData extends PatternDefinition implements Serializable {

    private final char messagePrefix;

    protected EventData(char messagePrefix) {
        super();
        this.messagePrefix = messagePrefix;
    }

    protected abstract void reset();

    public abstract StringBuilder readableString(StringBuilder builder);

    /**
     * Writes the event into the supplied StringBuilder.
     *
     * @param sb The shared StringBuilder that receives the encoded message.
     * @return 
     */
    public final StringBuilder write(StringBuilder sb) {
        EventWriter w = new EventWriter(sb);
        w.open(messagePrefix);
        writeProperties(w);
        w.close();
        return sb;
    }

    /**
     * Implementation shall resort to the supplied MessageWRiter encode each
     * relevant property.
     *
     * @param w The shared EventWriter
     */
    protected abstract void writeProperties(EventWriter w);

    /**
     * Read the event from the supplied string message. If the method fails to
     * recognize the message, then method returns false and might have load
     * inconsistent data into the supplied event.
     *
     * @param message The string that is supposed to contain a serialized event
     * @return true the string message contains a serialized event, false
     * otherwise
     * @throws IOException
     */
    public final boolean read(String message) throws IOException {
        String plausibleMessage = PatternDefinition.extractPlausibleMessage(messagePrefix, message);
        if (plausibleMessage == null) {
            return false;
        }
        reset();
        EventReader r = new EventReader();
        r.reset(message);

        while (r.hasMore()) {
            String propertyName = r.readPropertyName();
            if (!readProperty(r, propertyName)) {
                throw new IOException("unknown property");
            }
        }
        return true;
    }

    /**
     *
     * Implementation shall resort to the supplied MessageReader to decode data
     * for one individual property.
     *
     * @param r The helper EventReader parsing the message
     * @param propertyName The property to be extracted from the string
     * @return true if the property was recognized, false otherwise
     * @throws IOException
     */
    protected abstract boolean readProperty(EventReader r, String propertyName) throws IOException;
}
