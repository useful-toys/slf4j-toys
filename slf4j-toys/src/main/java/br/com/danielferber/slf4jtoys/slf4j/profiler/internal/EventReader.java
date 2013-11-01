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

import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.*;

/**
 * Provides methods that implement recurrent deserialization patterns. The
 * methods consist of a simplified parser of patterns produced by
 * {@link MessageWriter}.
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

    public EventReader reset(String encodedData) {
        firstProperty = true;
        firstValue = true;
        chars = encodedData.toCharArray();
        start = 0;
        lenght = chars.length;
        charsString = encodedData;
        return this;
    }

    public boolean hasMore() {
        return start < lenght;
    }

    /**
     * Read an identifier that defines the next incomming property.
     *
     * @return The name of the property.
     * @throws IOException Incomming chars are not a valid property identifier.
     */
    public String readPropertyName() throws IOException {
        if (!firstProperty) {
            readOperator(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        firstValue = true;

        if (start >= lenght) {
            throw new EOFException();
        }

        char c = chars[start];
        if (!Character.isJavaIdentifierStart(c)) {
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

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    public String readString() throws IOException {
        if (firstValue) {
            readOperator(PROPERTY_EQUALS);
            firstValue = false;
        } else {
            readOperator(PROPERTY_DIV);
        }

        if (start >= lenght) {
            throw new EOFException();
        }

        int end = start;
        while (end < lenght) {
            char c = chars[end];
            if (c == PROPERTY_DIV || c == PROPERTY_SEPARATOR) {
                break;
            } else if (c == QUOTE) {
                end++;
                if (end >= lenght) {
                    throw new EOFException();
                }
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    public long readLong() throws IOException {
        try {
            return Long.parseLong(readString());
        } catch (NumberFormatException e) {
            throw new IOException("invalid long", e);
        }
    }

    public long readLongOrZero() throws IOException {
        try {
            final String str = readString();
            if (str.isEmpty()) {
                return 0L;
            }
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new IOException("invalid long", e);
        }
    }

    public double readDouble() throws IOException {
        try {
            return Double.parseDouble(readString());
        } catch (NumberFormatException e) {
            throw new IOException("invalid double", e);
        }
    }

    public double readDoubleOrZero() throws IOException {
        try {
            final String str = readString();
            if (str.isEmpty()) {
                return 0F;
            }
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new IOException("invalid double", e);
        }
    }

    protected void readOperator(char operator) throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }
        char c = chars[start++];
        if (c != operator) {
            throw new IOException("expected: " + operator);
        }
    }

    public Map<String, String> readMap() throws IOException {
        if (firstValue) {
            readOperator(PROPERTY_EQUALS);
            firstValue = false;
        } else {
            readOperator(PROPERTY_DIV);
        }

        readOperator(MAP_OPEN);

        if (start >= lenght) {
            throw new EOFException();
        }
        char c = chars[start];
        if (c == MAP_CLOSE) {
            start++;
            return Collections.EMPTY_MAP;
        }
        Map<String, String> map = new TreeMap<String, String>();
        do {
            String key = readMapKey();
            readOperator(MAP_EQUAL);
            String value = readMapValue();
            map.put(key, value);
            c = chars[start];
            if (start >= lenght) {
                throw new EOFException();
            }
        } while (c != MAP_CLOSE);
                
        readOperator(MAP_CLOSE);

        return map;
    }

    public String readMapKey() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        int end = start;
        while (end < lenght) {
            char c = chars[end];
            if (c == MAP_EQUAL) {
                break;
            } else if (c == QUOTE) {
                end++;
                if (end >= lenght) {
                    throw new EOFException();
                }
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    public String readMapValue() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        int end = start;
        while (end < lenght) {
            char c = chars[end];
            if (c == MAP_SEPARATOR || c == MAP_CLOSE) {
                break;
            } else if (c == QUOTE) {
                end++;
                if (end >= lenght) {
                    throw new EOFException();
                }
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

}
