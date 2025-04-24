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

class ReportCharsetTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.charset");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogCharsetInformation() {
        // Arrange
        ReportCharset report = new ReportCharset(mockLogger);
        Charset defaultCharset = Charset.defaultCharset();

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Charset"));
        assertTrue(logs.contains("default charset: " + defaultCharset.displayName()));
        assertTrue(logs.contains("name=" + defaultCharset.name()));
        assertTrue(logs.contains("canEncode=" + defaultCharset.canEncode()));
        assertTrue(logs.contains("available charsets:"));
    }
}
