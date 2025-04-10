package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReportCharsetTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.charset");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogCharsetInformation() {
        // Arrange
        ReportCharset report = new ReportCharset(testLogger);
        Charset defaultCharset = Charset.defaultCharset();

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Charset"));
        assertTrue(logs.contains("default charset: " + defaultCharset.displayName()));
        assertTrue(logs.contains("name=" + defaultCharset.name()));
        assertTrue(logs.contains("canEncode=" + defaultCharset.canEncode()));
        assertTrue(logs.contains("available charsets:"));
    }
}
