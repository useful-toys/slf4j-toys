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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.usefultoys.slf4j.internal.SyntaxDefinition.*;

/**
 * Regular expression patterns used to deserialize events as encoded strings.
 * @author Daniel
 */
class PatternDefinition {

    static final Pattern encodePropertyValuePattern = quotedCharsPattern(PROPERTY_DIV, PROPERTY_SEPARATOR, MESSAGE_CLOSE, QUOTE, MAP_CLOSE, MAP_SEPARATOR);
    static final Pattern encodeMapValuePattern = quotedCharsPattern(MAP_CLOSE, MAP_SEPARATOR, MESSAGE_CLOSE, QUOTE);
    static final String encodeReplacement = "\\" + QUOTE + "$1";

    static final Pattern decodeValuePattern = Pattern.compile('\\' + QUOTE + "(.)");
    static final String decodeReplacement = "$1";

    // see http://ad.hominem.org/log/2005/05/quoted_strings.php
    static final Pattern messagePattern = Pattern.compile("(.)" + "\\" + MESSAGE_OPEN + "([^\\" + MESSAGE_CLOSE + "\\" + QUOTE + "]*(?:\\" + QUOTE + ".[^\"\\" + QUOTE + "]*)*)\\" + MESSAGE_CLOSE);

    /**
     * @param chars list of chars
     * @return a pattern that matches any of the chars
     */
    private static Pattern quotedCharsPattern(final char... chars) {
        final StringBuilder sb = new StringBuilder("([");
        for (final char c : chars) {
            sb.append('\\');
            sb.append(c);
        }
        sb.append("])");
        return Pattern.compile(sb.toString());
    }

    /**
     * Tests if the candidate string contains something that looks like an encoded substring.
     * A prefix is used to signal a potential valid encoded string.
     *  
     * @param prefix Prefix that identifies strings containing an encoded event
     * @param candidateString String that may contain an encoded event  
     * @return string that encodes event, null otherwise 
     */
    static String extractPlausibleMessage(final char prefix, final String candidateString) {
        final Matcher m = messagePattern.matcher(candidateString);
        if (m.find()) {
            if (m.group(1).charAt(0) == prefix) {
                return m.group(2);
            }
        }
        return null;
    }
}
