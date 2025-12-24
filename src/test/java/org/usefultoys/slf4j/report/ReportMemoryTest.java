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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

@ExtendWith({ResetReporterConfig.class, CharsetConsistency.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportMemoryTest {

    @Slf4jMock("test.report.memory")
    private Logger logger;

    @Test
    void shouldLogJvmMemoryInformation() {
        // Arrange
        final ReportMemory report = new ReportMemory(logger);
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
    void shouldLogCustomJvmMemoryInformation() {
        // Arrange: create a MemoryInfoProvider with controlled values
        ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return 1024L * 1024 * 1024; } // 1GB
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        ReportMemory report = new ReportMemory(logger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };
        // Act
        report.run();
        // Assert
        // Use AssertLogger to validate the INFO report contents
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
    void shouldLogUnlimitedMaxMemoryInformation() {
        // Arrange: create a MemoryInfoProvider with unlimited maxMemory
        ReportMemory.MemoryInfoProvider provider = new ReportMemory.MemoryInfoProvider() {
            @Override public long maxMemory() { return Long.MAX_VALUE; }
            @Override public long totalMemory() { return 1024L * 1024 * 512; } // 512MB
            @Override public long freeMemory() { return 1024L * 1024 * 128; } // 128MB
        };
        ReportMemory report = new ReportMemory(logger) {
            @Override
            protected MemoryInfoProvider getMemoryInfoProvider() {
                return provider;
            }
        };
        // Act
        report.run();
        // Assert
        // Use AssertLogger to validate the INFO report contents
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
