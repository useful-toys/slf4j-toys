package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link SystemMetrics}.
 * <p>
 * Tests verify that SystemMetrics correctly provides a singleton instance
 * of SystemMetricsCollector, ensuring consistent behavior across calls.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Singleton Pattern:</b> Verifies that getInstance() returns a non-null SystemMetricsCollector and the same instance on multiple calls</li>
 * </ul>
 */
@DisplayName("SystemMetrics")
@ValidateCharset
class SystemMetricsTest {

    @Test
    @DisplayName("should provide a non-null singleton instance")
    void getInstance_providesSingleton() {
        // Given: no setup required - SystemMetrics is a singleton provider
        // When: getInstance is called multiple times
        final SystemMetricsCollector instance1 = SystemMetrics.getInstance();
        final SystemMetricsCollector instance2 = SystemMetrics.getInstance();

        // Then: both calls should return the same non-null instance
        assertNotNull(instance1, "The first call to getInstance() should not return null");
        assertSame(instance1, instance2, "Subsequent calls to getInstance() should return the same instance");
    }
}
