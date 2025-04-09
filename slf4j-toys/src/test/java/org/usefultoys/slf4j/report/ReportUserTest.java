package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import static org.junit.jupiter.api.Assertions.*;

class ReportUserTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.user");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogUserInformation() {
        // Arrange
        ReportUser report = new ReportUser(testLogger);

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("User:"));
        assertTrue(logs.contains("name: " + System.getProperty("user.name")));
        assertTrue(logs.contains("home: " + System.getProperty("user.home")));
    }
}