package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import static org.junit.jupiter.api.Assertions.*;

class ReportPhysicalSystemTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.physical");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogPhysicalSystemInformation() {
        // Arrange
        ReportPhysicalSystem report = new ReportPhysicalSystem(testLogger);
        int expectedProcessors = Runtime.getRuntime().availableProcessors();

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Physical system"));
        assertTrue(logs.contains("processors: " + expectedProcessors));
    }
}
