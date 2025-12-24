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
import java.util.Locale;

/**
 * A report module that provides information about the system's default locale and lists all available locales.
 * It details the language, country, script, and variant for each locale.
 * This report is useful for diagnosing internationalization (i18n) and localization (l10n) issues.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportLocale
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportLocale implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing locale information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Locale loc = Locale.getDefault();
        ps.println("Locale");
        ps.printf(" - default locale: %s%n", loc.getDisplayName());
        ps.printf("   ; language=%s (%s)", loc.getDisplayLanguage(), loc.getLanguage());
        ps.printf("; country=%s (%s)", loc.getDisplayCountry(), loc.getCountry());
        //noinspection ErrorNotRethrown
        try {
            ps.printf("; script=%s (%s)", loc.getDisplayScript(), loc.getScript());
        } catch (final NoSuchMethodError ignored) {
            // Ignore property that exists only from Java 1.7 on.
        }
        ps.printf("; variant=%s (%s)%n", loc.getDisplayVariant(), loc.getVariant());
        ps.print(" - available locales: ");
        int i = 1;
        for (final Locale l : Locale.getAvailableLocales()) {
            if (i++ % 8 == 0) {
                ps.printf("%n      ");
            }
            ps.printf("%s; ", l.getDisplayName());
        }
        ps.println(); // Ensure a newline at the end of the report
    }
}
