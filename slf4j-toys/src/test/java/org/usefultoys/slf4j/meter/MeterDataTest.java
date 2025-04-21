/*
 * Copyright 2024 Daniel Felix Ferber
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
import org.usefultoys.slf4j.internal.EventDataTest;
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

        a.operation = "n";
        a.parent = "p";
        a.category = "c";
        populateMeterData((MeterData) a, "a", "b", "c", "d");

        b.operation = "n";
        b.parent = "p";
        b.category = "c";
        populateMeterData((MeterData) b, "a", "b", "c", "d");

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.category = "cc";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.category = "c";

        b.operation = "nn";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.operation = "n";

        b.parent = "pp";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.parent = "p";

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

        b.currentIteration = 44;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.currentIteration = 4;

        b.expectedIterations = 55;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.expectedIterations = 5;

        b.failPath = "ExceptionException";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.failPath = "Exception";

        b.failMessage = "bb";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.failMessage = "b";

        b.rejectPath = "B";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.rejectPath = "A";

        b.okPath = "FB";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.okPath = "FA";

        b.timeLimit = 100;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.timeLimit = 10;

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
        a.operation = "n";
        a.parent = "p";
        a.category = "c";

        EventDataTest.populateEventData(b);
        b.operation = "n";
        b.parent = "p";
        b.category = "c";

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        b.category = "cc";
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.category = "c";

        b.operation = "nn";
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.operation = "n";

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
    }


    public static void populateMeterData(MeterData a, String e, String f, String g, String h) {
        a.description = "a";
        a.createTime = 1;
        a.startTime = 2;
        a.stopTime = 3;
        a.currentIteration = 4;
        a.expectedIterations = 5;
        a.failPath = "Exception";
        a.failMessage = "b";
        a.rejectPath = "A";
        a.okPath = "FA";
        a.timeLimit = 10;
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
