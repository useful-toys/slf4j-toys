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
