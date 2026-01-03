/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Unit tests for {@link ReportCalendar}.
 * <p>
 * Tests verify that ReportCalendar correctly formats and logs calendar information
 * including current date/time, timezone details, and available timezone IDs.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Default Calendar Information:</b> Verifies logging of calendar information with default timezone details</li>
 *   <li><b>Custom Calendar Information:</b> Tests reporting with custom timezone and date settings</li>
 * </ul>
 */
@DisplayName("ReportCalendar")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportCalendarTest {

    @Slf4jMock
    private Logger logger;

    @Test
    @DisplayName("should log default calendar information")
    void shouldLogDefaultCalendarInformation() {
        // Given: a calendar info provider with fixed date and default timezone
        final Date fixedCurrentDate = new Date(1678886400000L); // March 15, 2023 00:00:00 GMT
        final TimeZone defaultTimeZone = TimeZone.getDefault();

        final ReportCalendar.CalendarInfoProvider provider = new ReportCalendar.CalendarInfoProvider() {
            @Override
            public Date getCurrentDate() {
                return fixedCurrentDate;
            }

            @Override
            public TimeZone getDefaultTimeZone() {
                return TimeZone.getDefault();
            }

            @Override
            public String[] getAvailableTimeZoneIDs() {
                return TimeZone.getAvailableIDs();
            }
        };

        final ReportCalendar report = new ReportCalendar(logger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log calendar information with default timezone details
        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(defaultTimeZone);
        final String expectedDateString = df.format(fixedCurrentDate);
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
    @DisplayName("should log custom calendar information")
    void shouldLogCustomCalendarInformation() {
        // Given: a calendar info provider with custom date, timezone, and available IDs
        final Date customDate = new Date(1678886400000L); // March 15, 2023 00:00:00 GMT
        final TimeZone customTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        final String[] customAvailableIDs = {"Europe/Berlin", "America/New_York", "Asia/Tokyo"};

        final ReportCalendar.CalendarInfoProvider provider = new ReportCalendar.CalendarInfoProvider() {
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

        final ReportCalendar report = new ReportCalendar(logger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // When: report is executed
        report.run();

        // Then: should log calendar information with custom timezone details and available IDs
        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(customTimeZone);
        final String expectedDateString = df.format(customDate);
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
