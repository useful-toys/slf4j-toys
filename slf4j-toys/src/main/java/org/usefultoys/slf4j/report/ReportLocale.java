package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Reports the default system locale and lists all available locales
 * with their respective language, country, script, and variant.
 */
@RequiredArgsConstructor
public class ReportLocale implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Locale loc = Locale.getDefault();
        ps.println("Locale");
        ps.printf(" - default locale: %s", loc.getDisplayName());
        ps.printf("; language=%s (%s)", loc.getDisplayLanguage(), loc.getLanguage());
        ps.printf("; country=%s (%s)", loc.getDisplayCountry(), loc.getCountry());
        //noinspection ErrorNotRethrown
        try {
            ps.printf("; script=%s (%s)", loc.getDisplayScript(), loc.getScript());
        } catch (final NoSuchMethodError ignored) {
            // Ignore property that exists only from Java 1.7 on.
        }
        ps.printf("; variant=%s (%s)", loc.getDisplayVariant(), loc.getVariant());
        ps.println();
        ps.print(" - available locales: ");
        int i = 1;
        for (final Locale l : Locale.getAvailableLocales()) {
            if (i++ % 8 == 0) {
                ps.print("\n      ");
            }
            ps.printf("%s; ", l.getDisplayName());
        }
        ps.close();
    }
}
