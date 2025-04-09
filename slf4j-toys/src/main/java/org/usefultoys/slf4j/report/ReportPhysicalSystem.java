package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * Reports basic information about the physical system, such as the number of available processors.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportPhysicalSystem implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final Runtime runtime = Runtime.getRuntime();
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Physical system");
        ps.printf(" - processors: %d%n", runtime.availableProcessors());
        ps.close();
    }
}
