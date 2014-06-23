/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class SystemDataTest {

    public SystemDataTest() {
    }

    @Test
    public void testResetImpl() {
        SystemData a = createSystemData();
        SystemData b = createSystemData();

        b.heap_commited = 1;
//        b.heap_init = 2;
        b.heap_max = 3;
        b.heap_used = 4;
        b.nonHeap_commited = 5;
//        b.nonHeap_init = 6;
        b.nonHeap_max = 7;
        b.nonHeap_used = 8;
        b.objectPendingFinalizationCount = 9;
        b.classLoading_loaded = 10;
        b.classLoading_total = 11;
        b.classLoading_unloaded = 12;
        b.compilationTime = 13;
        b.garbageCollector_count = 14;
        b.garbageCollector_time = 15;
        b.runtime_usedMemory = 16;
        b.runtime_maxMemory = 17;
        b.runtime_totalMemory = 18;
        b.systemLoad = 1.0;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.reset();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void testIsCompletelyEqualsImpl() {
        SystemData a = createSystemData();
        SystemData b = createSystemData();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        a.heap_commited = 1;
//        a.heap_init = 2;
        a.heap_max = 3;
        a.heap_used = 4;
        a.nonHeap_commited = 5;
//        a.nonHeap_init = 6;
        a.nonHeap_max = 7;
        a.nonHeap_used = 8;
        a.objectPendingFinalizationCount = 9;
        a.classLoading_loaded = 10;
        a.classLoading_total = 11;
        a.classLoading_unloaded = 12;
        a.compilationTime = 13;
        a.garbageCollector_count = 14;
        a.garbageCollector_time = 15;
        a.runtime_usedMemory = 16;
        a.runtime_maxMemory = 17;
        a.runtime_totalMemory = 18;
        a.systemLoad = 1.0;

        b.heap_commited = 1;
//        b.heap_init = 2;
        b.heap_max = 3;
        b.heap_used = 4;
        b.nonHeap_commited = 5;
//        b.nonHeap_init = 6;
        b.nonHeap_max = 7;
        b.nonHeap_used = 8;
        b.objectPendingFinalizationCount = 9;
        b.classLoading_loaded = 10;
        b.classLoading_total = 11;
        b.classLoading_unloaded = 12;
        b.compilationTime = 13;
        b.garbageCollector_count = 14;
        b.garbageCollector_time = 15;
        b.runtime_usedMemory = 16;
        b.runtime_maxMemory = 17;
        b.runtime_totalMemory = 18;
        b.systemLoad = 1.0;

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.heap_commited = 11;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.heap_commited = 1;
//        b.heap_init = 22;

//        assertFalse(a.isCompletelyEqualsTo(b));
//        assertFalse(b.isCompletelyEqualsTo(a));

//        b.heap_init = 2;
        b.heap_max = 33;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.heap_max = 3;
        b.heap_used = 44;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.heap_used = 4;
        b.nonHeap_commited = 55;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.nonHeap_commited = 5;
//        b.nonHeap_init = 66;

//        assertFalse(a.isCompletelyEqualsTo(b));
//        assertFalse(b.isCompletelyEqualsTo(a));

//        b.nonHeap_init = 6;
        b.nonHeap_max = 77;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.nonHeap_max = 7;
        b.nonHeap_used = 88;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.nonHeap_used = 8;
        b.objectPendingFinalizationCount = 99;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.objectPendingFinalizationCount = 9;
        b.classLoading_loaded = 1010;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.classLoading_loaded = 10;
        b.classLoading_total = 1111;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.classLoading_total = 11;
        b.classLoading_unloaded = 1212;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.classLoading_unloaded = 12;
        b.compilationTime = 1313;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.compilationTime = 13;
        b.garbageCollector_count = 1414;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.garbageCollector_count = 14;
        b.garbageCollector_time = 1515;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.garbageCollector_time = 15;
        b.runtime_usedMemory = 1616;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.runtime_usedMemory = 16;
        b.runtime_maxMemory = 1717;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.runtime_maxMemory = 17;
        b.runtime_totalMemory = 1818;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.runtime_totalMemory = 18;
        b.systemLoad = 2.0;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.systemLoad = 1.0;

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

    }

    @Test
    public void writeReadTest1() {
        SystemData a = createSystemData();

        String s = a.write(new StringBuilder(), 'S').toString();
        System.out.println(s);

        SystemData b = createSystemData();
        assertTrue(b.read(s, 'S'));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest2() {
        SystemData a = createSystemData();

        a.eventCategory = "aaaa";
        a.eventPosition = 1111;
        a.sessionUuid = "bbbb";
        a.time = 2222;

        a.heap_commited = 1;
//        a.heap_init = 2;
        a.heap_max = 3;
        a.heap_used = 4;
        a.nonHeap_commited = 5;
//        a.nonHeap_init = 6;
        a.nonHeap_max = 7;
        a.nonHeap_used = 8;
        a.objectPendingFinalizationCount = 9;
        a.classLoading_loaded = 10;
        a.classLoading_total = 11;
        a.classLoading_unloaded = 12;
        a.compilationTime = 13;
        a.garbageCollector_count = 14;
        a.garbageCollector_time = 15;
        a.runtime_usedMemory = 16;
        a.runtime_maxMemory = 17;
        a.runtime_totalMemory = 18;
        a.systemLoad = 1.0;

        String s = a.write(new StringBuilder(), 'S').toString();
        System.out.println(s);

        SystemData b = createSystemData();
        assertTrue(b.read(s, 'S'));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    private SystemData createSystemData() {
        return new SystemData() {
        	private static final long serialVersionUID = 1L;
        	@Override
            public StringBuilder readableString(StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

}
