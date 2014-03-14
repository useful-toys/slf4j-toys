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

import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

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
        String input = "ABC";
        String expected = "ABC";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWritePropertyValue2() {
        String input = "A;B|C\\D";
        String expected = "A\\;B\\|C\\\\D";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    public void testWritePropertyValue3() {
        String input = "A(B)C";
        String expected = "A(B\\)BC";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteMapValue1() {
        String input = "ABC";
        String expected = "ABC";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteMapValue2() {
        String input = "A;B|C\\D";
        String expected = "A\\;B\\|C\\\\D";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    public void testWriteMapValue3() {
        String input = "A(B)C";
        String expected = "A(B\\)BC";
        instance.writePropertyValue(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1() {
        String expected = "a=0";
        instance.property("a", 0);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2() {
        String expected = "a=0|1";
        instance.property("a", 0, 1);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3() {
        String expected = "a=0|1|2";
        instance.property("a", 0, 1, 2);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4() {
        String expected = "a=0|1|2|3";
        instance.property("a", 0, 1, 2, 3);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1x2() {
        String expected = "a=0;b=4";
        instance.property("a", 0).property("b", 4);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2x2() {
        String expected = "a=0|1;b=4|5";
        instance.property("a", 0, 1).property("b", 4, 5);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3x2() {
        String expected = "a=0|1|2;b=4|5|6";
        instance.property("a", 0, 1, 2).property("b", 4, 5, 6);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4x2() {
        String expected = "a=0|1|2|3;b=4|5|6|7";
        instance.property("a", 0, 1, 2, 3).property("b", 4, 5, 6, 7);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap1() {
        String expected = "m=[]";
        Map<String, String> m = new TreeMap<String, String>();
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap2() {
        String expected = "m=[a:b]";
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap3() {
        String expected = "m=[a:b,c:d]";
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", "d");
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap4() {
        String expected = "m=[a]";
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", null);
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap5() {
        String expected = "m=[a,c:d]";
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", null);
        m.put("c", "d");
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap6() {
        String expected = "m=[a:b,c]";
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", null);
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

}
