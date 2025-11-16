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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportMemoryTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.memory");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogJvmMemoryInformation() {
        // Arrange
        final ReportMemory report = new ReportMemory(mockLogger);
        final Runtime runtime = Runtime.getRuntime();
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final String expectedMaxMemory = (maxMemory == Long.MAX_VALUE)
                ? "no limit"
                : org.usefultoys.slf4j.utils.UnitFormatter.bytes(maxMemory);
        final String expectedTotalMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory);
        final String expectedMoreAvailable = (maxMemory == Long.MAX_VALUE)
                ? "n/a"
                : org.usefultoys.slf4j.utils.UnitFormatter.bytes(maxMemory - totalMemory);
        final String expectedUsedMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory - freeMemory);
        final String expectedFreeMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(freeMemory);

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Memory:"), "Should contain 'Memory:'");
        assertTrue(logs.contains("maximum allowed:"), "Should contain 'maximum allowed:'");
        assertTrue(logs.contains("currently allocated:"), "Should contain 'currently allocated:'");
        assertTrue(logs.contains("currently used:"), "Should contain 'currently used:'");
        assertTrue(logs.contains(expectedMaxMemory), "Should contain '" + expectedMaxMemory + "'");
        assertTrue(logs.contains(expectedTotalMemory), "Should contain '" + expectedTotalMemory + "'");
        assertTrue(logs.contains(expectedMoreAvailable), "Should contain '" + expectedMoreAvailable + "'");
        assertTrue(logs.contains(expectedUsedMemory), "Should contain '" + expectedUsedMemory + "'");
        assertTrue(logs.contains(expectedFreeMemory), "Should contain '" + expectedFreeMemory + "'");
    }

    @Test
    void shouldLogCustomJvmMemoryInformation() {
        // Arrange: create a MemoryInfoProvider with controlled values
        ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return 1024L * 1024 * 1024; } // 1GB
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        ReportMemory report = new ReportMemory(mockLogger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };
        // Act
        report.run();
        // Assert
        String logs = mockLogger.toText();
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
        assertTrue(logs.contains("Memory:"), "Should contain 'Memory:'");
        assertTrue(logs.contains("maximum allowed:"), "Should contain 'maximum allowed:'");
        assertTrue(logs.contains("currently allocated:"), "Should contain 'currently allocated:'");
        assertTrue(logs.contains("currently used:"), "Should contain 'currently used:'");
        assertTrue(logs.contains(expectedMaxMemory), "Should contain '" + expectedMaxMemory + "'");
        assertTrue(logs.contains(expectedTotalMemory), "Should contain '" + expectedTotalMemory + "'");
        assertTrue(logs.contains(expectedMoreAvailable), "Should contain '" + expectedMoreAvailable + "'");
        assertTrue(logs.contains(expectedUsedMemory), "Should contain '" + expectedUsedMemory + "'");
        assertTrue(logs.contains(expectedFreeMemory), "Should contain '" + expectedFreeMemory + "'");
    }

    @Test
    void shouldLogUnlimitedMaxMemoryInformation() {
        // Arrange: create a MemoryInfoProvider with unlimited maxMemory
        ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return Long.MAX_VALUE; }
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        ReportMemory report = new ReportMemory(mockLogger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };
        // Act
        report.run();
        // Assert
        String logs = mockLogger.toText();
        final long maxMemory = provider.maxMemory();
        final long totalMemory = provider.totalMemory();
        final long freeMemory = provider.freeMemory();
        final String expectedMaxMemory = "no limit";
        final String expectedTotalMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory);
        final String expectedMoreAvailable = "n/a";
        final String expectedUsedMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(totalMemory - freeMemory);
        final String expectedFreeMemory = org.usefultoys.slf4j.utils.UnitFormatter.bytes(freeMemory);
        assertTrue(logs.contains("Memory:"), "Should contain 'Memory:'");
        assertTrue(logs.contains("maximum allowed:"), "Should contain 'maximum allowed:'");
        assertTrue(logs.contains("currently allocated:"), "Should contain 'currently allocated:'");
        assertTrue(logs.contains("currently used:"), "Should contain 'currently used:'");
        assertTrue(logs.contains(expectedMaxMemory), "Should contain '" + expectedMaxMemory + "'");
        assertTrue(logs.contains(expectedTotalMemory), "Should contain '" + expectedTotalMemory + "'");
        assertTrue(logs.contains(expectedMoreAvailable), "Should contain '" + expectedMoreAvailable + "'");
        assertTrue(logs.contains(expectedUsedMemory), "Should contain '" + expectedUsedMemory + "'");
        assertTrue(logs.contains(expectedFreeMemory), "Should contain '" + expectedFreeMemory + "'");
    }
}
