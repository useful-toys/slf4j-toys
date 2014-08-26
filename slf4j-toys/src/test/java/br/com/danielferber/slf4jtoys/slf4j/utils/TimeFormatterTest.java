/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author X7WS
 */
public class TimeFormatterTest {

    @Test
    public void testTimeUnit() {
        assertEquals("0ns", UnitFormatter.nanoseconds(0));
        assertEquals("1ns", UnitFormatter.nanoseconds(1));
        assertEquals("999ns", UnitFormatter.nanoseconds(999));
        assertEquals("1000ns", UnitFormatter.nanoseconds(1000));
        assertEquals("1001ns", UnitFormatter.nanoseconds(1001));
        assertEquals("1,1us", UnitFormatter.nanoseconds(1100));
        assertEquals("1000,0ms", UnitFormatter.nanoseconds(1000000000L));
        assertEquals("1,1s", UnitFormatter.nanoseconds(1100000000L));
        assertEquals("60,0s", UnitFormatter.nanoseconds(60000000000L));
        assertEquals("61,0s", UnitFormatter.nanoseconds(61000000000L));
        assertEquals("1,1m", UnitFormatter.nanoseconds(66000000000L));
        assertEquals("10,0m", UnitFormatter.nanoseconds(600000000000L));
        assertEquals("60,0m", UnitFormatter.nanoseconds(3600000000000L));
        assertEquals("1,2h", UnitFormatter.nanoseconds(4400000000000L));
    }

    @Test
    public void testDoubleTimeUnit() {
        assertEquals("0ns", UnitFormatter.nanoseconds(0f));
        assertEquals("1,0ns", UnitFormatter.nanoseconds(1f));
        assertEquals("999,0ns", UnitFormatter.nanoseconds(999f));
        assertEquals("1000,0ns", UnitFormatter.nanoseconds(1000f));
        assertEquals("1001,0ns", UnitFormatter.nanoseconds(1001f));
        assertEquals("1,1us", UnitFormatter.nanoseconds(1100f));
        assertEquals("1000,0ms", UnitFormatter.nanoseconds(1000000000f));
        assertEquals("1,1s", UnitFormatter.nanoseconds(1100000000f));
        assertEquals("60,0s", UnitFormatter.nanoseconds(60000000000f));
        assertEquals("61,0s", UnitFormatter.nanoseconds(61000000000f));
        assertEquals("1,1m", UnitFormatter.nanoseconds(66000000000f));
        assertEquals("10,0m", UnitFormatter.nanoseconds(600000000000f));
        assertEquals("60,0m", UnitFormatter.nanoseconds(3600000000000f));
        assertEquals("1,2h", UnitFormatter.nanoseconds(4400000000000f));
    }
}
