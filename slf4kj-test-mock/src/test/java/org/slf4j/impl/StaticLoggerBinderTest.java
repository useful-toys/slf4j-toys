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
import org.slf4j.ILoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StaticLoggerBinder}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("StaticLoggerBinder Tests")
class StaticLoggerBinderTest {

    @Test
    @DisplayName("Should return singleton instance")
    void shouldReturnSingletonInstance() {
        // When
        StaticLoggerBinder instance1 = StaticLoggerBinder.getSingleton();
        StaticLoggerBinder instance2 = StaticLoggerBinder.getSingleton();
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should return MockLoggerFactory instance")
    void shouldReturnMockLoggerFactoryInstance() {
        // Given
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        
        // When
        ILoggerFactory loggerFactory = binder.getLoggerFactory();
        
        // Then
        assertNotNull(loggerFactory);
        assertTrue(loggerFactory instanceof MockLoggerFactory);
    }

    @Test
    @DisplayName("Should return same logger factory instance on multiple calls")
    void shouldReturnSameLoggerFactoryInstanceOnMultipleCalls() {
        // Given
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        
        // When
        ILoggerFactory factory1 = binder.getLoggerFactory();
        ILoggerFactory factory2 = binder.getLoggerFactory();
        
        // Then
        assertNotNull(factory1);
        assertNotNull(factory2);
        assertSame(factory1, factory2);
    }

    @Test
    @DisplayName("Should return correct logger factory class name")
    void shouldReturnCorrectLoggerFactoryClassName() {
        // Given
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        
        // When
        String className = binder.getLoggerFactoryClassStr();
        
        // Then
        assertEquals(MockLoggerFactory.class.getName(), className);
        assertEquals("org.slf4j.impl.MockLoggerFactory", className);
    }

    @Test
    @DisplayName("Should have correct requested API version")
    void shouldHaveCorrectRequestedApiVersion() {
        // When/Then
        assertEquals("1.6", StaticLoggerBinder.REQUESTED_API_VERSION);
    }

    @Test
    @DisplayName("Should maintain singleton pattern across different access methods")
    void shouldMaintainSingletonPatternAcrossDifferentAccessMethods() {
        // When
        StaticLoggerBinder instance1 = StaticLoggerBinder.getSingleton();
        StaticLoggerBinder instance2 = StaticLoggerBinder.getSingleton();
        
        ILoggerFactory factory1 = instance1.getLoggerFactory();
        ILoggerFactory factory2 = instance2.getLoggerFactory();
        
        // Then
        assertSame(instance1, instance2);
        assertSame(factory1, factory2);
        assertTrue(factory1 instanceof MockLoggerFactory);
        assertTrue(factory2 instanceof MockLoggerFactory);
    }

    @Test
    @DisplayName("Should be thread-safe singleton")
    void shouldBeThreadSafeSingleton() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        StaticLoggerBinder[] instances = new StaticLoggerBinder[numberOfThreads];
        Thread[] threads = new Thread[numberOfThreads];
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = StaticLoggerBinder.getSingleton();
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        StaticLoggerBinder firstInstance = instances[0];
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
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        
        // When
        String className1 = binder.getLoggerFactoryClassStr();
        ILoggerFactory factory1 = binder.getLoggerFactory();
        String className2 = binder.getLoggerFactoryClassStr();
        ILoggerFactory factory2 = binder.getLoggerFactory();
        
        // Then
        assertEquals(className1, className2);
        assertSame(factory1, factory2);
        assertEquals(MockLoggerFactory.class.getName(), className1);
        assertTrue(factory1 instanceof MockLoggerFactory);
    }
}