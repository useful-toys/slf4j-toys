package org.usefultoys.slf4j.report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ReportFileSystemTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.fs");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogFileSystemInformation() {
        // Arrange
        ReportFileSystem report = new ReportFileSystem(testLogger);
        File[] roots = File.listRoots();
        assertTrue(roots.length > 0, "Expected at least one file system root");

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("File system root: " + roots[0].getAbsolutePath()));
        assertTrue(logs.contains("total space:"));
        assertTrue(logs.contains("currently free space:"));
    }
}
