package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.PrintStream;

/**
 * Reports memory usage of the JVM, including maximum available memory, currently allocated memory, and currently used
 * memory.
 */
@RequiredArgsConstructor
public class ReportMemory implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Runtime runtime = Runtime.getRuntime();
        ps.println("Memory:");
        final long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        ps.printf(" - maximum allowed: %s%n", maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory));
        ps.printf(" - currently allocated: %s (%s more available)%n", UnitFormatter.bytes(totalMemory), UnitFormatter.bytes(maxMemory - totalMemory));
        ps.printf(" - currently used: %s (%s free)%n", UnitFormatter.bytes(totalMemory - freeMemory), UnitFormatter.bytes(freeMemory));
        ps.close();
    }
}
