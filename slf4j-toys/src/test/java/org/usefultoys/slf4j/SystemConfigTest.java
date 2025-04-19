package org.usefultoys.slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigTest {

    @BeforeEach
    void setUp() {
        // Reinitialize SystemConfig to ensure clean state for each test
        System.clearProperty(SystemConfig.SLF_4_JTOYS_USE_MEMORY_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN);

    }

    @AfterEach
    void tearDown() {
        System.clearProperty(SystemConfig.SLF_4_JTOYS_USE_MEMORY_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_COMPILATION_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_GARBAGE_COLLECTION_MANAGED_BEAN);
        System.clearProperty(SystemConfig.PROP_USE_PLATFORM_MANAGED_BEAN);
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
    void testHasSunOperatingSystemMXBean() {
        SystemConfig.init();

        // This value is determined at runtime and cannot be changed
        assertNotNull(SystemConfig.hasSunOperatingSystemMXBean, "hasSunOperatingSystemMXBean should not be null");
    }

    @Test
    void testUseClassLoadingManagedBean() {
        System.setProperty(SystemConfig.PROP_USE_CLASS_LOADING_MANAGED_BEAN, "true");
        SystemConfig.init(); // Reinitialize to apply new system properties
        assertTrue(SystemConfig.useClassLoadingManagedBean, "useClassLoadingManagedBean should reflect the system property value");
    }

    @Test
    void testUseMemoryManagedBean() {
        System.setProperty(SystemConfig.SLF_4_JTOYS_USE_MEMORY_MANAGED_BEAN, "true");
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
