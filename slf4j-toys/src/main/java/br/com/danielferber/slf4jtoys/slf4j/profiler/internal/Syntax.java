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

import java.awt.geom.QuadCurve2D;
import java.util.regex.Pattern;

/**
 * Defines symbols used to serialize data into log messages.
 *
 * @author Daniel Felix Ferber
 */
interface Syntax {
    /* Message delimiter symbols. */
    static final char MESSAGE_OPEN = '(';
    static final char MESSAGE_CLOSE = ')';
    
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
