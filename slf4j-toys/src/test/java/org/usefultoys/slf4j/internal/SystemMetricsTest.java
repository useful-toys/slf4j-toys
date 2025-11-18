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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit test for {@link SystemMetrics}.
 * This test formalizes the singleton behavior of the class.
 *
 * @author Daniel Felix Ferber
 */
class SystemMetricsTest {
    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @Test
    @DisplayName("Should provide a non-null singleton instance")
    void getInstance_providesSingleton() {
        // Act
        final SystemMetricsCollector instance1 = SystemMetrics.getInstance();
        final SystemMetricsCollector instance2 = SystemMetrics.getInstance();

        // Assert
        // 1. Ensure the instance is not null.
        assertNotNull(instance1, "The first call to getInstance() should not return null.");

        // 2. Ensure that subsequent calls return the exact same instance.
        assertSame(instance1, instance2, "Subsequent calls to getInstance() should return the same instance.");
    }
}
