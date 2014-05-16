/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class UnitFormatterTest {

    public UnitFormatterTest() {
    }

    @Test
    public void testBytes() {
        assertEquals("0B", UnitFormatter.bytes(0));
        assertEquals("10B", UnitFormatter.bytes(10));
        assertEquals("100B", UnitFormatter.bytes(100));
        assertEquals("1000B", UnitFormatter.bytes(1000));
        assertEquals("1099B", UnitFormatter.bytes(1099));
        assertEquals("1100B", UnitFormatter.bytes(1100));
        assertEquals("1,1kB", UnitFormatter.bytes(1101));
        assertEquals("10,0kB", UnitFormatter.bytes(10000));
        assertEquals("10,1kB", UnitFormatter.bytes(10100));
        assertEquals("100,0kB", UnitFormatter.bytes(100000));
        assertEquals("1000,0kB", UnitFormatter.bytes(1000000));
        assertEquals("10,0MB", UnitFormatter.bytes(10000000));
        assertEquals("10,1MB", UnitFormatter.bytes(10100000));
        assertEquals("100,0MB", UnitFormatter.bytes(100000000));
        assertEquals("1000,0MB", UnitFormatter.bytes(1000000000));
    }

}
