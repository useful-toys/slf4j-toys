/*
 * Copyright 2012 Daniel Felix Ferber
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

public class LoggerMessageWriter {

    /* Internal parser state. */
    private boolean firstProperty = true;

    /* Syntax definition. */
    public final Syntax syntax;
    public final StringBuilder buffer;

    public LoggerMessageWriter(final Syntax syntax, StringBuilder buffer) {
        super();
        this.buffer = buffer;
        this.syntax = syntax;
        this.syntax.reset();
    }

    public void writeQuotedString(final StringBuilder sb, final String string) {
        sb.append(syntax.STRING_DELIM);
        sb.append(string.replace(syntax.STRING_DELIM_STR, syntax.STRING_DELIM_QUOTED_STR));
        sb.append(syntax.STRING_DELIM);
    }

    public void openData() {
        buffer.append(syntax.DATA_OPEN);
    }

    public void property(String name, long value) {
        property(name, Long.toString(value));
    }

    public void property(String name, long value1, long value2) {
        property(name, Long.toString(value1), Long.toString(value2));
    }

    public void property(String name, long value1, long value2, long value3) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3));
    }

    public void property(String name, long value1, long value2, long value3, long value4) {
        property(name, Long.toString(value1), Long.toString(value2), Long.toString(value3), Long.toString(value4));
    }

    public void property(String name, double value) {
        property(name, Double.toString(value));
    }

    public void property(String name, String value) {
        if (!firstProperty) {
            buffer.append(syntax.PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        buffer.append(syntax.PROPERTY_SPACE);
        buffer.append(name);
        buffer.append(syntax.PROPERTY_EQUALS);
        buffer.append(value);
    }

    public void property(String name, String value1, String value2) {
        if (!firstProperty) {
            buffer.append(syntax.PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        buffer.append(name);
        buffer.append(syntax.PROPERTY_SPACE);
        buffer.append(syntax.PROPERTY_EQUALS);
        buffer.append(value1);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value2);
    }

    public void property(String name, String value1, String value2, String value3) {
        if (!firstProperty) {
            buffer.append(syntax.PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        buffer.append(name);
        buffer.append(syntax.PROPERTY_SPACE);
        buffer.append(syntax.PROPERTY_EQUALS);
        buffer.append(value1);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value2);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value3);
    }

    public void property(String name, String value1, String value2, String value3, String value4) {
        if (!firstProperty) {
            buffer.append(syntax.PROPERTY_SEPARATOR);
        } else {
            firstProperty = false;
        }
        buffer.append(name);
        buffer.append(syntax.PROPERTY_SPACE);
        buffer.append(syntax.PROPERTY_EQUALS);
        buffer.append(value1);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value2);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value3);
        buffer.append(syntax.PROPERTY_DIV);
        buffer.append(value4);
    }
}
