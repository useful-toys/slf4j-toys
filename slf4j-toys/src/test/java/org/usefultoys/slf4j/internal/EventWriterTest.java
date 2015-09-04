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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.usefultoys.slf4j.internal.EventWriter;

/**
 *
 * @author Daniel
 */
public class EventWriterTest {

    private StringBuilder sb;
    private EventWriter instance;

    public EventWriterTest() {
    }

    @Before
    public void createWriter() {
        sb = new StringBuilder();
        instance = new EventWriter(sb);
    }

    @Test
    public void testWritePropertyValue1() {
        final String input = "ABC";
        final String expected = "ABC";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWritePropertyValue2() {
        final String input = "A;B|C\\D";
        final String expected = "A\\;B\\|C\\\\D";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    public void testWritePropertyValue3() {
        final String input = "A(B)C";
        final String expected = "A(B\\)BC";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteMapValue1() {
        final String input = "ABC";
        final String expected = "ABC";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteMapValue2() {
        final String input = "A;B|C\\D";
        final String expected = "A\\;B\\|C\\\\D";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    public void testWriteMapValue3() {
        final String input = "A(B)C";
        final String expected = "A(B\\)BC";
        instance.writePropertyValue(input);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1() {
        final String expected = "a=0";
        instance.property("a", 0);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2() {
        final String expected = "a=0|1";
        instance.property("a", 0, 1);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3() {
        final String expected = "a=0|1|2";
        instance.property("a", 0, 1, 2);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4() {
        final String expected = "a=0|1|2|3";
        instance.property("a", 0, 1, 2, 3);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1x2() {
        final String expected = "a=0;b=4";
        instance.property("a", 0).property("b", 4);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2x2() {
        final String expected = "a=0|1;b=4|5";
        instance.property("a", 0, 1).property("b", 4, 5);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3x2() {
        final String expected = "a=0|1|2;b=4|5|6";
        instance.property("a", 0, 1, 2).property("b", 4, 5, 6);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4x2() {
        final String expected = "a=0|1|2|3;b=4|5|6|7";
        instance.property("a", 0, 1, 2, 3).property("b", 4, 5, 6, 7);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap1() {
        final String expected = "m=[]";
        final Map<String, String> m = new TreeMap<String, String>();
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap2() {
        final String expected = "m=[a:b]";
        final Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap3() {
        final String expected = "m=[a:b,c:d]";
        final Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", "d");
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap4() {
        final String expected = "m=[a]";
        final Map<String, String> m = new TreeMap<String, String>();
        m.put("a", null);
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap5() {
        final String expected = "m=[a,c:d]";
        final Map<String, String> m = new TreeMap<String, String>();
        m.put("a", null);
        m.put("c", "d");
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap6() {
        final String expected = "m=[a:b,c]";
        final Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", null);
        instance.property("m", m);
        final String output = sb.toString();
        assertEquals(expected, output);
    }

}
