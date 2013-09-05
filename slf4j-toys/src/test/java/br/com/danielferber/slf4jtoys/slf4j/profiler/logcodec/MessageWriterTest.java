/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec;

import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class MessageWriterTest {

    public MessageWriterTest() {
    }

    @Test
    public void testWriteQuotedString1() {
        String input = "ABC";
        String expected = "\"ABC\"";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString2() {
        String input = "A\"BC";
        String expected = "\"A\\\"BC\"";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString3() {
        String input = "\"ABC";
        String expected = "\"\\\"ABC\"";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString4() {
        String input = "ABC\"";
        String expected = "\"ABC\\\"\"";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString5() {
        String input = "A\"\"BC";
        String expected = "\"A\\\"\\\"BC\"";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter1() {
        String expected = "a=0";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2() {
        String expected = "a=0|1";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3() {
        String expected = "a=0|1|2";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1, 2);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4() {
        String expected = "a=0|1|2|3";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1, 2, 3);
        String output = sb.toString();
        assertEquals(expected, output);
    }
    
    @Test
    public void testWriter1x2() {
        String expected = "a=0;b=4";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0).property("b", 4);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter2x2() {
        String expected = "a=0|1;b=4|5";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1).property("b", 4, 5);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter3x2() {
        String expected = "a=0|1|2;b=4|5|6";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1, 2).property("b", 4, 5, 6);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriter4x2() {
        String expected = "a=0|1|2|3;b=4|5|6|7";
        StringBuilder sb = new StringBuilder();
        MessageWriter instance = new MessageWriter().reset(sb).property("a", 0, 1, 2, 3).property("b", 4, 5, 6, 7);
        String output = sb.toString();
        assertEquals(expected, output);
    }
    
    @Test
    public void testWriterMap1() {
        String expected = "[]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        MessageWriter instance = new MessageWriter().reset(sb).property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }
    
        @Test
    public void testWriterMap2() {
        String expected = "m=[a:\"b\"]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        MessageWriter instance = new MessageWriter().reset(sb).property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }
     @Test
    public void testWriterMap3() {
        String expected = "m=[a:\"b\",c:\"d\"]";
        StringBuilder sb = new StringBuilder();
        Map<String, String> m = new TreeMap<String, String>();
        m.put("a", "b");
        m.put("c", "d");
        MessageWriter instance = new MessageWriter().reset(sb).property("m", m);
        String output = sb.toString();
        assertEquals(expected, output);
    }
}