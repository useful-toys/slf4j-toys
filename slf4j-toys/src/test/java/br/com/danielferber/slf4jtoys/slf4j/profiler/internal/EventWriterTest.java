/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2() {
        String expected = "a=0|1";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3() {
        String expected = "a=0|1|2";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1, 2);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4() {
        String expected = "a=0|1|2|3";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1, 2, 3);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1x2() {
        String expected = "a=0;b=4";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0).property("b", 4);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2x2() {
        String expected = "a=0|1;b=4|5";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1).property("b", 4, 5);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3x2() {
        String expected = "a=0|1|2;b=4|5|6";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1, 2).property("b", 4, 5, 6);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4x2() {
        String expected = "a=0|1|2|3;b=4|5|6|7";
        StringBuilder sb = new StringBuilder();
        instance.property("a", 0, 1, 2, 3).property("b", 4, 5, 6, 7);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap1() {
        String expected = "m=[]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap2() {
        String expected = "m=[a:b]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriterMap3() {
        String expected = "m=[a:b,c:d]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", "d");
        instance.property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }
}
