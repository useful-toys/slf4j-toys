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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReportFileSystemTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.fs");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogFileSystemInformation() {
        // Arrange
        final ReportFileSystem report = new ReportFileSystem(mockLogger);
        final File[] roots = File.listRoots();
        assertTrue(roots.length > 0, "Expected at least one file system root");

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("File system root: " + roots[0].getAbsolutePath()));
        assertTrue(logs.contains("total space:"));
        assertTrue(logs.contains("currently free space:"));
    }
}
