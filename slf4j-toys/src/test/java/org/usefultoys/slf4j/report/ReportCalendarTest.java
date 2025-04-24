package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class ReportCalendarTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.calendar");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogCalendarInformation() {
        // Arrange
        ReportCalendar report = new ReportCalendar(mockLogger);
        TimeZone tz = TimeZone.getDefault();

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Calendar"));
        assertTrue(logs.contains("current date/time"));
        assertTrue(logs.contains("default timezone: " + tz.getDisplayName()));
        assertTrue(logs.contains("(" + tz.getID() + ")"));
        assertTrue(logs.contains("available IDs:"));
    }
}
