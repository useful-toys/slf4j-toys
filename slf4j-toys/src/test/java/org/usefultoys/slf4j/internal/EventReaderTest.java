/*
 * Copyright 2015 Daniel Felix Ferber.
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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.usefultoys.slf4j.internal.EventReader;

/**
 *
 * @author Daniel Felix Ferber
 */
public class EventReaderTest {

    @Test
    public void testReadIdentifier1() throws IOException {
        final String input = "abc=def";
        final String expected = "abc";
        final String output = new EventReader().reset(input).readPropertyName();
        assertEquals(expected, output);
    }

    @Test
    public void testReadIdentifier2() throws IOException {
        final String input = "ab1=def";
        final String expected = "ab1";
        final String output = new EventReader().reset(input).readPropertyName();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier3() throws IOException {
        final String input = " def";
        new EventReader().reset(input).readPropertyName();
    }

    @Test(expected = IOException.class)
    public void testReadIdentifier4() throws IOException {
        final String input = "1abc def";
        new EventReader().reset(input).readPropertyName();
    }

    @Test
    public void testReadString1() throws IOException {
        final String input = "=abc;def";
        final String expected = "abc";
        final String output = new EventReader().reset(input).readString();
        assertEquals(expected, output);
    }

    @Test(expected = IOException.class)
    public void testReadString2() throws IOException {
        final String input = " def";
        new EventReader().reset(input).readString();
    }

    @Test
    public void testReadString3() throws IOException {
        final String input = "=def";
        final String expected = "def";
        final String output = new EventReader().reset(input).readString();
        assertEquals(expected, output);
    }

    @Test
    public void testReadString4() throws IOException {
        final String input = "=def|ghi|jkl";
        final EventReader r = new EventReader().reset(input);
        assertEquals("def", r.readString());
        assertEquals("ghi", r.readString());
        assertEquals("jkl", r.readString());
    }

    @Test
    public void testReadMap1() throws IOException {
        final String input = "=[]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(0, m.size());
    }

    @Test
    public void testReadMap2() throws IOException {
        final String input = "=[a:b]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(1, m.size());
        assertTrue(m.containsKey("a"));
        assertEquals("b", m.get("a"));
    }

    @Test
    public void testReadMap3() throws IOException {
        final String input = "=[a:b,c:d]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(2, m.size());
        assertTrue(m.containsKey("a"));
        assertEquals("b", m.get("a"));
        assertTrue(m.containsKey("c"));
        assertEquals("d", m.get("c"));
    }

    @Test
    public void testReadMap4() throws IOException {
        final String input = "=[a]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(1, m.size());
        assertTrue(m.containsKey("a"));
        assertEquals(null, m.get("a"));
    }

    @Test
    public void testReadMap5() throws IOException {
        final String input = "=[a,c:d]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(2, m.size());
        assertTrue(m.containsKey("a"));
        assertEquals(null, m.get("a"));
        assertTrue(m.containsKey("c"));
        assertEquals("d", m.get("c"));
    }

    @Test
    public void testReadMap6() throws IOException {
        final String input = "=[a:b,c]";
        final EventReader r = new EventReader().reset(input);
        final Map<String, String> m = r.readMap();
        assertEquals(2, m.size());
        assertTrue(m.containsKey("a"));
        assertEquals("b", m.get("a"));
        assertTrue(m.containsKey("c"));
        assertEquals(null, m.get("c"));
    }
}
