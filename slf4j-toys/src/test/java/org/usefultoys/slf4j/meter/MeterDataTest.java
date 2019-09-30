/*
 * Copyright 2019 Daniel Felix Ferber
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
package org.usefultoys.slf4j.meter;

import org.junit.Test;
import org.usefultoys.slf4j.internal.EventData;
import org.usefultoys.slf4j.internal.EventDataTest;
import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.internal.SystemDataTest;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel
 */
public class MeterDataTest {

    public MeterDataTest() {
    }

    @Test
    public void testResetImpl() {
        final MeterData a = createMeterData();
        final MeterData b = createMeterData();

        populateMeterData(b, "a", "b", "c", "d");

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        SystemDataTest.resetAll(b);

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void testIsCompletelyEqualsImpl() {
        final MeterData a = createMeterData();
        final MeterData b = createMeterData();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        a.eventName = "n";
        a.eventParent = "p";
        a.eventCategory = "c";
        populateMeterData((MeterData) a, "a", "b", "c", "d");

        b.eventName = "n";
        b.eventParent = "p";
        b.eventCategory = "c";
        populateMeterData((MeterData) b, "a", "b", "c", "d");

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.eventCategory = "cc";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.eventCategory = "c";

        b.eventName = "nn";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.eventName = "n";

        b.eventParent = "pp";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.eventParent = "p";

        b.description = "aa";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.description = "a";

        b.createTime = 11;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.createTime = 1;

        b.startTime = 22;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.startTime = 2;

        b.stopTime = 33;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.stopTime = 3;

        b.iteration = 44;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.iteration = 4;

        b.expectedIterations = 55;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.expectedIterations = 5;

        b.failClass = "ExceptionException";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.failClass = "Exception";

        b.failMessage = "bb";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.failMessage = "b";

        b.rejectId = "B";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.rejectId = "A";

        b.pathId = "FB";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.pathId = "FA";

        b.timeLimitNanoseconds = 100;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.timeLimitNanoseconds = 10;

        b.context.put("e", "f");
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.context.remove("e");

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

    }

    @Test
    public void equalsHashTest() {
        final MeterData a = createMeterData();
        final MeterData b = createMeterData();

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        EventDataTest.populateEventData(a);
        a.eventName = "n";
        a.eventParent = "p";
        a.eventCategory = "c";

        EventDataTest.populateEventData(b);
        b.eventName = "n";
        b.eventParent = "p";
        b.eventCategory = "c";

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        b.eventCategory = "cc";
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.eventCategory = "c";

        b.eventName = "nn";
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.eventName = "n";

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
    }

    @Test
    public void writeReadTest1() {
        final MeterData a = createMeterData();

        final String s = a.write(new StringBuilder(), 'M').toString();
        System.out.println(s);

        final MeterData b = createMeterData();
        assertTrue(b.read(s));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest2() {
        final MeterData a = createMeterData();

        SystemDataTest.populateEventData(a);
        SystemDataTest.populateTestSystemData(a);

        populateMeterData(a, "a", "b", "c", "d");

        final String s = a.write(new StringBuilder(), 'M').toString();
        System.out.println(s);

        final MeterData b = createMeterData();
        assertTrue(b.read(s));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    public static void populateMeterData(MeterData a, String e, String f, String g, String h) {
        a.description = "a";
        a.createTime = 1;
        a.startTime = 2;
        a.stopTime = 3;
        a.iteration = 4;
        a.expectedIterations = 5;
        a.failClass = "Exception";
        a.failMessage = "b";
        a.rejectId = "A";
        a.pathId = "FA";
        a.timeLimitNanoseconds = 10;
        a.context = new HashMap<String, String>();
        a.context.put(e, f);
        a.context.put(g, h);
    }

    private MeterData createMeterData() {
        return new MeterData() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuilder readableString(final StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
