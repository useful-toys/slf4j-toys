package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.PrintStream;

/**
 * Reports memory usage of the JVM, including maximum available memory, currently allocated memory, and currently used
 * memory.
 */
public class ReportMemory implements Runnable {

    private final Logger logger;

    public ReportMemory(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Runtime runtime = Runtime.getRuntime();
        ps.println("Memory:");
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        ps.println(" - maximum allowed: " + (maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory)));
        ps.println(" - currently allocated: " + UnitFormatter.bytes(totalMemory) + " (" + UnitFormatter.bytes(maxMemory - totalMemory) + " more available)");
        ps.println(" - currently used: " + UnitFormatter.bytes(totalMemory - freeMemory) + " (" + UnitFormatter.bytes(freeMemory) + " free)");
        ps.close();
    }
}
