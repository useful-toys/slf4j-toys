package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Reports the default system locale and lists all available locales with their respective language, country, script,
 * and variant.
 */
@RequiredArgsConstructor
public class ReportCharset implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Charset charset = Charset.defaultCharset();
        ps.println("Charset");
        ps.print(" - default charset: " + charset.displayName());
        ps.print("; name=" + charset.name());
        ps.print("; canEncode=" + charset.canEncode());
        ps.println();
        ps.print(" - available charsets: ");
        int i = 1;
        for (final Charset l : Charset.availableCharsets().values()) {
            if (i++ % 15 == 0) {
                ps.print("\n      ");
            }
            ps.print(l.displayName() + "; ");
        }
        ps.close();
    }
}
