package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportVMTest {

    private TestLogger testLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        testLogger = (TestLogger) LoggerFactory.getLogger("test.report.vm");
        testLogger.clearEvents();
    }

    @Test
    void shouldLogJvmInformation() {
        // Arrange
        ReportVM report = new ReportVM(testLogger);

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Java Virtual Machine"));
        assertTrue(logs.contains("vendor: " + System.getProperty("java.vendor")));
        assertTrue(logs.contains("version: " + System.getProperty("java.version")));
        assertTrue(logs.contains("installation directory: " + System.getProperty("java.home")));
    }
}


