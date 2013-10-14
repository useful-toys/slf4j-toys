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

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel Felix Ferber
 */
public class EventReaderTest {

   

    @Test
    public void testReadIdentifier1() throws IOException {
        String input = "abc=def";
        String expected = "abc";
        String output = new EventReader().reset(input).readPropertyName();
        assertEquals(expected, output);
    }

    @Test
    public void testReadIdentifier2() throws IOException {
        String input = "ab1=def";
        String expected = "ab1";
        String output = new EventReader().reset(input).readPropertyName();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier3() throws IOException {
        String input = " def";
        new EventReader().reset(input).readPropertyName();
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier4() throws IOException {
        String input = "1abc def";
        new EventReader().reset(input).readPropertyName();
    }

    @Test
    public void testReadString1() throws IOException {
        String input = "=abc;def";
        String expected = "abc";
        String output = new EventReader().reset(input).readString();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadString2() throws IOException {
        String input = " def";
        new EventReader().reset(input).readString();
    }
    
    public void testReadString3() throws IOException {
        String input = "=def";
        String expected = "def";
        String output = new EventReader().reset(input).readString();
        assertEquals(expected, output);
    }
}