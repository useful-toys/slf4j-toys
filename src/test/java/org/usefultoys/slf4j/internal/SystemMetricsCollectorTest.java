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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.test.ResetSystemConfig;
import org.usefultoys.test.ValidateCharset;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SystemMetricsCollector}.
 * <p>
 * Tests verify that SystemMetricsCollector correctly collects system metrics from various MXBeans,
 * handles configuration flags, and gracefully handles edge cases and null beans.
 */
@DisplayName("SystemMetricsCollector")
@ValidateCharset
@ResetSystemConfig
class SystemMetricsCollectorTest {
    @Mock
    private com.sun.management.OperatingSystemMXBean mockSunOsBean;
    @Mock
    private OperatingSystemMXBean mockGenericOsBean; // For instanceof test
    @Mock
    private MemoryMXBean mockMemoryBean;
    @Mock
    private ClassLoadingMXBean mockClassLoadingBean;
    @Mock
    private CompilationMXBean mockCompilationBean;
    @Mock
    private GarbageCollectorMXBean mockGcBean1;
    @Mock
    private GarbageCollectorMXBean mockGcBean2;

    private AutoCloseable mockitoCloseable;
    private long totalMemory;
    private long usedMemory;
    private long maxMemory;

    // A test-specific subclass to override collectRuntimeStatus
    private class TestableSystemMetricsCollector extends SystemMetricsCollector {
        public TestableSystemMetricsCollector(final OperatingSystemMXBean osBean) {
            super(osBean, mockMemoryBean, mockClassLoadingBean, mockCompilationBean, Arrays.asList(mockGcBean1, mockGcBean2));
        }

        @Override
        public void collectRuntimeStatus(final SystemData data) {
            data.runtime_totalMemory = totalMemory;
            data.runtime_usedMemory = usedMemory;
            data.runtime_maxMemory = maxMemory;
        }
    }

    private TestableSystemMetricsCollector collector;
    private SystemData data;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        SystemConfig.reset();
        SystemConfig.init();
        collector = new TestableSystemMetricsCollector(mockSunOsBean);
        data = new SystemData() {
            // Anonymous concrete class
        };
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    @DisplayName("should collect all metrics when all configs are enabled")
    void collect_fullCollection() {
        // Given: all metrics enabled
        SystemConfig.usePlatformManagedBean = true;
        SystemConfig.useMemoryManagedBean = true;
        SystemConfig.useClassLoadingManagedBean = true;
        SystemConfig.useCompilationManagedBean = true;
        SystemConfig.useGarbageCollectionManagedBean = true;

        totalMemory = 2000L;
        usedMemory = 500L;
        maxMemory = 4000L;

        when(mockSunOsBean.getSystemCpuLoad()).thenReturn(0.5);

        final MemoryUsage mockHeap = new MemoryUsage(0, 100, 200, 300);
        final MemoryUsage mockNonHeap = new MemoryUsage(0, 400, 500, 600);
        when(mockMemoryBean.getHeapMemoryUsage()).thenReturn(mockHeap);
        when(mockMemoryBean.getNonHeapMemoryUsage()).thenReturn(mockNonHeap);
        when(mockMemoryBean.getObjectPendingFinalizationCount()).thenReturn(10);

        when(mockClassLoadingBean.getLoadedClassCount()).thenReturn(1000);
        when(mockClassLoadingBean.getTotalLoadedClassCount()).thenReturn(1500L);
        when(mockClassLoadingBean.getUnloadedClassCount()).thenReturn(500L);

        when(mockCompilationBean.getTotalCompilationTime()).thenReturn(5000L);

        when(mockGcBean1.getCollectionCount()).thenReturn(10L);
        when(mockGcBean1.getCollectionTime()).thenReturn(100L);
        when(mockGcBean2.getCollectionCount()).thenReturn(5L);
        when(mockGcBean2.getCollectionTime()).thenReturn(50L);

        // When: collect is called
        collector.collect(data);

        // Then: all metrics should be collected correctly
        assertEquals(usedMemory, data.getRuntime_usedMemory());
        assertEquals(totalMemory, data.getRuntime_totalMemory());
        assertEquals(maxMemory, data.getRuntime_maxMemory());
        assertEquals(0.5, data.getSystemLoad());
        assertEquals(100L, data.getHeap_used());
        assertEquals(200L, data.getHeap_commited());
        assertEquals(300L, data.getHeap_max());
        assertEquals(400L, data.getNonHeap_used());
        assertEquals(500L, data.getNonHeap_commited());
        assertEquals(600L, data.getNonHeap_max());
        assertEquals(10, data.getObjectPendingFinalizationCount());
        assertEquals(1000L, data.getClassLoading_loaded());
        assertEquals(1500L, data.getClassLoading_total());
        assertEquals(500L, data.getClassLoading_unloaded());
        assertEquals(5000L, data.getCompilationTime());
        assertEquals(15L, data.getGarbageCollector_count());
        assertEquals(150L, data.getGarbageCollector_time());
    }

    @Test
    @DisplayName("should not collect platform metrics when disabled")
    void collect_platformDisabled() {
        // Given: platform metrics disabled
        SystemConfig.usePlatformManagedBean = false;

        // When: collect is called
        collector.collect(data);

        // Then: platform bean should not be called and system load remains 0.0
        verify(mockSunOsBean, never()).getSystemCpuLoad();
        assertEquals(0.0, data.getSystemLoad());
    }

    @Test
    @DisplayName("should use fallback for system load when getSystemCpuLoad is negative")
    void collect_cpuLoadFallback() {
        // Given: system CPU load returns -1.0 (invalid)
        SystemConfig.usePlatformManagedBean = true;
        when(mockSunOsBean.getSystemCpuLoad()).thenReturn(-1.0);
        when(mockSunOsBean.getSystemLoadAverage()).thenReturn(1.6);
        when(mockSunOsBean.getAvailableProcessors()).thenReturn(8);

        // When: collectPlatformStatus is called
        collector.collectPlatformStatus(data);

        // Then: should use fallback calculation (1.6 / 8 = 0.2)
        assertEquals(0.2, data.getSystemLoad(), 0.001);
    }

    @Test
    @DisplayName("should ignore fallback when loadAverage is negative")
    void collect_ignoreFallbackOnNegativeLoad() {
        // Given: system load average returns -1.0 (invalid)
        SystemConfig.usePlatformManagedBean = true;
        when(mockSunOsBean.getSystemCpuLoad()).thenReturn(-1.0);
        when(mockSunOsBean.getSystemLoadAverage()).thenReturn(-1.0);
        data.systemLoad = 99.0;

        // When: collectPlatformStatus is called
        collector.collectPlatformStatus(data);

        // Then: system load should not be changed
        assertEquals(99.0, data.getSystemLoad(), "System load should not be changed");
    }

    @Test
    @DisplayName("should ignore fallback when availableProcessors is zero")
    void collect_ignoreFallbackOnZeroProcessors() {
        // Given: available processors is 0
        SystemConfig.usePlatformManagedBean = true;
        when(mockSunOsBean.getSystemCpuLoad()).thenReturn(-1.0);
        when(mockSunOsBean.getSystemLoadAverage()).thenReturn(1.6);
        when(mockSunOsBean.getAvailableProcessors()).thenReturn(0);
        data.systemLoad = 99.0;

        // When: collectPlatformStatus is called
        collector.collectPlatformStatus(data);

        // Then: system load should not be changed
        assertEquals(99.0, data.getSystemLoad(), "System load should not be changed");
    }

    @Test
    @DisplayName("should use fallback for system load when OSBean is not a Sun bean")
    void collect_cpuLoadFallbackOnNonSunBean() {
        // Given: generic OperatingSystemMXBean (not Sun implementation)
        SystemConfig.usePlatformManagedBean = true;
        when(mockGenericOsBean.getSystemLoadAverage()).thenReturn(2.4);
        when(mockGenericOsBean.getAvailableProcessors()).thenReturn(4);
        final TestableSystemMetricsCollector nonSunCollector = new TestableSystemMetricsCollector(mockGenericOsBean);

        // When: collectPlatformStatus is called
        nonSunCollector.collectPlatformStatus(data);

        // Then: should calculate using fallback (2.4 / 4 = 0.6)
        assertEquals(0.6, data.getSystemLoad(), 0.001);
    }

    @Test
    @DisplayName("should not collect memory metrics when disabled")
    void collect_memoryDisabled() {
        // Given: memory metrics disabled
        SystemConfig.useMemoryManagedBean = false;

        // When: collect is called
        collector.collect(data);

        // Then: memory bean should not be called
        verify(mockMemoryBean, never()).getHeapMemoryUsage();
        assertEquals(0, data.getHeap_used());
    }

    @Test
    @DisplayName("should not collect class loading metrics when disabled")
    void collect_classLoadingDisabled() {
        // Given: class loading metrics disabled
        SystemConfig.useClassLoadingManagedBean = false;

        // When: collectManagedBeanStatus is called
        collector.collectManagedBeanStatus(data);

        // Then: class loading bean should not be called
        verify(mockClassLoadingBean, never()).getLoadedClassCount();
        assertEquals(0, data.getClassLoading_loaded());
    }

    @Test
    @DisplayName("should not collect compilation metrics when disabled")
    void collect_compilationDisabled() {
        // Given: compilation metrics disabled
        SystemConfig.useCompilationManagedBean = false;

        // When: collectManagedBeanStatus is called
        collector.collectManagedBeanStatus(data);

        // Then: compilation bean should not be called
        verify(mockCompilationBean, never()).getTotalCompilationTime();
        assertEquals(0, data.getCompilationTime());
    }

    @Test
    @DisplayName("should not collect GC metrics when disabled")
    void collect_gcDisabled() {
        // Given: garbage collection metrics disabled
        SystemConfig.useGarbageCollectionManagedBean = false;

        // When: collectManagedBeanStatus is called
        collector.collectManagedBeanStatus(data);

        // Then: GC bean should not be called
        verify(mockGcBean1, never()).getCollectionCount();
        assertEquals(0, data.getGarbageCollector_count());
    }

    @Test
    @DisplayName("should correctly aggregate metrics from multiple GC beans")
    void collect_aggregatesGcMetrics() {
        // Given: garbage collection metrics enabled with multiple GC beans
        SystemConfig.useGarbageCollectionManagedBean = true;
        when(mockGcBean1.getCollectionCount()).thenReturn(10L);
        when(mockGcBean1.getCollectionTime()).thenReturn(100L);
        when(mockGcBean2.getCollectionCount()).thenReturn(5L);
        when(mockGcBean2.getCollectionTime()).thenReturn(50L);

        // When: collectManagedBeanStatus is called
        collector.collectManagedBeanStatus(data);

        // Then: metrics should be aggregated correctly
        assertEquals(15L, data.getGarbageCollector_count());
        assertEquals(150L, data.getGarbageCollector_time());
    }

    @Test
    @DisplayName("should handle empty GC bean list gracefully")
    void collect_emptyGcList() {
        // Given: empty GC bean list
        SystemConfig.useGarbageCollectionManagedBean = true;
        final SystemMetricsCollector emptyGcCollector = new SystemMetricsCollector(null, null, null, null, Collections.emptyList());

        // When: collectManagedBeanStatus is called
        emptyGcCollector.collectManagedBeanStatus(data);

        // Then: GC metrics should remain 0
        assertEquals(0L, data.getGarbageCollector_count());
        assertEquals(0L, data.getGarbageCollector_time());
    }

    @Test
    @DisplayName("should handle null beans gracefully")
    void collect_nullBeans() {
        // Given: all beans are null
        SystemConfig.usePlatformManagedBean = true;
        SystemConfig.useMemoryManagedBean = true;
        final SystemMetricsCollector nullCollector = new SystemMetricsCollector(null, null, null, null, null);

        // When: collect is called
        nullCollector.collect(data);

        // Then: should not throw exception and values should remain default
        assertEquals(0.0, data.getSystemLoad());
        assertEquals(0, data.getHeap_used());
    }

    @Test
    @DisplayName("should collect real runtime status without errors")
    void collectRuntimeStatus_realRuntime() {
        // Given: collector with real Runtime (not mocked)
        final SystemMetricsCollector realCollector = new SystemMetricsCollector(null, null, null, null, null);
        final SystemData realData = new SystemData() {
            // Anonymous concrete class
        };

        // When: collectRuntimeStatus is called
        realCollector.collectRuntimeStatus(realData);

        // Then: should have collected valid runtime memory values
        assertTrue(realData.getRuntime_maxMemory() > 0, "Max memory should be greater than 0");
        assertTrue(realData.getRuntime_totalMemory() > 0, "Total memory should be greater than 0");
        assertTrue(realData.getRuntime_usedMemory() >= 0, "Used memory should be greater than or equal to 0");
        assertTrue(realData.getRuntime_totalMemory() <= realData.getRuntime_maxMemory(), "Total memory should be less than or equal to max memory");
    }
}
