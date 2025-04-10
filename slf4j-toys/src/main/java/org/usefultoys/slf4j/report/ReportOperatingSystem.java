package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

import static org.usefultoys.slf4j.report.ReporterConfig.getPropertySafely;

/**
 * Reports properties of the operating system, including architecture, name, version, and path/file/line separators.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportOperatingSystem implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Operating System");
        ps.printf(" - architecture: %s%n", getPropertySafely("os.arch"));
        ps.printf(" - name: %s%n", getPropertySafely("os.name"));
        ps.printf(" - version: %s%n", getPropertySafely("os.version"));
        ps.printf(" - file separator: %s%n", Integer.toHexString(getPropertySafely("file.separator").charAt(0)));
        ps.printf(" - path separator: %s%n", Integer.toHexString(getPropertySafely("path.separator").charAt(0)));
        ps.printf(" - line separator: %s%n", Integer.toHexString(getPropertySafely("line.separator").charAt(0)));
    }
}
