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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportCalendarTest {

    @Slf4jMock("test.report.calendar")
    private Logger logger;

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

        ReportCalendar report = new ReportCalendar(logger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(defaultTimeZone); // Ensure formatter uses the actual default timezone for comparison
        String expectedDateString = df.format(fixedCurrentDate);
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Calendar",
            " - current date/time: " + expectedDateString,
            " - default timezone: " + defaultTimeZone.getDisplayName() + " (" + defaultTimeZone.getID() + ")",
            "; DST=" + (defaultTimeZone.getDSTSavings() / 60000) + "min",
            "; observesDST=" + defaultTimeZone.observesDaylightTime(),
            "; useDST=" + defaultTimeZone.useDaylightTime(),
            "; inDST=" + defaultTimeZone.inDaylightTime(fixedCurrentDate),
            "; offset=" + (defaultTimeZone.getRawOffset() / 60000) + "min",
            " - available IDs:",
            "America/Sao_Paulo; ",
            "UTC; ");
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

        ReportCalendar report = new ReportCalendar(logger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(customTimeZone); // Ensure formatter uses the custom timezone for comparison
        String expectedDateString = df.format(customDate); // Get the full formatted string
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Calendar",
            " - current date/time: " + expectedDateString,
            " - default timezone: " + customTimeZone.getDisplayName() + " (" + customTimeZone.getID() + ")",
            "; DST=" + (customTimeZone.getDSTSavings() / 60000) + "min",
            "; observesDST=" + customTimeZone.observesDaylightTime(),
            "; useDST=" + customTimeZone.useDaylightTime(),
            "; inDST=" + customTimeZone.inDaylightTime(customDate),
            "; offset=" + (customTimeZone.getRawOffset() / 60000) + "min",
            " - available IDs:",
            "Europe/Berlin; ",
            "America/New_York; ",
            "Asia/Tokyo; ");
        AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, "America/Sao_Paulo; ");
    }
}
