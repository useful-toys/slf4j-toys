/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportCalendarTest {

    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.calendar");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogDefaultCalendarInformation() {
        // Arrange
        final Date fixedCurrentDate = new Date(1678886400000L); // Use a fixed date for determinism (March 15, 2023 00:00:00 GMT)
        final TimeZone defaultTimeZone = TimeZone.getDefault(); // Still use actual default timezone

        ReportCalendar.CalendarInfoProvider provider = new ReportCalendar.CalendarInfoProvider() {
            @Override
            public Date getCurrentDate() {
                return fixedCurrentDate;
            }

            @Override
            public TimeZone getDefaultTimeZone() {
                return TimeZone.getDefault(); // Use actual default timezone
            }

            @Override
            public String[] getAvailableTimeZoneIDs() {
                return TimeZone.getAvailableIDs(); // Use actual available IDs
            }
        };

        ReportCalendar report = new ReportCalendar(mockLogger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Calendar"), "Should contain 'Calendar'");

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(defaultTimeZone); // Ensure formatter uses the actual default timezone for comparison
        String expectedDateString = df.format(fixedCurrentDate);
        assertTrue(logs.contains("current date/time: " + expectedDateString), "Should contain current date/time");

        assertTrue(logs.contains("default timezone: " + defaultTimeZone.getDisplayName()), "Should contain default timezone display name");
        assertTrue(logs.contains(" (" + defaultTimeZone.getID() + ")"), "Should contain default timezone ID");
        assertTrue(logs.contains("DST=" + (defaultTimeZone.getDSTSavings() / 60000) + "min"), "Should contain DST savings");
        assertTrue(logs.contains("useDST=" + defaultTimeZone.useDaylightTime()), "Should contain useDST status");
        assertTrue(logs.contains("inDST=" + defaultTimeZone.inDaylightTime(fixedCurrentDate)), "Should contain inDST status");
        assertTrue(logs.contains("offset=" + (defaultTimeZone.getRawOffset() / 60000) + "min"), "Should contain raw offset");
        assertTrue(logs.contains("available IDs:"), "Should contain 'available IDs:'");
        // We can't assert all available charsets as they vary by JVM, but we can check for a few common ones
        assertTrue(logs.contains("America/Sao_Paulo;"), "Should contain a common timezone ID");
        assertTrue(logs.contains("UTC;"), "Should contain UTC timezone ID");
    }

    @Test
    void shouldLogCustomCalendarInformation() {
        // Arrange: create a CalendarInfoProvider with controlled values
        final Date customDate = new Date(1678886400000L); // March 15, 2023 00:00:00 GMT
        final TimeZone customTimeZone = TimeZone.getTimeZone("Europe/Berlin"); // Berlin timezone
        final String[] customAvailableIDs = {"Europe/Berlin", "America/New_York", "Asia/Tokyo"};

        ReportCalendar.CalendarInfoProvider provider = new ReportCalendar.CalendarInfoProvider() {
            @Override
            public Date getCurrentDate() {
                return customDate;
            }

            @Override
            public TimeZone getDefaultTimeZone() {
                return customTimeZone;
            }

            @Override
            public String[] getAvailableTimeZoneIDs() {
                return customAvailableIDs;
            }
        };

        ReportCalendar report = new ReportCalendar(mockLogger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert
        String logs = mockLogger.toText();
        assertTrue(logs.contains("Calendar"), "Should contain 'Calendar'");
        
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(customTimeZone); // Ensure formatter uses the custom timezone for comparison
        String expectedDateString = df.format(customDate); // Get the full formatted string
        assertTrue(logs.contains("current date/time: " + expectedDateString), "Should contain custom date/time");

        assertTrue(logs.contains("default timezone: " + customTimeZone.getDisplayName()), "Should contain custom timezone display name");
        assertTrue(logs.contains(" (" + customTimeZone.getID() + ")"), "Should contain custom timezone ID");
        assertTrue(logs.contains("DST=" + (customTimeZone.getDSTSavings() / 60000) + "min"), "Should contain custom DST savings");
        assertTrue(logs.contains("useDST=" + customTimeZone.useDaylightTime()), "Should contain custom useDST status");
        assertTrue(logs.contains("inDST=" + customTimeZone.inDaylightTime(customDate)), "Should contain custom inDST status");
        assertTrue(logs.contains("offset=" + (customTimeZone.getRawOffset() / 60000) + "min"), "Should contain custom raw offset");
        assertTrue(logs.contains("available IDs:"), "Should contain 'available IDs:'");
        assertTrue(logs.contains("Europe/Berlin;"), "Should contain custom available ID: Europe/Berlin");
        assertTrue(logs.contains("America/New_York;"), "Should contain custom available ID: America/New_York");
        assertTrue(logs.contains("Asia/Tokyo;"), "Should contain custom available ID: Asia/Tokyo");
        assertTrue(!logs.contains("America/Sao_Paulo;"), "Should NOT contain default ID: America/Sao_Paulo");
    }
}
