package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportVMTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        mockLogger = (MockLogger) LoggerFactory.getLogger("test.report.vm");
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogJvmInformation() {
        // Arrange
        ReportVM report = new ReportVM(mockLogger);

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Java Virtual Machine"));
        assertTrue(logs.contains("vendor: " + System.getProperty("java.vendor")));
        assertTrue(logs.contains("version: " + System.getProperty("java.version")));
        assertTrue(logs.contains("installation directory: " + System.getProperty("java.home")));
    }
}


