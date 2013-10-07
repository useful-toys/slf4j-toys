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

/**
 * Defines symbols used to serialize data into log messages.
 * @author Daniel Felix Ferber
 */
class Syntax {
    /* Symbols used in parseable messages. */
    static final char STRING_QUOTE = '\\';
    static final char STRING_DELIM = '"';
    static final char MAP_CLOSE = ']';
    static final char MAP_OPEN = '[';
    static final char MAP_SEPARATOR = ',';
    static final char MAP_EQUAL = ':';
    static final char PROPERTY_DIV = '|';
    static final char PROPERTY_EQUALS = '=';
    static final char PROPERTY_SEPARATOR = ';';
    static final char DATA_OPEN = '(';
    static final char DATA_CLOSE = ')';
    /* Some symbols, as string, for convenience. */
    static final String STRING_DELIM_QUOTED_STR;
    static final String STRING_DELIM_STR;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(STRING_DELIM);
        STRING_DELIM_STR = sb.toString();
        sb = new StringBuilder();
        sb.append(STRING_QUOTE);
        sb.append(STRING_DELIM);
        STRING_DELIM_QUOTED_STR = sb.toString();
    }
}
