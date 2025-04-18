package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class ReportCalendarTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.calendar");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogCalendarInformation() {
        // Arrange
        ReportCalendar report = new ReportCalendar(testLogger);
        TimeZone tz = TimeZone.getDefault();

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Calendar"));
        assertTrue(logs.contains("current date/time"));
        assertTrue(logs.contains("default timezone: " + tz.getDisplayName()));
        assertTrue(logs.contains("(" + tz.getID() + ")"));
        assertTrue(logs.contains("available IDs:"));
    }
}
