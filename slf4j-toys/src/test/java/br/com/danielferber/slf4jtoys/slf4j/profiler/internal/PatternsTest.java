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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class PatternsTest {

    public PatternsTest() {
    }

    @Test
    public void printPatternsTest() {
        System.out.println(Patterns.encodeMapValuePattern.pattern());
        System.out.println(Patterns.encodePropertyValuePattern.pattern());
        System.out.println(Patterns.encodeReplacement);
        System.out.println(Patterns.decodeValuePattern.pattern());
        System.out.println(Patterns.decodeReplacement);
        System.out.println(Patterns.messagePattern);
    }

    @Test
    public void testExtractPlausibleMessageNotFound1() {
        char prefix = 'M';
        String input = "bla bla bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound2() {
        char prefix = 'M';
        String input = "bla M{bla bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound3() {
        char prefix = 'M';
        String input = "bla bla} bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageNotFound4() {
        char prefix = 'M';
        String input = "bla } bla M{ bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound1() {
        char prefix = 'M';
        String input = "bla M{BLA} bla";
        String expected = "BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound2() {
        char prefix = 'M';
        String input = "bla M{} bla";
        String expected = "";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound3() {
        char prefix = 'M';
        String input = "M{BLA BLA BLA}";
        String expected = "BLA BLA BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound4() {
        char prefix = 'M';
        String input = "bla M{BLA BLA BLA}";
        String expected = "BLA BLA BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageFound5() {
        char prefix = 'M';
        String input = "M{BLA BLA BLA} BLA";
        String expected = "BLA BLA BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted1() {
        char prefix = 'M';
        String input = "bla M{BLA\\} bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted2() {
        char prefix = 'M';
        String input = "bla M{\\} bla";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted3() {
        char prefix = 'M';
        String input = "M{BLA BLA BLA\\}";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoted4() {
        char prefix = 'M';
        String input = "bla M{BLA BLA BLA\\}";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuoate5() {
        char prefix = 'M';
        String input = "M{BLA BLA BLA\\} BLA";
        String expected = null;
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
    
    @Test
    public void testExtractPlausibleMessageQuotedFound1() {
        char prefix = 'M';
        String input = "bla M{BLA\\}} bla";
        String expected = "BLA\\}";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound2() {
        char prefix = 'M';
        String input = "bla M{\\}} bla";
        String expected = "\\}";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound3() {
        char prefix = 'M';
        String input = "M{BLA {BLA\\} BLA}";
        String expected = "BLA {BLA\\} BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound4() {
        char prefix = 'M';
        String input = "BLA M{BLA {BLA\\} BLA}";
        String expected = "BLA {BLA\\} BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessageQuotedFound5() {
        char prefix = 'M';
        String input = "M{BLA {BLA\\} BLA} BLA";
        String expected = "BLA {BLA\\} BLA";
        String output = Patterns.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
}
