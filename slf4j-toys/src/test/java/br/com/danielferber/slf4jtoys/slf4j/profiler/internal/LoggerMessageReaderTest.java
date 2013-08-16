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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel Felix Ferber
 */
public class LoggerMessageReaderTest {

    @Test
    public void testExtractPlausibleMessage1() {
        char prefix = 'M';
        String input = "bla bla bla";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage2() {
        char prefix = 'M';
        String input = "bla M( bla bla";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage3() {
        char prefix = 'M';
        String input = "bla bla ) bla";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage4() {
        char prefix = 'M';
        String input = "bla M(BLA) bla";
        String expected = "BLA";
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage5() {
        char prefix = 'M';
        String input = "bla M() bla";
        String expected = "";
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage6() {
        char prefix = 'M';
        String input = "M(BLA BLA BLA)";
        String expected = "BLA BLA BLA";
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage7() {
        char prefix = 'M';
        String input = "bla M(BLA BLA BLA)";
        String expected = "BLA BLA BLA";
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage8() {
        char prefix = 'M';
        String input = "bla ) bla M( bla";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
    
    @Test
    public void testExtractPlausibleMessage9() {
        char prefix = 'M';
        String input = "bla bla bla (";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
    
    @Test
    public void testExtractPlausibleMessage10() {
        char prefix = 'M';
        String input = "( bla bla bla bla";
        String expected = null;
        String output = LoggerMessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }
}