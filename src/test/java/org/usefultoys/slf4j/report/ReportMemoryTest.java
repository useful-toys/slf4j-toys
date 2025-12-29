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

package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

/**
 * Unit tests for {@link ReportMemory}.
 * <p>
 * Tests validate that ReportMemory correctly reports JVM memory information,
 * including maximum allowed memory, allocated memory, and currently used memory.
 */
@DisplayName("ReportMemory")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportMemoryTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should log JVM memory information")
    void shouldLogJvmMemoryInformation() {
        // Given: ReportMemory initialized with system runtime memory info
        final ReportMemory report = new ReportMemory(logger);

        // When: report is executed
        report.run();

        // Then: should log memory structure with all memory metrics
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Memory:",
            "maximum allowed:",
            "currently allocated:",
            "currently used:");
    }

    @Test
    @DisplayName("should log custom JVM memory information with controlled values")
    void shouldLogCustomJvmMemoryInformation() {
        // Given: custom MemoryInfoProvider with controlled memory values
        final ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return 1024L * 1024 * 1024; } // 1GB
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        final ReportMemory report = new ReportMemory(logger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log formatted memory values matching the provider values
        final long maxMemory = provider.maxMemory();
        final long totalMemory = provider.totalMemory();
        final long freeMemory = provider.freeMemory();
        final String expectedMaxMemory = (maxMemory == Long.MAX_VALUE)
                ? "no limit"
                : org.usefultoys.slf4j.utils.UnitFormatter.bytes(maxMemory);
        final String expectedTotalMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory);
        final String expectedMoreAvailable = (maxMemory == Long.MAX_VALUE)
                ? "n/a"
                : org.usefultoys.slf4j.utils.UnitFormatter.bytes(maxMemory - totalMemory);
        final String expectedUsedMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory - freeMemory);
        final String expectedFreeMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(freeMemory);
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Memory:",
            "maximum allowed:",
            "currently allocated:",
            "currently used:",
            expectedMaxMemory,
            expectedTotalMemory,
            expectedMoreAvailable,
            expectedUsedMemory,
            expectedFreeMemory);
    }

    @Test
    @DisplayName("should log unlimited max memory with special handling")
    void shouldLogUnlimitedMaxMemoryInformation() {
        // Given: MemoryInfoProvider with unlimited maxMemory (Long.MAX_VALUE)
        final ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return Long.MAX_VALUE; }
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        final ReportMemory report = new ReportMemory(logger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should display "no limit" for max memory and "n/a" for additional available memory
        final long maxMemory = provider.maxMemory();
        final long totalMemory = provider.totalMemory();
        final long freeMemory = provider.freeMemory();
        final String expectedMaxMemory = "no limit";
        final String expectedTotalMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory);
        final String expectedMoreAvailable = "n/a";
        final String expectedUsedMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory - freeMemory);
        final String expectedFreeMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(freeMemory);
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Memory:",
            "maximum allowed:",
            "currently allocated:",
            "currently used:",
            expectedMaxMemory,
            expectedTotalMemory,
            expectedMoreAvailable,
            expectedUsedMemory,
            expectedFreeMemory);
    }
}
