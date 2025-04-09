package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Reports the current date and time, default time zone configuration, and lists all available time zone IDs.
 */
@SuppressWarnings("Since15")
public class ReportCalendar implements Runnable {

    private final Logger logger;

    public ReportCalendar(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Calendar");
        ps.print(" - current date/time: " + DateFormat.getDateTimeInstance().format(new Date()));
        final TimeZone tz = TimeZone.getDefault();
        ps.print(" - default timezone: " + tz.getDisplayName());
        ps.print(" (" + tz.getID() + ")");
        ps.print("; DST=" + tz.getDSTSavings() / 60000 + "min");
        try {
            ps.print("; observesDT=" + tz.observesDaylightTime());
        } catch (final NoSuchMethodError ignored) {
            // Ignore property that exists only from Java 1.7 on.
        }
        ps.print("; useDT=" + tz.useDaylightTime());
        ps.print("; inDT=" + tz.inDaylightTime(new Date()));
        ps.print("; offset=" + tz.getRawOffset() / 60000 + "min");
        ps.println();
        ps.print(" - available IDs: ");
        int i = 1;
        for (final String id : TimeZone.getAvailableIDs()) {
            if (i++ % 8 == 0) {
                ps.print("\n      ");
            }
            ps.print(id + "; ");
        }
        ps.close();
    }
}
