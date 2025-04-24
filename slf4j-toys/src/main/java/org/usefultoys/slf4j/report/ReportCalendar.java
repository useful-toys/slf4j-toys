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
 * Reports the current date and time, default time zone configuration, and lists all available time zone IDs.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportCalendar implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Calendar");
        ps.printf(" - current date/time: %s", DateFormat.getDateTimeInstance().format(new Date()));
        final TimeZone tz = TimeZone.getDefault();
        ps.printf(" - default timezone: %s", tz.getDisplayName());
        ps.printf(" (%s)", tz.getID());
        ps.printf("; DST=%dmin", tz.getDSTSavings() / 60000);
        //noinspection ErrorNotRethrown
        try {
            ps.printf("; observesDT=%s", tz.observesDaylightTime());
        } catch (final NoSuchMethodError ignored) {
            // Ignore property that exists only from Java 1.7 on.
        }
        ps.printf("; useDT=%s", tz.useDaylightTime());
        ps.printf("; inDT=%s", tz.inDaylightTime(new Date()));
        ps.printf("; offset=%dmin", tz.getRawOffset() / 60000);
        ps.println();
        ps.print(" - available IDs: ");
        int i = 1;
        for (final String id : TimeZone.getAvailableIDs()) {
            if (i++ % 8 == 0) {
                ps.printf("%n      ");
            }
            ps.printf("%s; ", id);
        }
    }
}
