package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import static org.junit.jupiter.api.Assertions.*;

class ReportMemoryTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.memory");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogJvmMemoryInformation() {
        // Arrange
        ReportMemory report = new ReportMemory(testLogger);

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Memory:"));
        assertTrue(logs.contains("maximum allowed:"));
        assertTrue(logs.contains("currently allocated:"));
        assertTrue(logs.contains("currently used:"));
    }
}
