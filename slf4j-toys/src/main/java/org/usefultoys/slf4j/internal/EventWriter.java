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

import java.util.Map;
import java.util.Map.Entry;

import static org.usefultoys.slf4j.internal.PatternDefinition.*;
import static org.usefultoys.slf4j.internal.SyntaxDefinition.*;

/**
 * Provides methods that implement recurrent serialization patterns.
 * These patterns are recognized by {@link EventReader}.
 * <p>
 * To ease serialization of one event and to reduce the amount of parameters,
 * EventWrite keeps state of the serialization of the event. For sake of
 * simplicity, the EventWrite automatically produces separators.
 * <p>
 * Thus, the instance might be shared and reused to reduce object creation
 * overhead, as long as events are serialized one after the other and within
 * the same thread.
 *
 * @author Daniel Felix Ferber
 */
public final class EventWriter {

    private transient boolean firstProperty;
    private transient final StringBuilder builder;

    /**
     * Constructor.
     *
     * @param builder StringBuilder where encoded event is appended to.
     */
    EventWriter(final StringBuilder builder) {
        firstProperty = true;
        this.builder = builder;
    }

    /**
     * Writes the delimiter that starts the encoded string.
     *
     * @param prefix Prefix that identifies strings containing an encoded event
     */
    void open(final char prefix) {
        builder.append(prefix);
        builder.append(MESSAGE_OPEN);
    }

    /**
     * Writes the delimiter that ends the encoded string.
     */
    void close() {
        builder.append(MESSAGE_CLOSE);
    }

    /**
     * Writes a property whose value is an enumeration.
     *
     * @param name  property name
     * @param value property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final Enum<?> value) {
        property(name, value.name());
        return this;
    }

    /**
     * Writes a property whose value is a boolean.
     *
     * @param name  property name
     * @param value property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final boolean value) {
        property(name, Boolean.toString(value));
        return this;
    }

    /**
     * Writes a property whose value is a long integer.
     *
     * @param name  property name
     * @param value property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final long value) {
        property(name, Long.toString(value));
        return this;
    }

    /**
     * Writes a property whose value is a tuple of two long integers.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final long value1, final long value2) {
        property(name, Long.toString(value1), Long.toString(value2));
        return this;
    }

    /**
     * Writes a property whose value is a tuple of three long integers.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @param value3 property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final long value1, final long value2, final long value3) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3));
        return this;
    }

    /**
     * Writes a property whose value is a tuple of four long integers.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @param value3 property value
     * @param value4 property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final long value1, final long value2, final long value3, final long value4) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3), Long.toString(value4));
        return this;
    }

    /**
     * Writes a property whose value is a long double.
     *
     * @param name  property name
     * @param value property value
     * @return itself for chained method calls.
     */
    public EventWriter property(final String name, final double value) {
        property(name, Double.toString(value));
        return this;
    }

    /**
     * Writes a property whose value is a string.
     *
     * @param name  property name
     * @param value property value
     * @return itself for chained method calls.
     */
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

    /**
     * Writes a property whose value is a tuple of tow strings.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @return itself for chained method calls.
     */
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

    /**
     * Writes a property whose value is a tuple of three strings.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @param value3 property value
     * @return itself for chained method calls.
     */
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

    /**
     * Writes a property whose value is a tuple of four strings.
     *
     * @param name   property name
     * @param value1 property value
     * @param value2 property value
     * @param value3 property value
     * @param value4 property value
     * @return itself for chained method calls.
     */
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

    /**
     * Writes a property whose value is a map.
     *
     * @param name property name
     * @param map  property value
     * @return itself for chained method calls.
     */
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
        for (final Entry<String, String> entry : map.entrySet()) {
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
