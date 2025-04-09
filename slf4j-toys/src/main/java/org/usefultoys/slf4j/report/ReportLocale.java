package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Locale;

@SuppressWarnings("Since15")
/**
 * Reports the default system locale and lists all available locales
 * with their respective language, country, script, and variant.
 */
public class ReportLocale implements Runnable {

    private final Logger logger;

    public ReportLocale(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Locale loc = Locale.getDefault();
        ps.println("Locale");
        ps.print(" - default locale: " + loc.getDisplayName());
        ps.print("; language=" + loc.getDisplayLanguage() + " (" + loc.getLanguage() + ")");
        ps.print("; country=" + loc.getDisplayCountry() + " (" + loc.getCountry() + ")");
        try {
            ps.print("; script=" + loc.getDisplayScript() + " (" + loc.getScript() + ")");
        } catch (final NoSuchMethodError ignored) {
            // Ignore property that exists only from Java 1.7 on.
        }
        ps.print("; variant=" + loc.getDisplayVariant() + " (" + loc.getVariant() + ")");
        ps.println();
        ps.print(" - available locales: ");
        int i = 1;
        for (final Locale l : Locale.getAvailableLocales()) {
            if (i++ % 8 == 0) {
                ps.print("\n      ");
            }
            ps.print(l.getDisplayName() + "; ");
        }
        ps.close();
    }
}
