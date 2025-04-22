package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReportOperatingSystemTest {

    private TestLogger testLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.os");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogOperatingSystemInformation() {
        // Arrange
        ReportOperatingSystem report = new ReportOperatingSystem(testLogger);

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Operating System"));
        assertTrue(logs.contains("architecture: " + System.getProperty("os.arch")));
        assertTrue(logs.contains("name: " + System.getProperty("os.name")));
        assertTrue(logs.contains("version: " + System.getProperty("os.version")));

        assertTrue(logs.contains("file separator: " + Integer.toHexString(System.getProperty("file.separator").charAt(0))));
        assertTrue(logs.contains("path separator: " + Integer.toHexString(System.getProperty("path.separator").charAt(0))));
        assertTrue(logs.contains("line separator: " + Integer.toHexString(System.getProperty("line.separator").charAt(0))));
    }
}
