package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Reports the default system locale and lists all available locales with their respective language, country, script,
 * and variant.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportCharset implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Charset charset = Charset.defaultCharset();
        ps.println("Charset");
        ps.printf(" - default charset: %s", charset.displayName());
        ps.printf("; name=%s", charset.name());
        ps.printf("; canEncode=%s", charset.canEncode());
        ps.println();
        ps.print(" - available charsets: ");
        int i = 1;
        for (final Charset l : Charset.availableCharsets().values()) {
            if (i++ % 15 == 0) {
                ps.printf("%n      ");
            }
            ps.printf("%s; ", l.displayName());
        }
    }
}
