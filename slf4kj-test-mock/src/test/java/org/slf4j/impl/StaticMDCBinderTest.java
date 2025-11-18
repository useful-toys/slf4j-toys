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
package org.slf4j.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.spi.MDCAdapter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StaticMDCBinder}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("StaticMDCBinder Tests")
class StaticMDCBinderTest {

    @Test
    @DisplayName("Should return singleton instance")
    void shouldReturnSingletonInstance() {
        // When
        StaticMDCBinder instance1 = StaticMDCBinder.SINGLETON;
        StaticMDCBinder instance2 = StaticMDCBinder.SINGLETON;
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should return MockMDCAdapter instance")
    void shouldReturnMockMDCAdapterInstance() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        
        // When
        MDCAdapter mdcAdapter = binder.getMDCA();
        
        // Then
        assertNotNull(mdcAdapter);
        assertTrue(mdcAdapter instanceof MockMDCAdapter);
    }

    @Test
    @DisplayName("Should return same MDC adapter instance on multiple calls")
    void shouldReturnSameMDCAdapterInstanceOnMultipleCalls() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        
        // When
        MDCAdapter adapter1 = binder.getMDCA();
        MDCAdapter adapter2 = binder.getMDCA();
        
        // Then
        assertNotNull(adapter1);
        assertNotNull(adapter2);
        assertSame(adapter1, adapter2);
    }

    @Test
    @DisplayName("Should return correct MDC adapter class name")
    void shouldReturnCorrectMDCAdapterClassName() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        
        // When
        String className = binder.getMDCAdapterClassStr();
        
        // Then
        assertEquals(MockMDCAdapter.class.getName(), className);
        assertEquals("org.slf4j.impl.MockMDCAdapter", className);
    }

    @Test
    @DisplayName("Should maintain singleton pattern across different access methods")
    void shouldMaintainSingletonPatternAcrossDifferentAccessMethods() {
        // When
        StaticMDCBinder instance1 = StaticMDCBinder.SINGLETON;
        StaticMDCBinder instance2 = StaticMDCBinder.SINGLETON;
        
        MDCAdapter adapter1 = instance1.getMDCA();
        MDCAdapter adapter2 = instance2.getMDCA();
        
        // Then
        assertSame(instance1, instance2);
        assertSame(adapter1, adapter2);
        assertTrue(adapter1 instanceof MockMDCAdapter);
        assertTrue(adapter2 instanceof MockMDCAdapter);
    }

    @Test
    @DisplayName("Should be thread-safe singleton")
    void shouldBeThreadSafeSingleton() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        StaticMDCBinder[] instances = new StaticMDCBinder[numberOfThreads];
        Thread[] threads = new Thread[numberOfThreads];
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = StaticMDCBinder.SINGLETON;
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        StaticMDCBinder firstInstance = instances[0];
        assertNotNull(firstInstance);
        
        for (int i = 1; i < numberOfThreads; i++) {
            assertNotNull(instances[i]);
            assertSame(firstInstance, instances[i]);
        }
    }

    @Test
    @DisplayName("Should have consistent state across multiple accesses")
    void shouldHaveConsistentStateAcrossMultipleAccesses() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        
        // When
        String className1 = binder.getMDCAdapterClassStr();
        MDCAdapter adapter1 = binder.getMDCA();
        String className2 = binder.getMDCAdapterClassStr();
        MDCAdapter adapter2 = binder.getMDCA();
        
        // Then
        assertEquals(className1, className2);
        assertSame(adapter1, adapter2);
        assertEquals(MockMDCAdapter.class.getName(), className1);
        assertTrue(adapter1 instanceof MockMDCAdapter);
    }

    @Test
    @DisplayName("Should provide functional MDC adapter")
    void shouldProvideFunctionalMDCAdapter() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        MDCAdapter adapter = binder.getMDCA();
        
        // When
        adapter.put("testKey", "testValue");
        String retrievedValue = adapter.get("testKey");
        
        // Then
        assertEquals("testValue", retrievedValue);
    }

    @Test
    @DisplayName("Should maintain adapter state across binder calls")
    void shouldMaintainAdapterStateAcrossBinnerCalls() {
        // Given
        StaticMDCBinder binder = StaticMDCBinder.SINGLETON;
        
        // When
        MDCAdapter adapter1 = binder.getMDCA();
        adapter1.put("persistentKey", "persistentValue");
        
        MDCAdapter adapter2 = binder.getMDCA();
        String retrievedValue = adapter2.get("persistentKey");
        
        // Then
        assertEquals("persistentValue", retrievedValue);
        assertSame(adapter1, adapter2);
    }
}