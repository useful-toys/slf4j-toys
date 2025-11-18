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
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import java.io.File;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.usefultoys.slf4j.SessionConfig;
import java.nio.charset.Charset;
import static org.mockito.Mockito.*;

class ReportFileSystemTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private MockLogger mockLogger;

    @BeforeAll
    public static void setUpLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.filesystem");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void testRunWithNoFileSystemRoots() {
        try (MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            mockedStatic.when(File::listRoots).thenReturn(new File[0]);

            final ReportFileSystem report = new ReportFileSystem(mockLogger);
            report.run();

            // Expect 1 log event because PrintStream is always created and closed,
            // even if nothing is written to it.
            assertEquals(1, mockLogger.getEventCount());
            final String logs = mockLogger.getEvent(0).getFormattedMessage();
            // Assert that the log message does not contain any file system root information
            assertTrue(!logs.contains("File system root:"), "Log should not contain file system root info.");
            assertTrue(logs.trim().isEmpty(), "Log message should be empty or contain only whitespace.");
        }
    }

    @Test
    void testRunWithOneFileSystemRoot() {
        try (MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            File mockRoot = mock(File.class);
            when(mockRoot.getAbsolutePath()).thenReturn("/mock_root_a");
            when(mockRoot.getTotalSpace()).thenReturn(1000000000L); // 1 GB
            when(mockRoot.getFreeSpace()).thenReturn(500000000L);   // 0.5 GB
            when(mockRoot.getUsableSpace()).thenReturn(250000000L);  // 0.25 GB

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRoot});

            final ReportFileSystem report = new ReportFileSystem(mockLogger);
            report.run();
            assertEquals(1, mockLogger.getEventCount());
            final String logs = mockLogger.getEvent(0).getFormattedMessage();
            assertTrue(logs.contains("File system root: /mock_root_a"));
            assertTrue(logs.contains(" - total space: 1000.0MB")); // Corrected assertion
            assertTrue(logs.contains(" - currently free space: 500.0MB (250.0MB usable)")); // Corrected assertion
        }
    }

    @Test
    void testRunWithMultipleFileSystemRoots() {
        try (MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            File mockRootA = mock(File.class);
            when(mockRootA.getAbsolutePath()).thenReturn("/mock_root_a");
            when(mockRootA.getTotalSpace()).thenReturn(1000000000L);
            when(mockRootA.getFreeSpace()).thenReturn(500000000L);
            when(mockRootA.getUsableSpace()).thenReturn(250000000L);

            File mockRootB = mock(File.class);
            when(mockRootB.getAbsolutePath()).thenReturn("/mock_root_b");
            when(mockRootB.getTotalSpace()).thenReturn(2000000000L);
            when(mockRootB.getFreeSpace()).thenReturn(1000000000L);
            when(mockRootB.getUsableSpace()).thenReturn(750000000L);

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRootA, mockRootB});

            final ReportFileSystem report = new ReportFileSystem(mockLogger);
            report.run();

            assertEquals(1, mockLogger.getEventCount());

            final String logs0 = mockLogger.getEvent(0).getFormattedMessage();
            assertTrue(logs0.contains("File system root: /mock_root_a"));
            assertTrue(logs0.contains(" - total space: 1000.0MB")); // Corrected assertion
            assertTrue(logs0.contains(" - currently free space: 500.0MB (250.0MB usable)")); // Corrected assertion

            final String logs1 = logs0;
            assertTrue(logs1.contains("File system root: /mock_root_b"));
            assertTrue(logs1.contains(" - total space: 2.0GB")); // Corrected assertion
            assertTrue(logs1.contains(" - currently free space: 1000.0MB (750.0MB usable)")); // Corrected assertion
        }
    }

    @Test
    void testRunWithZeroSpaceFileSystemRoot() {
        try (MockedStatic<File> mockedStatic = mockStatic(File.class)) {
            File mockRoot = mock(File.class);
            when(mockRoot.getAbsolutePath()).thenReturn("/zero_space_root");
            when(mockRoot.getTotalSpace()).thenReturn(0L);
            when(mockRoot.getFreeSpace()).thenReturn(0L);
            when(mockRoot.getUsableSpace()).thenReturn(0L);

            mockedStatic.when(File::listRoots).thenReturn(new File[]{mockRoot});

            final ReportFileSystem report = new ReportFileSystem(mockLogger);
            report.run();

            assertEquals(1, mockLogger.getEventCount());
            final String logs = mockLogger.getEvent(0).getFormattedMessage();
            assertTrue(logs.contains("File system root: /zero_space_root"));
            assertTrue(logs.contains(" - total space: 0B")); // Corrected assertion
            assertTrue(logs.contains(" - currently free space: 0B (0B usable)")); // Corrected assertion
        }
    }
}
