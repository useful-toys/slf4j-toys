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
import java.nio.charset.Charset;

/**
 * A report module that provides information about the system's default character set and lists all available character sets.
 * This report is useful for diagnosing encoding-related issues in the application's environment.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportCharset
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportCharset implements Runnable {

    private final @NonNull Logger logger;

    /**
     * Executes the report, writing character set information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Charset charset = Charset.defaultCharset();
        ps.println("Charset");
        ps.printf(" - default charset: %s", charset.displayName());
        ps.printf("; name=%s", charset.name());
        ps.printf("; canEncode=%s%n", charset.canEncode());
        ps.print(" - available charsets: ");
        int i = 1;
        for (final Charset l : Charset.availableCharsets().values()) {
            if (i++ % 15 == 0) {
                ps.printf("%n      ");
            }
            ps.printf("%s; ", l.displayName());
        }
        ps.println(); // Ensure a newline at the end of the report
    }
}
