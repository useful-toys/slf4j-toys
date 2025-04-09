package org.usefultoys.slf4j.report;

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
@RequiredArgsConstructor
public class ReportCalendar implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
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
                ps.print("\n      ");
            }
            ps.printf("%s; ", id);
        }
        ps.close();
    }
}
