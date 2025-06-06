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

package org.usefultoys.slf4j;

import org.junit.jupiter.api.*;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        // Reinitialize SystemConfig to ensure clean state for each test
        SystemConfig.reset();

    }

    @AfterAll
    static void tearDown() {
        // Reinitialize SystemConfig to ensure clean state for further tests
        SystemConfig.reset();
    }

    @Test
    void testDefaultValues() {
        SystemConfig.init();

        assertFalse(SystemConfig.useMemoryManagedBean, "Default value for useMemoryManagedBean should be false");
        assertFalse(SystemConfig.useClassLoadingManagedBean, "Default value for useClassLoadingManagedBean should be false");
        assertFalse(SystemConfig.useCompilationManagedBean, "Default value for useCompilationManagedBean should be false");
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "Default value for useGarbageCollectionManagedBean should be false");
        assertFalse(SystemConfig.usePlatformManagedBean, "Default value for usePlatformManagedBean should be false");
    }

    @Test
    void testResetValues() {
        SystemConfig.reset();

        assertFalse(SystemConfig.useMemoryManagedBean, "Default value for useMemoryManagedBean should be false");
        assertFalse(SystemConfig.useClassLoadingManagedBean, "Default value for useClassLoadingManagedBean should be false");
        assertFalse(SystemConfig.useCompilationManagedBean, "Default value for useCompilationManagedBean should be false");
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "Default value for useGarbageCollectionManagedBean should be false");
        assertFalse(SystemConfig.usePlatformManagedBean, "Default value for usePlatformManagedBean should be false");
    }

    @Test
    void testUseClassLoadingManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.useClassLoadingManagedBean, "useClassLoadingManagedBean should reflect the system property value");
    }

    @Test
    void testUseMemoryManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_MEMORY_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.useMemoryManagedBean, "useMemoryManagedBean should reflect the system property value");
    }

    @Test
    void testUseCompilationManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.useCompilationManagedBean, "useCompilationManagedBean should reflect the system property value");
    }

    @Test
    void testUseGarbageCollectionManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.useGarbageCollectionManagedBean, "useGarbageCollectionManagedBean should reflect the system property value");
    }

    @Test
    void testUsePlatformManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.usePlatformManagedBean, "usePlatformManagedBean should reflect the system property value");
    }
}
