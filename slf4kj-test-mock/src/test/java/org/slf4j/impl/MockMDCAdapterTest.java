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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MockMDCAdapter}.
 *
 * @author Daniel Felix Ferber
 */
@DisplayName("MockMDCAdapter Tests")
class MockMDCAdapterTest {

    private MockMDCAdapter mdcAdapter;

    @BeforeEach
    void setUp() {
        mdcAdapter = new MockMDCAdapter();
    }

    @Test
    @DisplayName("Should put and get values correctly")
    void shouldPutAndGetValuesCorrectly() {
        // When
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // Then
        assertEquals("value1", mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void shouldReturnNullForNonExistentKey() {
        // When/Then
        assertNull(mdcAdapter.get("nonexistent"));
    }

    @Test
    @DisplayName("Should handle null key and value")
    void shouldHandleNullKeyAndValue() {
        // When
        mdcAdapter.put(null, "value");
        mdcAdapter.put("key", null);
        
        // Then
        assertEquals("value", mdcAdapter.get(null));
        assertNull(mdcAdapter.get("key"));
    }

    @Test
    @DisplayName("Should overwrite existing values")
    void shouldOverwriteExistingValues() {
        // Given
        mdcAdapter.put("key", "oldValue");
        
        // When
        mdcAdapter.put("key", "newValue");
        
        // Then
        assertEquals("newValue", mdcAdapter.get("key"));
    }

    @Test
    @DisplayName("Should remove values correctly")
    void shouldRemoveValuesCorrectly() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // When
        mdcAdapter.remove("key1");
        
        // Then
        assertNull(mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should handle removal of non-existent key gracefully")
    void shouldHandleRemovalOfNonExistentKeyGracefully() {
        // When/Then
        assertDoesNotThrow(() -> mdcAdapter.remove("nonexistent"));
        assertNull(mdcAdapter.get("nonexistent"));
    }

    @Test
    @DisplayName("Should clear all values")
    void shouldClearAllValues() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        mdcAdapter.put("key3", "value3");
        
        // When
        mdcAdapter.clear();
        
        // Then
        assertNull(mdcAdapter.get("key1"));
        assertNull(mdcAdapter.get("key2"));
        assertNull(mdcAdapter.get("key3"));
    }

    @Test
    @DisplayName("Should return copy of context map")
    void shouldReturnCopyOfContextMap() {
        // Given
        mdcAdapter.put("key1", "value1");
        mdcAdapter.put("key2", "value2");
        
        // When
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        
        // Then
        assertNotNull(contextMap);
        assertEquals(2, contextMap.size());
        assertEquals("value1", contextMap.get("key1"));
        assertEquals("value2", contextMap.get("key2"));
        
        // Verify it's a copy (modifications don't affect original)
        contextMap.put("key3", "value3");
        assertNull(mdcAdapter.get("key3"));
    }

    @Test
    @DisplayName("Should return empty map when no context")
    void shouldReturnEmptyMapWhenNoContext() {
        // When
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        
        // Then
        assertNotNull(contextMap);
        assertTrue(contextMap.isEmpty());
    }

    @Test
    @DisplayName("Should set context map correctly")
    void shouldSetContextMapCorrectly() {
        // Given
        Map<String, String> newContext = new HashMap<>();
        newContext.put("newKey1", "newValue1");
        newContext.put("newKey2", "newValue2");
        
        mdcAdapter.put("oldKey", "oldValue");
        
        // When
        mdcAdapter.setContextMap(newContext);
        
        // Then
        assertEquals("newValue1", mdcAdapter.get("newKey1"));
        assertEquals("newValue2", mdcAdapter.get("newKey2"));
        assertNull(mdcAdapter.get("oldKey")); // Old context should be cleared
    }

    @Test
    @DisplayName("Should handle null context map in setContextMap")
    void shouldHandleNullContextMapInSetContextMap() {
        // Given
        mdcAdapter.put("key", "value");
        
        // When/Then
        assertThrows(NullPointerException.class, () -> mdcAdapter.setContextMap(null));
    }

    @Test
    @DisplayName("Should handle empty context map in setContextMap")
    void shouldHandleEmptyContextMapInSetContextMap() {
        // Given
        Map<String, String> emptyContext = new HashMap<>();
        mdcAdapter.put("key", "value");
        
        // When
        mdcAdapter.setContextMap(emptyContext);
        
        // Then
        assertNull(mdcAdapter.get("key"));
        assertTrue(mdcAdapter.getCopyOfContextMap().isEmpty());
    }

    @Test
    @DisplayName("Should isolate context between threads")
    void shouldIsolateContextBetweenThreads() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        String[] thread1Result = new String[1];
        String[] thread2Result = new String[1];
        
        // When
        executor.submit(() -> {
            try {
                mdcAdapter.put("threadKey", "thread1Value");
                Thread.sleep(100); // Allow other thread to potentially interfere
                thread1Result[0] = mdcAdapter.get("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        executor.submit(() -> {
            try {
                mdcAdapter.put("threadKey", "thread2Value");
                Thread.sleep(100); // Allow other thread to potentially interfere
                thread2Result[0] = mdcAdapter.get("threadKey");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
        
        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        
        assertEquals("thread1Value", thread1Result[0]);
        assertEquals("thread2Value", thread2Result[0]);
    }

    @Test
    @DisplayName("Should preserve context map modifications")
    void shouldPreserveContextMapModifications() {
        // Given
        Map<String, String> originalContext = new HashMap<>();
        originalContext.put("key1", "value1");
        originalContext.put("key2", "value2");
        
        mdcAdapter.setContextMap(originalContext);
        
        // When
        originalContext.put("key3", "value3"); // Modify original map
        
        // Then
        assertNull(mdcAdapter.get("key3")); // Should not affect MDC
        assertEquals("value1", mdcAdapter.get("key1"));
        assertEquals("value2", mdcAdapter.get("key2"));
    }

    @Test
    @DisplayName("Should handle large number of key-value pairs")
    void shouldHandleLargeNumberOfKeyValuePairs() {
        // Given
        int numEntries = 1000;
        
        // When
        for (int i = 0; i < numEntries; i++) {
            mdcAdapter.put("key" + i, "value" + i);
        }
        
        // Then
        for (int i = 0; i < numEntries; i++) {
            assertEquals("value" + i, mdcAdapter.get("key" + i));
        }
        
        Map<String, String> contextMap = mdcAdapter.getCopyOfContextMap();
        assertEquals(numEntries, contextMap.size());
    }

    @Test
    @DisplayName("Should handle special characters in keys and values")
    void shouldHandleSpecialCharactersInKeysAndValues() {
        // Given
        String specialKey = "key.with-special_chars@123";
        String specialValue = "value with spaces, symbols !@#$%^&*()";
        
        // When
        mdcAdapter.put(specialKey, specialValue);
        
        // Then
        assertEquals(specialValue, mdcAdapter.get(specialKey));
    }
}