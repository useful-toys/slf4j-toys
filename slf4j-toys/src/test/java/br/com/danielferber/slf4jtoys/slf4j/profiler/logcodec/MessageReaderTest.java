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
package br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MessageReaderTest {

    @Test
    public void testExtractPlausibleMessage1() {
        char prefix = 'M';
        String input = "bla bla bla";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage2() {
        char prefix = 'M';
        String input = "bla M( bla bla";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage3() {
        char prefix = 'M';
        String input = "bla bla ) bla";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage4() {
        char prefix = 'M';
        String input = "bla M(BLA) bla";
        String expected = "BLA";
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage5() {
        char prefix = 'M';
        String input = "bla M() bla";
        String expected = "";
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage6() {
        char prefix = 'M';
        String input = "M(BLA BLA BLA)";
        String expected = "BLA BLA BLA";
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage7() {
        char prefix = 'M';
        String input = "bla M(BLA BLA BLA)";
        String expected = "BLA BLA BLA";
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage8() {
        char prefix = 'M';
        String input = "bla ) bla M( bla";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage9() {
        char prefix = 'M';
        String input = "bla bla bla (";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testExtractPlausibleMessage10() {
        char prefix = 'M';
        String input = "( bla bla bla bla";
        String expected = null;
        String output = MessageReader.extractPlausibleMessage(prefix, input);
        assertEquals(expected, output);
    }

    @Test
    public void testReadIdentifier1() throws IOException {
        String input = "abc=def";
        String expected = "abc";
        String output = new MessageReader().reset(input).readIdentifier();
        assertEquals(expected, output);
    }

    @Test
    public void testReadIdentifier2() throws IOException {
        String input = "ab1=def";
        String expected = "ab1";
        String output = new MessageReader().reset(input).readIdentifier();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier3() throws IOException {
        String input = " def";
        String expected = null;
        String output = new MessageReader().reset(input).readIdentifier();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier4() throws IOException {
        String input = "1abc def";
        String expected = "abc";
        String output = new MessageReader().reset(input).readIdentifier();
        assertEquals(expected, output);
    }

    @Test
    public void testReadString1() throws IOException {
        String input = "=abc def";
        String expected = "abc";
        String output = new MessageReader().reset(input).readString();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadString2() throws IOException {
        String input = " def";
        String expected = null;
        String output = new MessageReader().reset(input).readString();
        assertEquals(expected, output);
    }
    
    public void testReadString3() throws IOException {
        String input = "=def";
        String expected = "def";
        String output = new MessageReader().reset(input).readString();
        assertEquals(expected, output);
    }
}