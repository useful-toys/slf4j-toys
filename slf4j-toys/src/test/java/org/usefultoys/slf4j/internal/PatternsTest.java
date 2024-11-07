/*
 * Copyright 2024 Daniel Felix Ferber
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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Daniel
 */
public class PatternsTest {

    public PatternsTest() {
    }

    @Test
    public void printPatternsTest() {
        System.out.println(PatternDefinition.encodeMapValuePattern.pattern());
        System.out.println(PatternDefinition.encodePropertyValuePattern.pattern());
        System.out.println(PatternDefinition.encodeReplacement);
        System.out.println(PatternDefinition.decodeValuePattern.pattern());
        System.out.println(PatternDefinition.decodeReplacement);
        System.out.println(PatternDefinition.messagePattern);
    }

    @Test
    public void testExtractPlausibleMessageNotFound1() {
        final char prefix = 'M';
        final String input = "bla bla bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound2() {
        final char prefix = 'M';
        final String input = "bla M{bla bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound3() {
        final char prefix = 'M';
        final String input = "bla bla} bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound4() {
        final char prefix = 'M';
        final String input = "bla } bla M{ bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound1() {
        final char prefix = 'M';
        final String input = "bla M{BLA} bla";
        final String expected = "BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound2() {
        final char prefix = 'M';
        final String input = "bla M{} bla";
        final String expected = "";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound3() {
        final char prefix = 'M';
        final String input = "M{BLA BLA BLA}";
        final String expected = "BLA BLA BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound4() {
        final char prefix = 'M';
        final String input = "bla M{BLA BLA BLA}";
        final String expected = "BLA BLA BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound5() {
        final char prefix = 'M';
        final String input = "M{BLA BLA BLA} BLA";
        final String expected = "BLA BLA BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted1() {
        final char prefix = 'M';
        final String input = "bla M{BLA\\} bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted2() {
        final char prefix = 'M';
        final String input = "bla M{\\} bla";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted3() {
        final char prefix = 'M';
        final String input = "M{BLA BLA BLA\\}";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted4() {
        final char prefix = 'M';
        final String input = "bla M{BLA BLA BLA\\}";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoate5() {
        final char prefix = 'M';
        final String input = "M{BLA BLA BLA\\} BLA";
        final String expected = null;
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound1() {
        final char prefix = 'M';
        final String input = "bla M{BLA\\}} bla";
        final String expected = "BLA\\}";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound2() {
        final char prefix = 'M';
        final String input = "bla M{\\}} bla";
        final String expected = "\\}";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound3() {
        final char prefix = 'M';
        final String input = "M{BLA {BLA\\} BLA}";
        final String expected = "BLA {BLA\\} BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound4() {
        final char prefix = 'M';
        final String input = "BLA M{BLA {BLA\\} BLA}";
        final String expected = "BLA {BLA\\} BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound5() {
        final char prefix = 'M';
        final String input = "M{BLA {BLA\\} BLA} BLA";
        final String expected = "BLA {BLA\\} BLA";
        final String output = PatternDefinition.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
}
