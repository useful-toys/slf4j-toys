/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import static br.com.danielferber.slf4jtoys.slf4j.profiler.internal.Syntax.PROPERTY_DIV;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Daniel
 */
public class Patterns implements Syntax {

    static final Pattern encodePropertyValuePattern = quotedCharsPattern(PROPERTY_DIV, PROPERTY_SEPARATOR, MESSAGE_CLOSE, QUOTE);
    static final Pattern encodeMapValuePattern = quotedCharsPattern(MAP_CLOSE, MAP_SEPARATOR, MESSAGE_CLOSE, QUOTE);
    static final String encodeReplacement = "\\" + QUOTE + "$1";

    static final Pattern decodeValuePattern = Pattern.compile('\\' + QUOTE + "(.)");
    static final String decodeReplacement = "$1";

    // see http://ad.hominem.org/log/2005/05/quoted_strings.php
    static final Pattern messagePattern = Pattern.compile("(.)" + "\\" + MESSAGE_OPEN + "([^\\" + MESSAGE_CLOSE + "\\" + QUOTE + "]*(?:\\" + QUOTE + ".[^\"\\" + QUOTE + "]*)*)\\" + MESSAGE_CLOSE);

    private static Pattern quotedCharsPattern(char... chars) {
        StringBuilder sb = new StringBuilder("([");
        for (char c : chars) {
            sb.append('\\');
            sb.append(c);
        }
        sb.append("])");
        return Pattern.compile(sb.toString());
    }

    static String extractPlausibleMessage(char prefix, String s) {
        Matcher m = messagePattern.matcher(s);
        if (m.find()) {
            if (m.group(1).charAt(0) == prefix) {
                return m.group(2);
            }
        }
        return null;
    }
}
