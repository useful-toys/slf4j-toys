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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.ResetSystemConfig;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SystemConfig}.
 * <p>
 * Tests validate that SystemConfig correctly parses and applies system properties,
 * with proper error handling for invalid values.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Default Values:</b> Verifies that all managed bean flags are false by default and no initialization errors occur</li>
 *   <li><b>Reset Functionality:</b> Ensures that reset() restores all values to defaults without errors</li>
 *   <li><b>Boolean Property Parsing:</b> Tests parsing of useClassLoadingManagedBean, useMemoryManagedBean, useCompilationManagedBean, useGarbageCollectionManagedBean, and usePlatformManagedBean with true, false, and invalid values</li>
 *   <li><b>Error Handling:</b> Validates that invalid boolean values fall back to defaults and report errors via ConfigParser</li>
 * </ul>
 */
@ValidateCharset
@ResetSystemConfig
class SystemConfigTest {

    @Test
    @DisplayName("should have correct default values")
    void shouldReturnCorrectDefaultValues() {
        // Given: SystemConfig not yet initialized
        // When: init() is called
        SystemConfig.init();

        // Then: all managed bean flags should be false and no initialization errors
        assertFalse(SystemConfig.useMemoryManagedBean, "Default value for useMemoryManagedBean should be false");
        assertFalse(SystemConfig.useClassLoadingManagedBean, "Default value for useClassLoadingManagedBean should be false");
        assertFalse(SystemConfig.useCompilationManagedBean, "Default value for useCompilationManagedBean should be false");
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "Default value for useGarbageCollectionManagedBean should be false");
        assertFalse(SystemConfig.usePlatformManagedBean, "Default value for usePlatformManagedBean should be false");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for default values");
    }

    @Test
    @DisplayName("should reset values to defaults")
    void shouldResetValuesToDefaults() {
        // Given: SystemConfig with custom values
        // When: reset() is called
        SystemConfig.reset();

        // Then: all values should return to defaults
        assertFalse(SystemConfig.useMemoryManagedBean, "Default value for useMemoryManagedBean should be false");
        assertFalse(SystemConfig.useClassLoadingManagedBean, "Default value for useClassLoadingManagedBean should be false");
        assertFalse(SystemConfig.useCompilationManagedBean, "Default value for useCompilationManagedBean should be false");
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "Default value for useGarbageCollectionManagedBean should be false");
        assertFalse(SystemConfig.usePlatformManagedBean, "Default value for usePlatformManagedBean should be false");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported after reset");
    }

    @Test
    @DisplayName("should parse useClassLoadingManagedBean as true when system property is true")
    void shouldParseUseClassLoadingManagedBeanWhenTrue() {
        // Given: system property set to "true"
        System.setProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN, "true");
        // When: init() is called
        SystemConfig.init();
        // Then: useClassLoadingManagedBean should be true
        assertTrue(SystemConfig.useClassLoadingManagedBean, "useClassLoadingManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should parse useClassLoadingManagedBean as false when system property is false")
    void shouldParseUseClassLoadingManagedBeanWhenFalse() {
        // Given: system property set to "false"
        System.setProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN, "false");
        // When: init() is called
        SystemConfig.init();
        // Then: useClassLoadingManagedBean should be false
        assertFalse(SystemConfig.useClassLoadingManagedBean, "useClassLoadingManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report error when useClassLoadingManagedBean has invalid format")
    void shouldReportErrorWhenUseClassLoadingManagedBeanInvalid() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN, "invalid");
        // When: init() is called
        SystemConfig.init();
        // Then: should use default value and report error
        assertFalse(SystemConfig.useClassLoadingManagedBean, "useClassLoadingManagedBean should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN));
    }

    @Test
    @DisplayName("should parse useMemoryManagedBean as true when system property is true")
    void shouldParseUseMemoryManagedBeanWhenTrue() {
        // Given: system property set to "true"
        System.setProperty(SystemConfig.PROP_USE_MEMORY_MANAGED_BEAN, "true");
        // When: init() is called
        SystemConfig.init();
        // Then: useMemoryManagedBean should be true
        assertTrue(SystemConfig.useMemoryManagedBean, "useMemoryManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should parse useMemoryManagedBean as false when system property is false")
    void shouldParseUseMemoryManagedBeanWhenFalse() {
        // Given: system property set to "false"
        System.setProperty(SystemConfig.PROP_USE_MEMORY_MANAGED_BEAN, "false");
        // When: init() is called
        SystemConfig.init();
        // Then: useMemoryManagedBean should be false
        assertFalse(SystemConfig.useMemoryManagedBean, "useMemoryManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report error when useMemoryManagedBean has invalid format")
    void shouldReportErrorWhenUseMemoryManagedBeanInvalid() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SystemConfig.PROP_USE_MEMORY_MANAGED_BEAN, "invalid");
        // When: init() is called
        SystemConfig.init();
        // Then: should use default value and report error
        assertFalse(SystemConfig.useMemoryManagedBean, "useMemoryManagedBean should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + SystemConfig.PROP_USE_MEMORY_MANAGED_BEAN));
    }

    @Test
    @DisplayName("should parse useCompilationManagedBean as true when system property is true")
    void shouldParseUseCompilationManagedBeanWhenTrue() {
        // Given: system property set to "true"
        System.setProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN, "true");
        // When: init() is called
        SystemConfig.init();
        // Then: useCompilationManagedBean should be true
        assertTrue(SystemConfig.useCompilationManagedBean, "useCompilationManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should parse useCompilationManagedBean as false when system property is false")
    void shouldParseUseCompilationManagedBeanWhenFalse() {
        // Given: system property set to "false"
        System.setProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN, "false");
        // When: init() is called
        SystemConfig.init();
        // Then: useCompilationManagedBean should be false
        assertFalse(SystemConfig.useCompilationManagedBean, "useCompilationManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report error when useCompilationManagedBean has invalid format")
    void shouldReportErrorWhenUseCompilationManagedBeanInvalid() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN, "invalid");
        // When: init() is called
        SystemConfig.init();
        // Then: should use default value and report error
        assertFalse(SystemConfig.useCompilationManagedBean, "useCompilationManagedBean should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN));
    }

    @Test
    @DisplayName("should parse useGarbageCollectionManagedBean as true when system property is true")
    void shouldParseUseGarbageCollectionManagedBeanWhenTrue() {
        // Given: system property set to "true"
        System.setProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, "true");
        // When: init() is called
        SystemConfig.init();
        // Then: useGarbageCollectionManagedBean should be true
        assertTrue(SystemConfig.useGarbageCollectionManagedBean, "useGarbageCollectionManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should parse useGarbageCollectionManagedBean as false when system property is false")
    void shouldParseUseGarbageCollectionManagedBeanWhenFalse() {
        // Given: system property set to "false"
        System.setProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, "false");
        // When: init() is called
        SystemConfig.init();
        // Then: useGarbageCollectionManagedBean should be false
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "useGarbageCollectionManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report error when useGarbageCollectionManagedBean has invalid format")
    void shouldReportErrorWhenUseGarbageCollectionManagedBeanInvalid() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN, "invalid");
        // When: init() is called
        SystemConfig.init();
        // Then: should use default value and report error
        assertFalse(SystemConfig.useGarbageCollectionManagedBean, "useGarbageCollectionManagedBean should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN));
    }

    @Test
    @DisplayName("should parse usePlatformManagedBean as true when system property is true")
    void shouldParseUsePlatformManagedBeanWhenTrue() {
        // Given: system property set to "true"
        System.setProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN, "true");
        // When: init() is called
        SystemConfig.init();
        // Then: usePlatformManagedBean should be true
        assertTrue(SystemConfig.usePlatformManagedBean, "usePlatformManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should parse usePlatformManagedBean as false when system property is false")
    void shouldParseUsePlatformManagedBeanWhenFalse() {
        // Given: system property set to "false"
        System.setProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN, "false");
        // When: init() is called
        SystemConfig.init();
        // Then: usePlatformManagedBean should be false
        assertFalse(SystemConfig.usePlatformManagedBean, "usePlatformManagedBean should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report error when usePlatformManagedBean has invalid format")
    void shouldReportErrorWhenUsePlatformManagedBeanInvalid() {
        // Given: system property set to invalid value "invalid"
        System.setProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN, "invalid");
        // When: init() is called
        SystemConfig.init();
        // Then: should use default value and report error
        assertFalse(SystemConfig.usePlatformManagedBean, "usePlatformManagedBean should fall back to default for invalid format");
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN));
    }
}
