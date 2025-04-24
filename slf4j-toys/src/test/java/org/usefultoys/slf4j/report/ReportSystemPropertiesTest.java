package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReportSystemPropertiesTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.sysprops");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogSystemProperties() {
        // Arrange
        ReportSystemProperties report = new ReportSystemProperties(mockLogger);

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("System Properties:"));
        assertTrue(logs.contains("java.version")); // valor pode variar, mas chave é conhecida
        assertTrue(logs.contains(System.getProperty("java.version"))); // valor conhecido no ambiente atual
    }
}