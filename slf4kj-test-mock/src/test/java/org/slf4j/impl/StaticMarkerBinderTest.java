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
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StaticMarkerBinder}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("StaticMarkerBinder Tests")
class StaticMarkerBinderTest {

    @Test
    @DisplayName("Should return singleton instance")
    void shouldReturnSingletonInstance() {
        // When
        StaticMarkerBinder instance1 = StaticMarkerBinder.SINGLETON;
        StaticMarkerBinder instance2 = StaticMarkerBinder.SINGLETON;
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should return BasicMarkerFactory instance")
    void shouldReturnBasicMarkerFactoryInstance() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        
        // When
        IMarkerFactory markerFactory = binder.getMarkerFactory();
        
        // Then
        assertNotNull(markerFactory);
        assertTrue(markerFactory instanceof BasicMarkerFactory);
    }

    @Test
    @DisplayName("Should return same marker factory instance on multiple calls")
    void shouldReturnSameMarkerFactoryInstanceOnMultipleCalls() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        
        // When
        IMarkerFactory factory1 = binder.getMarkerFactory();
        IMarkerFactory factory2 = binder.getMarkerFactory();
        
        // Then
        assertNotNull(factory1);
        assertNotNull(factory2);
        assertSame(factory1, factory2);
    }

    @Test
    @DisplayName("Should return correct marker factory class name")
    void shouldReturnCorrectMarkerFactoryClassName() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        
        // When
        String className = binder.getMarkerFactoryClassStr();
        
        // Then
        assertEquals(BasicMarkerFactory.class.getName(), className);
        assertEquals("org.slf4j.helpers.BasicMarkerFactory", className);
    }

    @Test
    @DisplayName("Should maintain singleton pattern across different access methods")
    void shouldMaintainSingletonPatternAcrossDifferentAccessMethods() {
        // When
        StaticMarkerBinder instance1 = StaticMarkerBinder.SINGLETON;
        StaticMarkerBinder instance2 = StaticMarkerBinder.SINGLETON;
        
        IMarkerFactory factory1 = instance1.getMarkerFactory();
        IMarkerFactory factory2 = instance2.getMarkerFactory();
        
        // Then
        assertSame(instance1, instance2);
        assertSame(factory1, factory2);
        assertTrue(factory1 instanceof BasicMarkerFactory);
        assertTrue(factory2 instanceof BasicMarkerFactory);
    }

    @Test
    @DisplayName("Should be thread-safe singleton")
    void shouldBeThreadSafeSingleton() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        StaticMarkerBinder[] instances = new StaticMarkerBinder[numberOfThreads];
        Thread[] threads = new Thread[numberOfThreads];
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = StaticMarkerBinder.SINGLETON;
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        StaticMarkerBinder firstInstance = instances[0];
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
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        
        // When
        String className1 = binder.getMarkerFactoryClassStr();
        IMarkerFactory factory1 = binder.getMarkerFactory();
        String className2 = binder.getMarkerFactoryClassStr();
        IMarkerFactory factory2 = binder.getMarkerFactory();
        
        // Then
        assertEquals(className1, className2);
        assertSame(factory1, factory2);
        assertEquals(BasicMarkerFactory.class.getName(), className1);
        assertTrue(factory1 instanceof BasicMarkerFactory);
    }

    @Test
    @DisplayName("Should provide functional marker factory")
    void shouldProvideFunctionalMarkerFactory() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        IMarkerFactory factory = binder.getMarkerFactory();
        
        // When
        Marker marker1 = factory.getMarker("TEST_MARKER");
        Marker marker2 = factory.getMarker("ANOTHER_MARKER");
        
        // Then
        assertNotNull(marker1);
        assertNotNull(marker2);
        assertEquals("TEST_MARKER", marker1.getName());
        assertEquals("ANOTHER_MARKER", marker2.getName());
        assertNotSame(marker1, marker2);
    }

    @Test
    @DisplayName("Should maintain marker factory state across binder calls")
    void shouldMaintainMarkerFactoryStateAcrossBinderCalls() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        
        // When
        IMarkerFactory factory1 = binder.getMarkerFactory();
        Marker marker1 = factory1.getMarker("PERSISTENT_MARKER");
        
        IMarkerFactory factory2 = binder.getMarkerFactory();
        Marker marker2 = factory2.getMarker("PERSISTENT_MARKER");
        
        // Then
        assertSame(factory1, factory2);
        assertSame(marker1, marker2); // Same marker instance should be returned for same name
    }

    @Test
    @DisplayName("Should support marker hierarchy operations")
    void shouldSupportMarkerHierarchyOperations() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        IMarkerFactory factory = binder.getMarkerFactory();
        
        // When
        Marker parent = factory.getMarker("PARENT_MARKER");
        Marker child = factory.getMarker("CHILD_MARKER");
        child.add(parent);
        
        // Then
        assertTrue(child.contains(parent));
        assertFalse(parent.contains(child));
    }

    @Test
    @DisplayName("Should handle detached markers correctly")
    void shouldHandleDetachedMarkersCorrectly() {
        // Given
        StaticMarkerBinder binder = StaticMarkerBinder.SINGLETON;
        IMarkerFactory factory = binder.getMarkerFactory();
        
        // When
        Marker marker = factory.getDetachedMarker("DETACHED_MARKER");
        
        // Then
        assertNotNull(marker);
        assertEquals("DETACHED_MARKER", marker.getName());
        
        // Detached marker should not be the same as regular marker with same name
        Marker regularMarker = factory.getMarker("DETACHED_MARKER");
        assertNotSame(marker, regularMarker);
    }
}