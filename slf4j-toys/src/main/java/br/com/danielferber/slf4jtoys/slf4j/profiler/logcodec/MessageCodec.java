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
package br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventData;
import java.io.IOException;

/**
 * Defines the strategy to serialize/deserialize data into/from log messages. It
 * orchestrates a MessageWriter or a MessageReader and calls abstract methods
 * that delegate the properties to the implementation.
 *
 * @param <T> Type of event to serialize/deserialize by the implementation.
 * @author Daniel Felix Ferber
 */
public abstract class MessageCodec<T extends EventData> {

    protected final char messagePrefix;

    protected MessageCodec(char messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    /**
     * Writes an event into the supplied StringBuilder. It resorts to the
     * supplied MessageWriter shared within the same thread. The previous
     * content of the StringBuilder is reset.
     * <p>
     * Although the StringBuilder keeps internal state of the MessageCodec, its
     * lifecycle is controlled outside to allows reuse of the StringBuilder and
     * avoid creation of objets.
     *
     * @param sb The shared StringBuilder that receives the serialized message.
     * @param w The shared MessageWriter
     * @param e The event to serialized
     */
    public final void writeLogMessage(StringBuilder sb, MessageWriter w, T e) {
        /* Restart the MessageWriter with a new StringBuilder. */
        w.reset(sb);
        /* Write properties properly enclosed by the message delimiters. */
        w.openData(messagePrefix);
        writeProperties(w, e);
        w.closeData();
    }

    /**
     * Implementation shall resort to the supplied MessageWRiter serialize each
     * relevant property of the supplied event.
     *
     * @param w The shared MessageWriter
     * @param e The event to serialized
     */
    protected abstract void writeProperties(MessageWriter w, T e);

    /**
     * Extracts a serialized event if found within the supplied string.
     *
     * @param s The string to analyse
     * @return The substring that may contain a serialized event or null if the
     * string does not look like to contain a serialized event.
     */
    protected String extractPlausibleMessage(String s) {
        return MessageReader.extractPlausibleMessage(messagePrefix, s);
    }

    /**
     * Read an event from the supplied string message. It resorts to the
     * supplied MessageReader shared within the same thread. The data is written
     * into the supplied event. If the method fails to recognize the message,
     * then method returns false and might have load inconsistent data into the
     * supplied event.
     *
     * @param message The string that is supposed to contain a serialized event
     * @param r The shared MessageReader
     * @param e The event that receives data read from the string
     * @return True the string message contains a serialized event, false
     * otherwise
     * @throws IOException
     */
    public boolean readLogMessage(String message, MessageReader r, T e) throws IOException {
        String plausibleMessage = extractPlausibleMessage(message);
        if (plausibleMessage == null) {
            return false;
        }
        e.reset();
        while (r.hasMore()) {
            String propertyName = r.readIdentifier();
            if (!readProperty(r, propertyName, e)) {
                throw new IOException("unknown property");
            }
        }
        return true;
    }

    /**
     *
     * Implementation shall resort to the supplied MessageReader to serialize
     * individual properties.
     * @param r The shared MessageReader
     * @param propertyName The property to be extracted from the string
     * @param e The event that receives the value of the property
     * @return True if the property was recognized
     * @throws IOException
     */
    protected abstract boolean readProperty(MessageReader r, String propertyName, T e) throws IOException;
}
