/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class LoggerMessageWriterTest {

    public LoggerMessageWriterTest() {
    }

    @Test
    public void testWriteQuotedString1() {
        System.out.println("writeQuotedString");
        String input = "ABC";
        String expected = "\"ABC\"";
        StringBuilder sb = new StringBuilder();
        LoggerMessageWriter instance = new LoggerMessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString2() {
        System.out.println("writeQuotedString");
        String input = "A\"BC";
        String expected = "\"A\\\"BC\"";
        StringBuilder sb = new StringBuilder();
        LoggerMessageWriter instance = new LoggerMessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString3() {
        System.out.println("writeQuotedString");
        String input = "\"ABC";
        String expected = "\"\\\"ABC\"";
        StringBuilder sb = new StringBuilder();
        LoggerMessageWriter instance = new LoggerMessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

    @Test
    public void testWriteQuotedString4() {
        System.out.println("writeQuotedString");
        String input = "ABC\"";
        String expected = "\"ABC\\\"\"";
        StringBuilder sb = new StringBuilder();
        LoggerMessageWriter instance = new LoggerMessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }
    
    @Test
    public void testWriteQuotedString5() {
        System.out.println("writeQuotedString");
        String input = "A\"\"BC";
        String expected = "\"A\\\"\\\"BC\"";
        StringBuilder sb = new StringBuilder();
        LoggerMessageWriter instance = new LoggerMessageWriter().reset(sb).writeQuotedString(input);
        String output = sb.toString();
        assertEquals(expected, output);
    }

}