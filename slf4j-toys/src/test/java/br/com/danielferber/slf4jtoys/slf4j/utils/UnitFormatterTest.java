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
package br.com.danielferber.slf4jtoys.slf4j.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Daniel
 */
public class UnitFormatterTest {

    public UnitFormatterTest() {
    }
    private static final int[] FACTORS = new int[]{1000, 1000, 1000};
    private static final String[] UNITS = new String[]{"A", "B", "C"};

    @Test
    public void testLongUnit() {
        assertEquals("0A", UnitFormatter.longUnit(0, UNITS, FACTORS));
        assertEquals("1A", UnitFormatter.longUnit(1, UNITS, FACTORS));
        assertEquals("9A", UnitFormatter.longUnit(9, UNITS, FACTORS));
        assertEquals("10A", UnitFormatter.longUnit(10, UNITS, FACTORS));
        assertEquals("11A", UnitFormatter.longUnit(11, UNITS, FACTORS));
        assertEquals("99A", UnitFormatter.longUnit(99, UNITS, FACTORS));
        assertEquals("100A", UnitFormatter.longUnit(100, UNITS, FACTORS));
        assertEquals("101A", UnitFormatter.longUnit(101, UNITS, FACTORS));
        assertEquals("999A", UnitFormatter.longUnit(999, UNITS, FACTORS));
        assertEquals("1000A", UnitFormatter.longUnit(1000, UNITS, FACTORS));
        assertEquals("1001A", UnitFormatter.longUnit(1001, UNITS, FACTORS));
        assertEquals("1099A", UnitFormatter.longUnit(1099, UNITS, FACTORS));
        assertEquals("1,1B", UnitFormatter.longUnit(1100, UNITS, FACTORS));
        assertEquals("1,1B", UnitFormatter.longUnit(1101, UNITS, FACTORS));
        assertEquals("1,1B", UnitFormatter.longUnit(1149, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1150, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1151, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1199, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1200, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1201, UNITS, FACTORS));
        assertEquals("1,2B", UnitFormatter.longUnit(1249, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1250, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1251, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1299, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1300, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1301, UNITS, FACTORS));
        assertEquals("1,3B", UnitFormatter.longUnit(1349, UNITS, FACTORS));
        assertEquals("4,9B", UnitFormatter.longUnit(4900, UNITS, FACTORS));
        assertEquals("4,9B", UnitFormatter.longUnit(4949, UNITS, FACTORS));
        assertEquals("5,0B", UnitFormatter.longUnit(4990, UNITS, FACTORS));
        assertEquals("5,0B", UnitFormatter.longUnit(5000, UNITS, FACTORS));
        assertEquals("5,0B", UnitFormatter.longUnit(5010, UNITS, FACTORS));
        assertEquals("5,1B", UnitFormatter.longUnit(5050, UNITS, FACTORS));
        assertEquals("999,9B", UnitFormatter.longUnit(999900, UNITS, FACTORS));
        assertEquals("1000,0B", UnitFormatter.longUnit(1000000, UNITS, FACTORS));
        assertEquals("1,1C", UnitFormatter.longUnit(1100000, UNITS, FACTORS));
    }

}
