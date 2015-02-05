/* 
 * Copyright 2015 Daniel Felix Ferber.
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
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.PatternDefinition.encodeMapValuePattern;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.PatternDefinition.encodePropertyValuePattern;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.PatternDefinition.encodeReplacement;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MAP_CLOSE;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MAP_EQUAL;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MAP_OPEN;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MAP_SEPARATOR;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MESSAGE_CLOSE;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.MESSAGE_OPEN;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.PROPERTY_DIV;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.PROPERTY_EQUALS;
import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SyntaxDefinition.PROPERTY_SEPARATOR;

import java.util.Map;

public final class EventWriter {

    private transient boolean firstProperty;
    private transient final StringBuilder builder;

    EventWriter(final StringBuilder builder) {
        super();
        firstProperty = true;
        this.builder = builder;
    }

    void open(final char id) {
        builder.append(id);
        builder.append(MESSAGE_OPEN);
    }

    void close() {
        builder.append(MESSAGE_CLOSE);
    }

    public EventWriter property(final String name, final boolean value) {
        property(name, Boolean.toString(value));
        return this;
    }

    public EventWriter property(final String name, final long value) {
        property(name, Long.toString(value));
        return this;
    }

    public EventWriter property(final String name, final long value1, final long value2) {
        property(name, Long.toString(value1), Long.toString(value2));
        return this;
    }

    public EventWriter property(final String name, final long value1, final long value2, final long value3) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3));
        return this;
    }

    public EventWriter property(final String name, final long value1, final long value2, final long value3, final long value4) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3), Long.toString(value4));
        return this;
    }

    public EventWriter property(final String name, final double value) {
        property(name, Double.toString(value));
        return this;
    }

    public EventWriter property(final String name, final String value) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        writePropertyValue(value);
        return this;
    }

    public EventWriter property(final String name, final String value1, final String value2) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        writePropertyValue(value1);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value2);
        return this;
    }

    public EventWriter property(final String name, final String value1, final String value2, final String value3) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        writePropertyValue(value1);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value2);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value3);
        return this;
    }

    public EventWriter property(final String name, final String value1, final String value2, final String value3, final String value4) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        writePropertyValue(value1);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value2);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value3);
        builder.append(PROPERTY_DIV);
        writePropertyValue(value4);
        return this;
    }

    public EventWriter property(final String name, final Map<String, String> map) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        builder.append(MAP_OPEN);
        boolean firstEntry = true;
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            if (!firstEntry) {
                builder.append(MAP_SEPARATOR);
            } else {
                firstEntry = false;
            }
            final String key = entry.getKey();
            final String value = entry.getValue();
            builder.append(key);
            if (value != null) {
                builder.append(MAP_EQUAL);
                writeMapValue(value);
            }
        }
        builder.append(MAP_CLOSE);

        return this;
    }

    void writePropertyValue(final String value) {
        builder.append(encodePropertyValuePattern.matcher(value).replaceAll(encodeReplacement));
    }

    void writeMapValue(final String value) {
        builder.append(encodeMapValuePattern.matcher(value).replaceAll(encodeReplacement));
    }
}
