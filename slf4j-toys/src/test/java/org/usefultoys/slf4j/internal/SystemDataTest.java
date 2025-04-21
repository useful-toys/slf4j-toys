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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.usefultoys.slf4j.SystemConfig;

import static org.junit.jupiter.api.Assertions.*;

class SystemDataTest {

    static class TestSystemData extends SystemData {
        public TestSystemData() {
            super();
        }

        public TestSystemData(String sessionUuid) {
            super(sessionUuid);
        }

        public TestSystemData(String sessionUuid, long position) {
            super(sessionUuid, position);
        }

        public TestSystemData(final String sessionUuid, final long position, final long time) {
            super(sessionUuid, position, time);
        }

        public TestSystemData(final String sessionUuid, final long position, final long time, final long heap_commited,
                              final long heap_max, final long heap_used, final long nonHeap_commited, final long nonHeap_max,
                              final long nonHeap_used, final long objectPendingFinalizationCount, final long classLoading_loaded,
                              final long classLoading_total, final long classLoading_unloaded, final long compilationTime,
                              final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory,
                              final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad) {
            super(sessionUuid, position, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max,
                    nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded,
                    compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory,
                    runtime_totalMemory, systemLoad);
        }

        @Override
        protected StringBuilder readableStringBuilder(StringBuilder sb) {
            sb.append("a");
            return sb;
        }

        @Override
        public String encodedMessage() {
            return "";
        }
    }

    @AfterEach
    void tearDown() {
        SystemConfig.useMemoryManagedBean = false;
        SystemConfig.useClassLoadingManagedBean = false;
        SystemConfig.useCompilationManagedBean = false;
        SystemConfig.useGarbageCollectionManagedBean = false;
        SystemConfig.usePlatformManagedBean = false;
    }

    void testConstructorAndGettersN() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(1, event.getPosition());
        assertEquals(2L, event.getTime());
        assertEquals(3L, event.getHeap_commited());
        assertEquals(4L, event.getHeap_max());
        assertEquals(5L, event.getHeap_used());
        assertEquals(6L, event.getNonHeap_commited());
        assertEquals(7L, event.getNonHeap_max());
        assertEquals(8L, event.getNonHeap_used());
        assertEquals(9L, event.getObjectPendingFinalizationCount());
        assertEquals(10L, event.getClassLoading_loaded());
        assertEquals(11L, event.getClassLoading_total());
        assertEquals(12L, event.getClassLoading_unloaded());
        assertEquals(13L, event.getCompilationTime());
        assertEquals(14L, event.getGarbageCollector_count());
        assertEquals(15L, event.getGarbageCollector_time());
        assertEquals(16L, event.getRuntime_usedMemory());
        assertEquals(17L, event.getRuntime_maxMemory());
        assertEquals(18L, event.getRuntime_totalMemory());
        assertEquals(19.0, event.getSystemLoad());


    }

    @Test
    void testResetClearsFields() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        event.reset();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getTime());
        assertEquals(0L, event.getHeap_commited());
        assertEquals(0L, event.getHeap_max());
        assertEquals(0L, event.getHeap_used());
        assertEquals(0L, event.getNonHeap_commited());
        assertEquals(0L, event.getNonHeap_max());
        assertEquals(0L, event.getNonHeap_used());
        assertEquals(0L, event.getObjectPendingFinalizationCount());
        assertEquals(0L, event.getClassLoading_loaded());
        assertEquals(0L, event.getClassLoading_total());
        assertEquals(0L, event.getClassLoading_unloaded());
        assertEquals(0L, event.getCompilationTime());
        assertEquals(0L, event.getGarbageCollector_count());
        assertEquals(0L, event.getGarbageCollector_time());
        assertEquals(0L, event.getRuntime_usedMemory());
        assertEquals(0L, event.getRuntime_maxMemory());
        assertEquals(0L, event.getRuntime_totalMemory());
        assertEquals(0.0, event.getSystemLoad());
    }

    @Test
    void testReadableMessage() {
        final TestSystemData event = new TestSystemData("abc", 5L);
        String message = event.readableMessage();
        assertTrue(message.equals("a"));
    }

    @Test
    void testCollectRuntimeStatus() {
        final TestSystemData event = new TestSystemData("abc");
        event.collectRuntimeStatus();
        assertNotEquals(0L, event.getRuntime_usedMemory());
        assertNotEquals(0L, event.getRuntime_maxMemory());
        assertNotEquals(0L, event.getRuntime_totalMemory());
    }

    @Test() @DisabledOnOs(OS.WINDOWS)
    void testCollectPlatformStatus() {
        SystemConfig.usePlatformManagedBean = true;
        final TestSystemData event = new TestSystemData("abc");
        event.collectPlatformStatus();
        assertNotEquals(0L, event.getSystemLoad());
    }

    @Test
    void testCollectManagedBeanStatus() {
        SystemConfig.useMemoryManagedBean = true;
        SystemConfig.useClassLoadingManagedBean = true;
        SystemConfig.useCompilationManagedBean = true;
        SystemConfig.useGarbageCollectionManagedBean = true;

        final TestSystemData event = new TestSystemData("abc");
        System.gc();
        event.collectManagedBeanStatus();
        assertNotEquals(0L, event.getHeap_commited());
        assertNotEquals(0L, event.getHeap_max());
        assertNotEquals(0L, event.getHeap_used());
        assertNotEquals(0L, event.getNonHeap_commited());
        assertNotEquals(0L, event.getNonHeap_max());
        assertNotEquals(0L, event.getNonHeap_used());
//        assertNotEquals(0L, event.getObjectPendingFinalizationCount());
        assertNotEquals(0L, event.getClassLoading_loaded());
        assertNotEquals(0L, event.getClassLoading_total());
//        assertNotEquals(0L, event.getClassLoading_unloaded());
        assertNotEquals(0L, event.getCompilationTime());
        assertNotEquals(0L, event.getGarbageCollector_count());
        assertNotEquals(0L, event.getGarbageCollector_time());
    }

    @Test
    void testJsonMessageAllAttributes() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
    }

    @Test
    void testJsonMessageEssentialAttributes() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,h:[5,3,4]}", json);
    }

    @Test
    void testJsonMessageWithoutHeapAttributes() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 0, 0, 0, 0, 0, 0, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
    }

    @Test
    void testJsonMessageWithoutClassLoadingAttributes() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 13, 14, 15, 16, 17, 18, 19.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],ct:13,gc:[14,15],sl:19.0}", json);
    }

    @Test
    void testJsonMessageWithoutGarbageCollectorAttributes() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0, 0, 16, 17, 18, 19.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,sl:19.0}", json);
    }

    @Test
    void testJsonMessageWithoutSystemLoad() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 0.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15]}", json);
    }

    void testJsonMessageMemory() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 0, 0, 19.0);
        String json = event.json5Message();
        assertEquals("{_:abc,$:1,t:2,h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
    }

    @Test
    void testReadJson5SystemDataMessage() {
        final SystemData systemData = new SystemData() {
            @Override
            protected StringBuilder readableStringBuilder(StringBuilder sb) {
                return sb;
            }

            @Override
            protected void writePropertiesImpl(EventWriter w) {
                // no-op
            }

            @Override
            public String encodedMessage() {
                return "";
            }
        };

        systemData.readJson5("{_:abc,$:5,t:6,m:[100,200,300],h:[400,500,600],nh:[700,800,900],fc:10,cl:[20,30,40],ct:50,gc:[60,70],sl:0.8}");
        assertEquals("abc", systemData.getSessionUuid());
        assertEquals(5L, systemData.getPosition());
        assertEquals(6L, systemData.getTime());
        assertEquals(100L, systemData.getRuntime_usedMemory());
        assertEquals(200L, systemData.getRuntime_totalMemory());
        assertEquals(300L, systemData.getRuntime_maxMemory());
        assertEquals(400L, systemData.getHeap_used());
        assertEquals(500L, systemData.getHeap_commited());
        assertEquals(600L, systemData.getHeap_max());
        assertEquals(700L, systemData.getNonHeap_used());
        assertEquals(800L, systemData.getNonHeap_commited());
        assertEquals(900L, systemData.getNonHeap_max());
        assertEquals(10L, systemData.getObjectPendingFinalizationCount());
        assertEquals(20L, systemData.getClassLoading_total());
        assertEquals(30L, systemData.getClassLoading_loaded());
        assertEquals(40L, systemData.getClassLoading_unloaded());
        assertEquals(50L, systemData.getCompilationTime());
        assertEquals(60L, systemData.getGarbageCollector_count());
        assertEquals(70L, systemData.getGarbageCollector_time());
        assertEquals(0.8, systemData.getSystemLoad(), 0.01);
    }
}
