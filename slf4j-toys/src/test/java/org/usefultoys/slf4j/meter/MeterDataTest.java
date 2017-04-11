/*
 * Copyright 2017 Daniel Felix Ferber
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

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

        b.eventName = "n";
        b.eventParent = "p";
        b.eventCategory = "c";
        b.description = "a";
        b.createTime = 1;
        b.startTime = 2;
        b.stopTime = 3;
        b.iteration = 4;
        b.failClass = "Exception";
        b.failMessage = "b";
        b.rejectId = "B";
        b.pathId = "FB";
        b.timeLimitNanoseconds = 10;
        b.context = new HashMap<String, String>();
        b.context.put("a", "b");
        b.context.put("c", "d");

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.resetBridge();

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
        a.description = "a";
        a.createTime = 1;
        a.startTime = 2;
        a.stopTime = 3;
        a.iteration = 4;
        a.failClass = "Exception";
        a.failMessage = "b";
        a.rejectId = "A";
        a.pathId = "FA";
        a.timeLimitNanoseconds = 10;
        a.context = new HashMap<String, String>();
        a.context.put("a", "b");
        a.context.put("c", "d");

        b.eventName = "n";
        b.eventParent = "p";
        b.eventCategory = "c";
        b.description = "a";
        b.createTime = 1;
        b.startTime = 2;
        b.stopTime = 3;
        b.iteration = 4;
        b.failClass = "Exception";
        b.failMessage = "b";
        b.rejectId = "A";
        b.pathId = "FA";
        b.timeLimitNanoseconds = 10;
        b.context = new HashMap<String, String>();
        b.context.put("a", "b");
        b.context.put("c", "d");

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

        a.eventName = "n";
        a.eventParent = "p";
        a.eventCategory = "c";
        a.setEventPosition(1);
        a.setSessionUuid("uuid");

        b.eventName = "n";
        b.eventParent = "p";
        b.eventCategory = "c";
        b.setEventPosition(1);
        b.setSessionUuid("uuid");

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        b.eventCategory = "cc";
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.eventCategory = "c";

        b.setEventPosition(11);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.setEventPosition(1);

        b.setSessionUuid("uuiduuid");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());
        b.setSessionUuid("uuid");

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
        final MeterDataMock a = createMeterData();

        a.eventName = "n";
        a.eventParent = "p";
        a.eventCategory = "c";
        a.setEventPosition(1111);
        a.setSessionUuid("bbbb");
        a.setTime(2222);

        a.setHeap_commited(11);
//        a.setHeap_init(22);
        a.setHeap_max(33);
        a.setHeap_used(44);
        a.setNonHeap_commited(55);
//        a.setNonHeap_init(66);
        a.setNonHeap_max(77);
        a.setNonHeap_used(88);
        a.setObjectPendingFinalizationCount(99);
        a.setClassLoading_loaded(1010);
        a.setClassLoading_total(1111);
        a.setClassLoading_unloaded(1212);
        a.setCompilationTime(1313);
        a.setGarbageCollector_count(1414);
        a.setGarbageCollector_time(1515);
        a.setRuntime_usedMemory(1616);
        a.setRuntime_maxMemory(1717);
        a.setRuntime_totalMemory(1818);
        a.setSystemLoad(1919.0);

        a.description = "a";
        a.createTime = 1;
        a.startTime = 2;
        a.stopTime = 3;
        a.iteration = 4;
        a.failClass = "Exception";
        a.failMessage = "b";
        a.rejectId = "A";
        a.pathId = "FA";
        a.timeLimitNanoseconds = 10;
        a.context = new HashMap<String, String>();
        a.context.put("e", "f");
        a.context.put("g", "h");

        final String s = a.write(new StringBuilder(), 'M').toString();
        System.out.println(s);

        final MeterData b = createMeterData();
        assertTrue(b.read(s));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    private MeterDataMock createMeterData() {
        return new MeterDataMock() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuilder readableString(final StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
