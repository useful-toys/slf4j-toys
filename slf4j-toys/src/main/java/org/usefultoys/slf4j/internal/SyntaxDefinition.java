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

/**
 * Symbols used to serialize/deserialize events as encoded strings.
 *
 * @author Daniel Felix Ferber
 */
final class SyntaxDefinition {

    protected SyntaxDefinition() {
        // prevent instances
    }

    /* Message delimiter symbols. */
    static final char MESSAGE_OPEN = '{';
    static final char MESSAGE_CLOSE = '}';

    /* Map delimiter symbols. */
    static final char MAP_CLOSE = ']';
    static final char MAP_OPEN = '[';
    static final char MAP_SEPARATOR = ',';
    static final char MAP_EQUAL = ':';

    /* Property delimiter symbols. */
    static final char PROPERTY_DIV = '|';
    static final char PROPERTY_EQUALS = '=';
    static final char PROPERTY_SEPARATOR = ';';

    /* Property value quoting to prevent collision with other symbols. */
    static final char QUOTE = '\\';

}
