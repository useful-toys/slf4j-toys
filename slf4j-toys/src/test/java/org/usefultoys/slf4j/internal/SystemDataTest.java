/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemDataTest {

    static class TestSystemData extends SystemData {
        public TestSystemData() {
            super();
        }

        public TestSystemData(final String sessionUuid, final long position) {
            super(sessionUuid); // Call EventData constructor
            this.position = position; // Set position directly as EventData constructor doesn't take it
        }

        // for tests only
        protected TestSystemData(final String sessionUuid, final long position, final long lastCurrentTime,
                              final long heap_commited, final long heap_max, final long heap_used,
                              final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used,
                              final long objectPendingFinalizationCount, final long classLoading_loaded,
                              final long classLoading_total, final long classLoading_unloaded, final long compilationTime,
                              final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory,
                              final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad) {
            super(sessionUuid, position, lastCurrentTime, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max,
                    nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded,
                    compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory,
                    runtime_totalMemory, systemLoad);
        }
    }

    @BeforeAll
    static void setupConsistentLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetConfigsBeforeEach() {
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetConfigsAfterAll() {
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @Test
    void testConstructorAndGetters() {
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(1, event.getPosition());
        assertEquals(2L, event.getLastCurrentTime());
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
        assertEquals(0L, event.getLastCurrentTime());
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

//    @Test
//    void testCollectRuntimeStatus() {
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectRuntimeStatus();
//        assertNotEquals(0L, event.getRuntime_usedMemory());
//        assertNotEquals(0L, event.getRuntime_maxMemory());
//        assertNotEquals(0L, event.getRuntime_totalMemory());
//    }
//
//    @Test
//    void testCollectPlatformStatusWhenEnabled() {
//        SystemConfig.usePlatformManagedBean = true;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectPlatformStatus();
//        // systemLoad can be 0.0 if the OS bean doesn't provide it or it's genuinely 0.
//        // We can only assert that it attempts to collect it.
//        // For a more robust test, mocking ManagementFactory.getOperatingSystemMXBean() would be needed,
//        // which is complex due to static/final nature.
//        // For now, we assert that it's not negative, as per the code's logic.
//        assertTrue(event.getSystemLoad() >= 0.0);
//    }
//
//    @Test
//    void testCollectPlatformStatusWhenDisabled() {
//        SystemConfig.usePlatformManagedBean = false; // Default, but explicit for test
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectPlatformStatus();
//        assertEquals(0.0, event.getSystemLoad()); // Should remain default 0.0
//    }
//
//    @Test
//    void testCollectManagedBeanStatusMemoryEnabled() {
//        SystemConfig.useMemoryManagedBean = true;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        System.gc(); // Hint for GC to run, might affect counts
//        event.collectManagedBeanStatus();
//        assertNotEquals(0L, event.getHeap_commited());
//        assertNotEquals(0L, event.getHeap_max());
//        assertNotEquals(0L, event.getHeap_used());
//        assertNotEquals(0L, event.getNonHeap_commited());
//        assertNotEquals(0L, event.getNonHeap_max());
//        assertNotEquals(0L, event.getNonHeap_used());
//        // objectPendingFinalizationCount can legitimately be 0
//    }
//
//    @Test
//    void testCollectManagedBeanStatusMemoryDisabled() {
//        SystemConfig.useMemoryManagedBean = false;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        assertEquals(0L, event.getHeap_commited());
//        assertEquals(0L, event.getHeap_max());
//        assertEquals(0L, event.getHeap_used());
//        assertEquals(0L, event.getNonHeap_commited());
//        assertEquals(0L, event.getNonHeap_max());
//        assertEquals(0L, event.getNonHeap_used());
//        assertEquals(0L, event.getObjectPendingFinalizationCount());
//    }
//
//    @Test
//    void testCollectManagedBeanStatusClassLoadingEnabled() {
//        SystemConfig.useClassLoadingManagedBean = true;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        assertNotEquals(0L, event.getClassLoading_loaded());
//        assertNotEquals(0L, event.getClassLoading_total());
//        // unloaded can legitimately be 0
//    }
//
//    @Test
//    void testCollectManagedBeanStatusClassLoadingDisabled() {
//        SystemConfig.useClassLoadingManagedBean = false;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        assertEquals(0L, event.getClassLoading_loaded());
//        assertEquals(0L, event.getClassLoading_total());
//        assertEquals(0L, event.getClassLoading_unloaded());
//    }
//
//    @Test
//    void testCollectManagedBeanStatusCompilationEnabled() {
//        SystemConfig.useCompilationManagedBean = true;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        // Compilation time can be 0 if JIT is not active or no compilation occurred yet
//        assertTrue(event.getCompilationTime() >= 0L);
//    }
//
//    @Test
//    void testCollectManagedBeanStatusCompilationDisabled() {
//        SystemConfig.useCompilationManagedBean = false;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        assertEquals(0L, event.getCompilationTime());
//    }
//
//    @Test
//    void testCollectManagedBeanStatusGarbageCollectionEnabled() {
//        SystemConfig.useGarbageCollectionManagedBean = true;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        System.gc(); // Hint for GC to run, might affect counts
//        event.collectManagedBeanStatus();
//        // These can be 0 if no GC occurred or if the JVM doesn't provide them
//        assertTrue(event.getGarbageCollector_count() >= 0L);
//        assertTrue(event.getGarbageCollector_time() >= 0L);
//    }
//
//    @Test
//    void testCollectManagedBeanStatusGarbageCollectionDisabled() {
//        SystemConfig.useGarbageCollectionManagedBean = false;
//        final TestSystemData event = new TestSystemData("abc", 5L);
//        event.collectManagedBeanStatus();
//        assertEquals(0L, event.getGarbageCollector_count());
//        assertEquals(0L, event.getGarbageCollector_time());
//    }
//
//    @Test
//    void testJsonMessageAllAttributes() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
//    }
//
//    @Test
//    void testJsonMessageEssentialAttributes() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,h:[5,3,4]}", json);
//    }
//
//    @Test
//    void testJsonMessageWithoutHeapAttributes() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 0, 0, 0, 0, 0, 0, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
//    }
//
//    @Test
//    void testJsonMessageWithoutClassLoadingAttributes() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 0, 13, 14, 15, 16, 17, 18, 19.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],ct:13,gc:[14,15],sl:19.0}", json);
//    }
//
//    @Test
//    void testJsonMessageWithoutGarbageCollectorAttributes() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0, 0, 16, 17, 18, 19.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,sl:19.0}", json);
//    }
//
//    @Test
//    void testJsonMessageWithoutSystemLoad() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 0.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,m:[16,18,17],h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15]}", json);
//    }
//
//    @Test
//    void testJsonMessageMemory() {
//        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 0, 0, 19.0);
//        final String json = event.json5Message();
//        assertEquals("{_:abc,$:1,t:2,h:[5,3,4],nh:[8,6,7],fc:9,cl:[11,10,12],ct:13,gc:[14,15],sl:19.0}", json);
//    }
//
//    @Test
//    void testReadJson5SystemDataMessage() {
//        final SystemData systemData = new TestSystemData(); // Use TestSystemData for concrete instance
//
//        systemData.readJson5("{_:abc,$:5,t:6,m:[100,200,300],h:[400,500,600],nh:[700,800,900],fc:10,cl:[20,30,40],ct:50,gc:[60,70],sl:0.8}");
//        assertEquals("abc", systemData.getSessionUuid());
//        assertEquals(5L, systemData.getPosition());
//        assertEquals(6L, systemData.getLastCurrentTime());
//        assertEquals(100L, systemData.getRuntime_usedMemory());
//        assertEquals(200L, systemData.getRuntime_totalMemory());
//        assertEquals(300L, systemData.getRuntime_maxMemory());
//        assertEquals(400L, systemData.getHeap_used());
//        assertEquals(500L, systemData.getHeap_commited());
//        assertEquals(600L, systemData.getHeap_max());
//        assertEquals(700L, systemData.getNonHeap_used());
//        assertEquals(800L, systemData.getNonHeap_commited());
//        assertEquals(900L, systemData.getNonHeap_max());
//        assertEquals(10L, systemData.getObjectPendingFinalizationCount());
//        assertEquals(20L, systemData.getClassLoading_total());
//        assertEquals(30L, systemData.getClassLoading_loaded());
//        assertEquals(40L, systemData.getClassLoading_unloaded());
//        assertEquals(50L, systemData.getCompilationTime());
//        assertEquals(60L, systemData.getGarbageCollector_count());
//        assertEquals(70L, systemData.getGarbageCollector_time());
//        assertEquals(0.8, systemData.getSystemLoad(), 0.01);
//    }
//
//    @Test
//    void testReadJson5SystemDataMessageMissingAttributes() {
//        final TestSystemData systemData = new TestSystemData("initialSession", 100L, 200L, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17.0);
//
//        // Test with only session, position, time
//        systemData.readJson5("{_:newSession,$:1,t:2}");
//        assertEquals("newSession", systemData.getSessionUuid());
//        assertEquals(1L, systemData.getPosition());
//        assertEquals(2L, systemData.getLastCurrentTime());
//        assertEquals(1L, systemData.getHeap_commited()); // Should retain old value
//        assertEquals(14L, systemData.getRuntime_usedMemory()); // Should retain old value
//
//        // Reset and test with only memory
//        systemData.reset();
//        systemData.readJson5("{m:[100,200,300]}");
//        assertEquals(100L, systemData.getRuntime_usedMemory());
//        assertEquals(200L, systemData.getRuntime_totalMemory());
//        assertEquals(300L, systemData.getRuntime_maxMemory());
//        assertNull(systemData.getSessionUuid()); // Should retain default null
//        assertEquals(0L, systemData.getPosition()); // Should retain default 0
//
//        // Reset and test with only heap
//        systemData.reset();
//        systemData.readJson5("{h:[400,500,600]}");
//        assertEquals(400L, systemData.getHeap_used());
//        assertEquals(500L, systemData.getHeap_commited());
//        assertEquals(600L, systemData.getHeap_max());
//
//        // Reset and test with only non-heap
//        systemData.reset();
//        systemData.readJson5("{nh:[700,800,900]}");
//        assertEquals(700L, systemData.getNonHeap_used());
//        assertEquals(800L, systemData.getNonHeap_commited());
//        assertEquals(900L, systemData.getNonHeap_max());
//
//        // Reset and test with only finalization count
//        systemData.reset();
//        systemData.readJson5("{fc:10}");
//        assertEquals(10L, systemData.getObjectPendingFinalizationCount());
//
//        // Reset and test with only class loading
//        systemData.reset();
//        systemData.readJson5("{cl:[20,30,40]}");
//        assertEquals(20L, systemData.getClassLoading_total());
//        assertEquals(30L, systemData.getClassLoading_loaded());
//        assertEquals(40L, systemData.getClassLoading_unloaded());
//
//        // Reset and test with only compilation time
//        systemData.reset();
//        systemData.readJson5("{ct:50}");
//        assertEquals(50L, systemData.getCompilationTime());
//
//        // Reset and test with only garbage collector
//        systemData.reset();
//        systemData.readJson5("{gc:[60,70]}");
//        assertEquals(60L, systemData.getGarbageCollector_count());
//        assertEquals(70L, systemData.getGarbageCollector_time());
//
//        // Reset and test with only system load
//        systemData.reset();
//        systemData.readJson5("{sl:0.8}");
//        assertEquals(0.8, systemData.getSystemLoad(), 0.01);
//    }
}
