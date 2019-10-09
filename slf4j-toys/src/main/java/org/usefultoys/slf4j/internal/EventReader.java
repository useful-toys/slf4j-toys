/*
 * Copyright 2019 Daniel Felix Ferber
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

import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.usefultoys.slf4j.internal.SyntaxDefinition.*;

/**
 * Provides methods that implement recurrent deserialization patterns. The
 * methods consist of a simplified parser of patterns produced by
 * {@link EventWriter}.
 * <p>
 * To ease deserialization of one event and to reduce the amount of parameters,
 * EventReader keeps state of the deserialization of the event. For sake of
 * simplicity, the EventReader automatically consumes separators.
 * <p>
 * Thus, the instance might be shared and reused to reduce object creation
 * overhead, as long as events are deserialized one after the other and within
 * the same thread.
 *
 * @author Daniel Felix Ferber
 */
public class EventReader {

    /* Internal parser state. */
    private boolean firstProperty = true;
    private boolean firstValue = true;
    private int start;
    private String charsString;
    private char[] chars;
    private int lenght;

    public EventReader reset(final String encodedData) {
        firstProperty = true;
        firstValue = true;
        chars = encodedData.toCharArray();
        start = 0;
        lenght = chars.length;
        charsString = encodedData;
        return this;
    }

    /**
     * @return If there are more potentially incoming property.
     */
    public boolean hasMore() {
        return start < lenght;
    }

    /**
     * Read a name of the next expected incoming property.
     *
     * @return The name of the property.
     * @throws IOException Incoming chars are not a valid property name.
     */
    public String readPropertyName() throws IOException {
        if (!firstProperty) {
            readSymbol(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        firstValue = true;
        return readPropertyKey();
    }

    /**
     * Read the property value as string. May be called several times if the
     * property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public String readString() throws IOException {
        if (firstValue) {
            readSymbol(PROPERTY_EQUALS);
            firstValue = false;
        } else {
            readSymbol(PROPERTY_DIV);
        }
        return readPropertyValue();
    }

    /**
     * Read the property value as boolean. May be called several times if the
     * property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public boolean readBoolean() throws IOException {
        return Boolean.parseBoolean(readString());
    }

    /**
     * Read the property value as enumeration. May be called several times if
     * the property value is a tuple.
     *
     * @param <T> type of enumeration
     * @param c class of enumeration
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public <T extends Enum<T>> T readEnum(final Class<T> c) throws IOException {
        try {
            return Enum.valueOf(c, readString().toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IOException("invalid enum", e);
        }
    }

    /**
     * Read the property value as long integer. May be called several times if
     * the property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public long readLong() throws IOException {
        try {
            return Long.parseLong(readString());
        } catch (final NumberFormatException e) {
            throw new IOException("invalid long", e);
        }
    }

    /**
     * Read the property value as long integer or zero if values is empty. May
     * be called several times if the property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public long readLongOrZero() throws IOException {
        try {
            final String str = readString();
            if (str.isEmpty()) {
                return 0L;
            }
            return Long.parseLong(str);
        } catch (final NumberFormatException e) {
            throw new IOException("invalid long", e);
        }
    }

    /**
     * Read the property value as double. May be called several times if the
     * property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public double readDouble() throws IOException {
        try {
            return Double.parseDouble(readString());
        } catch (final NumberFormatException e) {
            throw new IOException("invalid double", e);
        }
    }

    /**
     * Read the property value as double or zero if values is empty. May be
     * called several times if the property value is a tuple.
     *
     * @return the property value
     * @throws IOException Incoming chars are not a valid property value.
     */
    public double readDoubleOrZero() throws IOException {
        try {
            final String str = readString();
            if (str.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(str);
        } catch (final NumberFormatException e) {
            throw new IOException("invalid double", e);
        }
    }

    /**
     * Consumes an expected symbol.
     *
     * @param symbol the expected symbol.
     * @throws IOException All chars were consumed or incoming char is not the expected symbol.
     */
    protected void readSymbol(final char symbol) throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }
        final char c = chars[start++];
        if (c != symbol) {
            throw new IOException("expected: " + symbol);
        }
    }

    /**
     * Consumes an optional symbol if available.
     *
     * @param symbol the optionally expected symbol.
     * @return true if the consumed char was the symbol, false otherwise.
     * @throws IOException All chars were consumed.
     */
    protected boolean readOptionalSymbol(final char symbol) throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }
        final char c = chars[start];
        if (c == symbol) {
            start++;
            return true;
        }
        return false;
    }

    /**
     * Read the property value as a map of string key and values.
     *
     * @return the map
     * @throws IOException Incoming chars are not a valid property value.
     */
    public Map<String, String> readMap() throws IOException {
        if (firstValue) {
            readSymbol(PROPERTY_EQUALS);
            firstValue = false;
        } else {
            readSymbol(PROPERTY_DIV);
        }

        readSymbol(MAP_OPEN);

        if (readOptionalSymbol(MAP_CLOSE)) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new TreeMap<String, String>();
        do {
            final String key = readMapKey();
            String value = null;
            if (readOptionalSymbol(MAP_EQUAL)) {
                value = readMapValue();
            }
            map.put(key, value);
        } while (readOptionalSymbol(MAP_SEPARATOR));

        readSymbol(MAP_CLOSE);

        return map;
    }

    protected String readMapKey() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        return readStringImp(MAP_EQUAL, MAP_SEPARATOR, MAP_CLOSE);
    }

    protected String readMapValue() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        return readStringImp(MAP_SEPARATOR, MAP_CLOSE, Character.MIN_VALUE);

    }

    protected String readPropertyValue() throws EOFException {
        if (start >= lenght) {
            throw new EOFException();
        }

        return readStringImp(PROPERTY_DIV, PROPERTY_SEPARATOR, Character.MIN_VALUE);
    }

    protected String readPropertyKey() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        char c = chars[start];
        if (!Character.isJavaIdentifierStart(c) && c != '#') {
            throw new IOException("invalid identifier");
        }
        int end = start + 1;
        while (end < lenght) {
            c = chars[end];
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            end++;
        }

        final String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    private String readStringImp(final char delimiter1, final char delimiter2, final char delimiter3) throws EOFException {
        final StringBuilder sb = new StringBuilder();
        int end = start;
        while (end < lenght) {
            final char c = chars[end];
            if (c == delimiter1 || c == delimiter2 || c == delimiter3) {
                break;
            } else if (c == QUOTE) {
                sb.append(charsString.substring(start, end));
                end++;
                if (end >= lenght) {
                    throw new EOFException();
                }
                start = end;
            }
            end++;
        }

        sb.append(charsString.substring(start, end));
        start = end;
        return sb.toString();
    }
}
