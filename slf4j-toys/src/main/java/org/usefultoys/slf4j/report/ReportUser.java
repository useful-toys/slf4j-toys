package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

import static org.usefultoys.slf4j.report.ReporterConfig.getPropertySafely;

/**
 * Reports environment variables available to the current process.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportUser implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("User:");
        ps.printf(" - name: %s%n", getPropertySafely("user.name"));
        ps.printf(" - home: %s%n", getPropertySafely("user.home"));
    }
}
