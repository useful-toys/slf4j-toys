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
package org.usefultoys.slf4j.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Daniel
 */
public class SystemDataTest {

    public SystemDataTest() {
    }

    @Test
    public void testResetImpl() {
        final SystemData a = createSystemData();
        final SystemData b = createSystemData();

        populateTestSystemData(b);

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        resetAll(b);

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void testIsCompletelyEqualsImpl() {
        final SystemData a = createSystemData();
        final SystemData b = createSystemData();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        populateEventData(a);
        populateEventData(b);
        populateTestSystemData(a);
        populateTestSystemData(b);

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.heap_commited = 11;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.heap_commited = 1;
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

       public static void populateEventData(EventData a) {
        a.position = 1111;
        a.sessionUuid = "bbbb";
        a.time = 2222;
    }

    public static void populateTestSystemData(SystemData a) {
        a.heap_commited = 1;
        a.heap_max = 3;
        a.heap_used = 4;
        a.nonHeap_commited = 5;
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
    }

    public static void resetAll(SystemData b) {
        b.reset();
    }

    private SystemData createSystemData() {
        return new SystemData() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuilder readableString(final StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

}
