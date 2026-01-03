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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link SystemData}.
 * <p>
 * Tests verify that SystemData correctly initializes, manages, and resets system monitoring information
 * including memory, garbage collection, class loading, and compilation metrics.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Constructor Initialization:</b> Tests initialization with full constructor setting all system metrics</li>
 *   <li><b>Reset Functionality:</b> Verifies that reset clears all fields to default values</li>
 *   <li><b>Field Management:</b> Ensures correct setting and getting of memory, GC, class loading, and compilation data</li>
 * </ul>
 */
@DisplayName("SystemData")
@ValidateCharset
@WithLocale("en")
class SystemDataTest {

    static class TestSystemData extends SystemData {
        public TestSystemData() {
        }

        public TestSystemData(final String sessionUuid, final long position) {
            super(sessionUuid);
            this.position = position;
        }

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

    @Test
    @DisplayName("should initialize all fields correctly with full constructor")
    void testConstructorAndGetters() {
        // Given: SystemData with all parameters
        // When: object is created
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);

        // Then: all fields should be set correctly
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
    @DisplayName("should reset all fields to default values")
    void testResetClearsFields() {
        // Given: SystemData with values set
        final TestSystemData event = new TestSystemData("abc", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19.0);

        // When: reset is called
        event.reset();

        // Then: all fields should return to default values
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
}
