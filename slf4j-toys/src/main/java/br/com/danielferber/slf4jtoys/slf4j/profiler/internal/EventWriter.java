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

import java.util.Map;
import java.util.regex.Matcher;

public final class EventWriter extends Patterns {
    private transient boolean firstProperty;
    private transient final StringBuilder builder;

    EventWriter(StringBuilder builder) {
        this.builder = builder;
    }

    void open(char id) {
        firstProperty = true;
        builder.append(id);
        builder.append(MESSAGE_OPEN);
    }
    
    void close() {
        builder.append(MESSAGE_CLOSE);
    }
    
    public EventWriter property(String name, long value) {
        property(name, Long.toString(value));
        return this;
    }

    public EventWriter property(String name, long value1, long value2) {
        property(name, Long.toString(value1), Long.toString(value2));
        return this;
    }

    public EventWriter property(String name, long value1, long value2, long value3) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3));
        return this;
    }

    public EventWriter property(String name, long value1, long value2, long value3, long value4) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3), Long.toString(value4));
        return this;
    }

    public EventWriter property(String name, double value) {
        property(name, Double.toString(value));
        return this;
    }

    public EventWriter property(String name, String value) {
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

    public EventWriter property(String name, String value1, String value2) {
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

    public EventWriter property(String name, String value1, String value2, String value3) {
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

    public EventWriter property(String name, String value1, String value2, String value3, String value4) {
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

    public EventWriter property(String name, Map<String, String> map) {
        if (!firstProperty) {
            builder.append(PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        builder.append(name);
        builder.append(PROPERTY_EQUALS);
        builder.append(MAP_OPEN);
        boolean firstEntry = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!firstEntry) {
                builder.append(MAP_SEPARATOR);
            } else {
                firstEntry = false;
            }
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key);
            builder.append(MAP_EQUAL);
            writeMapValue(value);
        }
        builder.append(MAP_CLOSE);

        return this;
    }

    void writePropertyValue(String value) {
        builder.append(encodePropertyValuePattern.matcher(value).replaceAll(encodeReplacement));
    }

    void writeMapValue(String value) {
        builder.append(encodeMapValuePattern.matcher(value).replaceAll(encodeReplacement));
    }
}
