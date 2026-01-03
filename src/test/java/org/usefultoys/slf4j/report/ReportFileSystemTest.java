/*
 * Copyright 2026 Daniel Felix Ferber
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
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportFileSystem}.
 * <p>
 * Tests verify that ReportFileSystem correctly reports file system roots,
 * handles multiple file systems, and formats space information appropriately.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Empty File Systems:</b> Verifies behavior when no file system roots exist, ensuring no logging occurs</li>
 *   <li><b>Single File System:</b> Tests reporting of individual file system roots with total, free, and usable space information</li>
 *   <li><b>Multiple File Systems:</b> Validates handling of multiple file system roots, logging each with appropriate space formatting</li>
 *   <li><b>Zero Space Scenarios:</b> Ensures proper formatting and reporting when file systems have zero total/free/usable space</li>
 *   <li><b>Space Unit Formatting:</b> Tests automatic unit conversion (bytes to MB/GB) for different space magnitudes</li>
 * </ul>
 */
@DisplayName("ReportFileSystem")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportFileSystemTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should report when no file system roots exist")
    void shouldReportWhenNoFileSystemRoots() {
        // Given: File system with no roots
        try (final MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            mockedStatic.when(File::listRoots).thenReturn(new File[0]);

            final ReportFileSystem report = new ReportFileSystem(logger);

            // When: report is executed
            report.run();

            // Then: no file system root information should be logged
            AssertLogger.assertNoEvent(logger, "File system root:");
        }
    }

    @Test
    @DisplayName("should report one file system root")
    void shouldReportOneFileSystemRoot() {
        // Given: File system with one root
        try (final MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            final File mockRoot = mock(File.class);
            when(mockRoot.getAbsolutePath()).thenReturn("/mock_root_a");
            when(mockRoot.getTotalSpace()).thenReturn(1000000000L); // 1 GB
            when(mockRoot.getFreeSpace()).thenReturn(500000000L);   // 0.5 GB
            when(mockRoot.getUsableSpace()).thenReturn(250000000L);  // 0.25 GB

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRoot});

            final ReportFileSystem report = new ReportFileSystem(logger);

            // When: report is executed
            report.run();

            // Then: should log file system root with space information
            AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "File system root: /mock_root_a",
                " - total space: 1000.0MB",
                " - currently free space: 500.0MB (250.0MB usable)");
        }
    }

    @Test
    @DisplayName("should report multiple file system roots")
    void shouldReportMultipleFileSystemRoots() {
        // Given: File system with multiple roots
        try (final MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            final File mockRootA = mock(File.class);
            when(mockRootA.getAbsolutePath()).thenReturn("/mock_root_a");
            when(mockRootA.getTotalSpace()).thenReturn(1000000000L);
            when(mockRootA.getFreeSpace()).thenReturn(500000000L);
            when(mockRootA.getUsableSpace()).thenReturn(250000000L);

            final File mockRootB = mock(File.class);
            when(mockRootB.getAbsolutePath()).thenReturn("/mock_root_b");
            when(mockRootB.getTotalSpace()).thenReturn(2000000000L);
            when(mockRootB.getFreeSpace()).thenReturn(1000000000L);
            when(mockRootB.getUsableSpace()).thenReturn(750000000L);

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRootA, mockRootB});

            final ReportFileSystem report = new ReportFileSystem(logger);

            // When: report is executed
            report.run();

            // Then: should log both file system roots with their space information
            AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "File system root: /mock_root_a",
                " - total space: 1000.0MB",
                " - currently free space: 500.0MB (250.0MB usable)",
                "File system root: /mock_root_b",
                " - total space: 2.0GB",
                " - currently free space: 1000.0MB (750.0MB usable)");
        }
    }

    @Test
    @DisplayName("should report file system root with zero space")
    void shouldReportZeroSpaceFileSystemRoot() {
        // Given: File system root with zero space
        try (final MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            final File mockRoot = mock(File.class);
            when(mockRoot.getAbsolutePath()).thenReturn("/zero_space_root");
            when(mockRoot.getTotalSpace()).thenReturn(0L);
            when(mockRoot.getFreeSpace()).thenReturn(0L);
            when(mockRoot.getUsableSpace()).thenReturn(0L);

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRoot});

            final ReportFileSystem report = new ReportFileSystem(logger);

            // When: report is executed
            report.run();

            // Then: should log file system root with zero space values
            AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "File system root: /zero_space_root",
                " - total space: 0B",
                " - currently free space: 0B (0B usable)");
        }
    }
}
