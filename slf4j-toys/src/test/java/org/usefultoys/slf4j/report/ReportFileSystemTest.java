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
