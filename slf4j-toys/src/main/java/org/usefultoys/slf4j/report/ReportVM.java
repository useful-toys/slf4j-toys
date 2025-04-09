package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * Reports basic information about the Java Virtual Machine (JVM), including vendor, version, and installation
 * directory.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportVM implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Java Virtual Machine");
        ps.println(" - vendor: " + System.getProperty("java.vendor"));
        ps.println(" - version: " + System.getProperty("java.version"));
        ps.println(" - installation directory: " + System.getProperty("java.home"));
        ps.close();
    }
}
