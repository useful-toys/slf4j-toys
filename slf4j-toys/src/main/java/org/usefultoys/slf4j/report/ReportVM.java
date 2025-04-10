package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

import static org.usefultoys.slf4j.report.ReporterConfig.getPropertySafely;

/**
 * Reports basic information about the Java Virtual Machine (JVM), including vendor, version, and installation
 * directory.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportVM implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Java Virtual Machine");
        ps.printf(" - vendor: %s%n", getPropertySafely("java.vendor"));
        ps.printf(" - version: %s%n", getPropertySafely("java.version"));
        ps.printf(" - installation directory: %s%n", getPropertySafely("java.home"));
    }
}
