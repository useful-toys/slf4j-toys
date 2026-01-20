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

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A report module that provides information about the system's calendar and time zone settings.
 * It reports the current date and time, the default time zone configuration, and lists all available time zone IDs.
 * This report is useful for diagnosing environment-specific time-related issues.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportCalendar
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportCalendar implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Interface for providing Calendar and TimeZone information to the report.
     * <p>
     * This interface can be overridden in tests to simulate different date/time and timezone scenarios.
     * The default implementation uses the standard Java API (`Date` and `TimeZone` classes).
     */
    protected interface CalendarInfoProvider {
        /**
         * @return the current date and time.
         */
        Date getCurrentDate();

        /**
         * @return the default time zone for this Java virtual machine.
         */
        TimeZone getDefaultTimeZone();

        /**
         * @return an array of all available time zone IDs.
         */
        String[] getAvailableTimeZoneIDs();
    }

    /**
     * Returns the provider for calendar and timezone information used by this report.
     * <p>
     * The default implementation returns a provider based on the standard Java API.
     * This method can be overridden in subclasses for testing or custom information sources.
     *
     * @return a CalendarInfoProvider instance
     */
    protected CalendarInfoProvider getCalendarInfoProvider() {
        return new DefaultCalendarInfoProvider();
    }

    /**
     * Executes the report, writing calendar and time zone information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final CalendarInfoProvider provider = getCalendarInfoProvider();
        final Date currentDate = provider.getCurrentDate();
        final TimeZone tz = provider.getDefaultTimeZone();

        ps.println("Calendar");
        final DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        dateTimeInstance.setTimeZone(tz);
        ps.printf(" - current date/time: %s%n", dateTimeInstance.format(currentDate));
        ps.printf(" - default timezone: %s (%s)%n", tz.getDisplayName(), tz.getID());
        ps.printf("; DST=%dmin", tz.getDSTSavings() / 60000);
        ps.printf("; observesDST=%s", tz.observesDaylightTime());
        ps.printf("; useDST=%s", tz.useDaylightTime());
        ps.printf("; inDST=%s", tz.inDaylightTime(currentDate));
        ps.printf("; offset=%dmin%n", tz.getRawOffset() / 60000);
        ps.print(" - available IDs: ");
        int i = 1;
        for (final String id : provider.getAvailableTimeZoneIDs()) {
            if (i++ % 8 == 0) {
                ps.printf("%n      ");
            }
            ps.printf("%s; ", id);
        }
        ps.println(); // Ensure a newline at the end of the report
    }

    /**
     * Default implementation of CalendarInfoProvider using the standard Java API.
     */
    private static class DefaultCalendarInfoProvider implements CalendarInfoProvider {
        @Override
        public Date getCurrentDate() {
            return new Date();
        }

        @Override
        public TimeZone getDefaultTimeZone() {
            return TimeZone.getDefault();
        }

        @Override
        public String[] getAvailableTimeZoneIDs() {
            return TimeZone.getAvailableIDs();
        }
    }
}
