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
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import java.util.HashMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Daniel
 */
public class MeterDataTest {

    public MeterDataTest() {
    }

    @Test
    public void testResetImpl() {
        final MeterData a = createMeterData();
        final MeterData b = createMeterData();

        b.description = "a";
        b.createTime = 1;
        b.startTime = 2;
        b.stopTime = 3;
        b.currentIteration = 4;
        b.exceptionClass = "Exception";
        b.exceptionMessage = "b";
        b.success = true;
        b.timeLimitNanoseconds = 10;
        b.threadStartId = 5;
        b.threadStopId = 6;
        b.threadStartName = "c";
        b.threadStopName = "d";
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

        a.description = "a";
        a.createTime = 1;
        a.startTime = 2;
        a.stopTime = 3;
        a.currentIteration = 4;
        a.exceptionClass = "Exception";
        a.exceptionMessage = "b";
        a.success = true;
        a.timeLimitNanoseconds = 10;
        a.threadStartId = 5;
        a.threadStopId = 6;
        a.threadStartName = "c";
        a.threadStopName = "d";
        a.context = new HashMap<String, String>();
        a.context.put("a", "b");
        a.context.put("c", "d");

        b.description = "a";
        b.createTime = 1;
        b.startTime = 2;
        b.stopTime = 3;
        b.currentIteration = 4;
        b.exceptionClass = "Exception";
        b.exceptionMessage = "b";
        b.success = true;
        b.timeLimitNanoseconds = 10;
        b.threadStartId = 5;
        b.threadStopId = 6;
        b.threadStartName = "c";
        b.threadStopName = "d";
        b.context = new HashMap<String, String>();
        b.context.put("a", "b");
        b.context.put("c", "d");

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

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

        b.exceptionClass = "ExceptionException";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.exceptionClass = "Exception";

        b.exceptionMessage = "bb";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.exceptionMessage = "b";

        b.success = false;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.success = true;

        b.timeLimitNanoseconds = 100;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.timeLimitNanoseconds = 10;

        b.threadStartId = 55;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.threadStartId = 5;

        b.threadStopId = 66;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.threadStopId = 6;

        b.threadStartName = "cc";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.threadStartName = "c";

        b.threadStopName = "dd";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.threadStopName = "d";

        b.context.put("e", "f");
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.context.remove("e");

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

    }

    @Test
    public void writeReadTest1() {
        final MeterData a = createMeterData();

        final String s = a.write(new StringBuilder(), 'M').toString();
        System.out.println(s);

        final MeterData b = createMeterData();
        assertTrue(b.read(s, 'M'));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest2() {
        final MeterDataMock a = createMeterData();

        a.setEventCategory("aaaa");
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
        a.currentIteration = 4;
        a.exceptionClass = "Exception";
        a.exceptionMessage = "b";
        a.success = true;
        a.timeLimitNanoseconds = 10;
        a.threadStartId = 5;
        a.threadStopId = 6;
        a.threadStartName = "c";
        a.threadStopName = "d";
        a.context = new HashMap<String, String>();
        a.context.put("e", "f");
        a.context.put("g", "h");

        final String s = a.write(new StringBuilder(), 'S').toString();
        System.out.println(s);

        final MeterData b = createMeterData();
        assertTrue(b.read(s, 'S'));

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
